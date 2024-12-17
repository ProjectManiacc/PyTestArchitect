package com.pytestarchitect;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GenerateTestAction extends AnAction {

    private static final Logger log = LoggerFactory.getLogger(GenerateTestAction.class);
    private static TestGenerationService testGenerationService = new DummyTestGenerationService();

    // Setter for dependency injection (testing purposes)
    public static void setTestGenerationService(TestGenerationService service) {
        testGenerationService = service;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) return;

        TestContext testContext = getTestContext(event, project);
        if (testContext == null) return;

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Generating Tests...", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                indicator.setText("Contacting AI to generate tests...");

                String augmentedSourceCode = createAugmentedSourceCode(testContext.sourceCode, testContext.importPath);
                String testCode = testGenerationService.generateTests(augmentedSourceCode);

                if (testCode == null || testCode.isEmpty()) {
                    log.warn("No tests generated for {}", testContext.name);
                    notifyUser(project, "Failed to generate tests for " + testContext.name, NotificationType.WARNING);
                    return;
                }

                saveGeneratedTests(project, testContext.name, testCode);
            }
        });
    }


    private TestContext getTestContext(@NotNull AnActionEvent event, Project project) {
        var editor = event.getData(CommonDataKeys.EDITOR);
        if (editor == null) return null;

        var psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (psiFile == null) return null;

        VirtualFile fileUnderTests = psiFile.getVirtualFile();
        String importPath = getRelativeImportPath(project, fileUnderTests);
        if (importPath == null) {
            log.warn("Could not determine import path for the file.");
            return null;
        }

        PsiElement element = psiFile.findElementAt(editor.getCaretModel().getOffset());
        if (element == null) {
            log.warn("No element found at caret position.");
            return null;
        }

        PyClass pyClass = PsiTreeUtil.getParentOfType(element, PyClass.class);
        PyFunction pyFunction = PsiTreeUtil.getParentOfType(element, PyFunction.class);

        String name = pyClass != null ? pyClass.getName() : (pyFunction != null ? pyFunction.getName() : null);
        if (name == null) {
            log.warn("No valid class or function found at caret position.");
            return null;
        }

        PsiElement targetElement = pyClass != null ? pyClass : pyFunction;
        String sourceCode = targetElement.getText();

        return new TestContext(name, sourceCode, importPath);
    }


    private String createAugmentedSourceCode(String sourceCode, String importPath) {
        return "# Import path: from " + importPath + " import *\n" + sourceCode;
    }


    private static void saveGeneratedTests(Project project, String testName, String testCode) {
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                VirtualFile testsDir = createTestsDirectory(project);
                VirtualFile testFile = createTestFile(testsDir, testName, testCode);

                notifyUser(project, "Tests generated successfully in " + testFile.getPath(), NotificationType.INFORMATION);
            } catch (IOException e) {
                log.error("Could not create tests: {}", e.getMessage());
                notifyUser(project, "Error creating test file: " + e.getMessage(), NotificationType.ERROR);
            }
        });
    }


    private static VirtualFile createTestsDirectory(Project project) throws IOException {
        VirtualFile baseDir = project.getBaseDir();
        if (baseDir == null) throw new IOException("Project base directory not found.");

        return WriteAction.compute(() -> {
            VirtualFile dir = baseDir.findChild("tests");
            if (dir == null) {
                dir = baseDir.createChildDirectory(GenerateTestAction.class, "tests");
            }
            return dir;
        });
    }


    private static VirtualFile createTestFile(VirtualFile testsDir, String testName, String testCode) throws IOException {
        String fileName = "test_" + testName.toLowerCase() + ".py";
        return WriteAction.compute(() -> {
            VirtualFile file = testsDir.findChild(fileName);
            if (file == null) {
                file = testsDir.createChildData(GenerateTestAction.class, fileName);
            }
            VfsUtil.saveText(file, testCode);
            return file;
        });
    }


    private static void notifyUser(Project project, String message, NotificationType type) {
        Notifications.Bus.notify(
                new Notification("Test Generation", "Test Generation", message, type), project
        );
    }


    private String getRelativeImportPath(Project project, VirtualFile file) {
        VirtualFile baseDir = project.getBaseDir();
        if (baseDir == null) return null;

        String relativePath = VfsUtil.getRelativePath(file, baseDir, '/');
        if (relativePath == null) return null;

        return relativePath.replaceAll("\\.py$", "").replace('/', '.');
    }

    public static void triggerForElement(PsiElement element) {
        Project project = element.getProject();
        String sourceCode = element.getText();
        String name = element instanceof PyClass ? ((PyClass) element).getName()
                : element instanceof PyFunction ? ((PyFunction) element).getName()
                : "unknown";

        String augmentedSourceCode = "# Generating tests for: " + name + "\n" + sourceCode;

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Generating Tests...", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                indicator.setText("Contacting AI to generate tests...");

                String testCode = testGenerationService.generateTests(augmentedSourceCode);

                if (testCode == null || testCode.isEmpty()) {
                    notifyUser(project, "Failed to generate tests for " + name, NotificationType.WARNING);
                    return;
                }

                saveGeneratedTests(project, name, testCode);
            }
        });
    }

    public static String extractCodeForElement(PsiElement element) {
        if (element == null) return null;
        return element.getText();
    }


    private static class TestContext {
        final String name;
        final String sourceCode;
        final String importPath;

        TestContext(String name, String sourceCode, String importPath) {
            this.name = name;
            this.sourceCode = sourceCode;
            this.importPath = importPath;
        }
    }
}
