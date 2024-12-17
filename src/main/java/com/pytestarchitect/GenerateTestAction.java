package com.pytestarchitect;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.WriteAction;
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
    private static TestGenerationService testGenerationService;

    static {
        testGenerationService = new DummyTestGenerationService();
    }

    public static void setTestGenerationService(TestGenerationService service) {
        GenerateTestAction.testGenerationService = service;
    }

    public static TestGenerationService getTestGenerationService() {
        return GenerateTestAction.testGenerationService;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) return;

        var editor = event.getData(CommonDataKeys.EDITOR);
        if (editor == null) return;

        var psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (psiFile == null) return;

        VirtualFile fileUnderTests = psiFile.getVirtualFile();
        String importPath = getRelativeImportPath(project, fileUnderTests);
        if (importPath == null) {
            log.warn("Could not determine import path for the file. {}, {}", project.getName(), fileUnderTests.getPath());
            return;
        }

        int offset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(offset);
        if (element == null) {
            log.warn("No element found at offset {}", offset);
            return;
        }

        PyClass pyClass = PsiTreeUtil.getParentOfType(element, PyClass.class);
        PyFunction pyFunction = PsiTreeUtil.getParentOfType(element, PyFunction.class);

        String name;
        if (pyClass != null) {
            name = pyClass.getName();
        } else if (pyFunction != null) {
            name = pyFunction.getName();
        } else {
            log.warn("No valid class or function found at offset {}", offset);
            return;
        }

        PsiElement targetElement = pyClass != null ? pyClass : pyFunction;
        String sourceCode = extractCodeForElement(targetElement);

        String augmentedSourceCode = "# Import path: from " + importPath + " import *\n" + sourceCode;


        String testCode = testGenerationService.generateTests(augmentedSourceCode);
        if (testCode == null || testCode.isEmpty()) {
            log.warn("No tests generated for {}", name);
            log.warn("Source code: {}", sourceCode);
            return;
        }
        try {
            VirtualFile baseDir = project.getBaseDir();
            if (baseDir == null) {
                log.warn("Project base directory not found: {}", project.getName());
                return;
            }

            VirtualFile testsDir = WriteAction.compute(() -> {
                VirtualFile dir = baseDir.findChild("tests");
                if (dir == null) {
                    dir = baseDir.createChildDirectory(this, "tests");
                }
                return dir;
            });

            String fileName = "test_" + name.toLowerCase() + ".py";
            VirtualFile testFile = WriteAction.compute(() -> {
                VirtualFile file = testsDir.findChild(fileName);
                if (file == null) {
                    file = testsDir.createChildData(this, fileName);
                }
                return file;
            });

            WriteAction.run(() -> VfsUtil.saveText(testFile, testCode));

            Notifications.Bus.notify(
                    new Notification(
                            "Test Generation",
                            "Success",
                            "Tests generated successfully in " + testFile.getPath(),
                            NotificationType.INFORMATION
                    ),
                    project
            );
        } catch (IOException ioException) {
            log.error("Could not create tests: {}", ioException.getMessage());
        }

    }

    public static void triggerForElement(PsiElement element) {
        String code = extractCodeForElement(element);
        if (code == null || code.isEmpty()) {
            TestState.setLastGeneratedTests("No code found for this element.");
            return;
        }

        String testCode = testGenerationService.generateTests(code);
        TestState.setLastGeneratedTests(testCode);

        Notification notification = new Notification(
                "Test Generation",
                "Generated Tests",
                testCode,
                NotificationType.INFORMATION
        );
        Notifications.Bus.notify(notification);
    }

    private String getRelativeImportPath(Project project, VirtualFile file) {
        VirtualFile baseDir = project.getBaseDir();
        if (baseDir == null) return null;

        String relativePath = VfsUtil.getRelativePath(file, baseDir, '/');
        if (relativePath == null) return null;

        relativePath = relativePath.replaceAll("\\.py$", "").replace('/', '.');
        return relativePath;
    }


    private static String extractCodeForElement(PsiElement element) {
        return element.getText();
    }
}
