package com.pytestarchitect;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import org.jetbrains.annotations.NotNull;

public class GenerateTestAction extends AnAction {

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

        if (psiFile.getVirtualFile() != null && "py".equalsIgnoreCase(psiFile.getVirtualFile().getExtension())) {
            int caretOffset = editor.getCaretModel().getOffset();
            PsiElement elementAtCaret = psiFile.findElementAt(caretOffset);

            PsiElement targetElement = PsiTreeUtil.getParentOfType(elementAtCaret, PyClass.class, PyFunction.class);

            if (targetElement == null) {
                Notifications.Bus.notify(
                        new Notification(
                                "Test Generation",
                                "Test Generation Failed",
                                "No valid class or function found at the caret position. Please click on a class or function to generate tests.",
                                NotificationType.WARNING
                        ),
                        project
                );
                return;
            }

            String sourceCode = extractCodeForElement(targetElement);

            if (sourceCode == null || sourceCode.isEmpty()) {
                Notifications.Bus.notify(
                        new Notification(
                                "Test Generation",
                                "Test Generation Failed",
                                "Failed to extract code for the selected element.",
                                NotificationType.WARNING
                        ),
                        project
                );
                return;
            }

            String generatedTests = testGenerationService.generateTests(sourceCode);

            if (generatedTests == null || generatedTests.isEmpty()) {
                Notifications.Bus.notify(
                        new Notification(
                                "Test Generation",
                                "Test Generation Failed",
                                "Failed to generate tests for the selected element. Please check the logs for details.",
                                NotificationType.WARNING
                        ),
                        project
                );
                return;
            }

            Notifications.Bus.notify(
                    new Notification(
                            "Test Generation",
                            "Generated Tests",
                            generatedTests,
                            NotificationType.INFORMATION
                    ),
                    project
            );

        } else {
            Notifications.Bus.notify(
                    new Notification(
                            "Test Generation",
                            "Test Generation Failed",
                            "The selected file is not a valid Python file.",
                            NotificationType.WARNING
                    ),
                    project
            );
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

    private static String extractCodeForElement(PsiElement element) {
        return element.getText();
    }
}
