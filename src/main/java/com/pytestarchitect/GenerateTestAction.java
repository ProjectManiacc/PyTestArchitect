package com.pytestarchitect;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import com.intellij.psi.*;

public class GenerateTestAction extends AnAction {

    private static TestGenerationService testGenerationService;

    static {
          testGenerationService  = new DummyTestGenerationService();
    }

    public static void setTestGenerationService(TestGenerationService service) {
        GenerateTestAction.testGenerationService = service;
    }

    public static TestGenerationService getTestGenerationService() {
        return GenerateTestAction.testGenerationService;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        var project = event.getProject();
        if (project == null) return;

        var editor = event.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR);
        if (editor == null) return;

        var psiFile = com.intellij.psi.PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (psiFile == null) return;

        if (psiFile.getVirtualFile() != null && "py".equalsIgnoreCase(psiFile.getVirtualFile().getExtension())) {
            var pythonCode = psiFile.getText();
            TestState.setLastExtractedCode(pythonCode);

            String generatedTests = testGenerationService.generateTests(pythonCode);
            TestState.setLastGeneratedTests(generatedTests);

            if (generatedTests == null || generatedTests.isEmpty()) {
                Notifications.Bus.notify(
                        new Notification(
                                "Test Generation",
                                "Test Generation Failed",
                                "Failed to generate tests for the provided code. Please check the logs for details.",
                                NotificationType.WARNING
                        ),
                        project
                );
            }

        } else {
            TestState.setLastExtractedCode(null);
            TestState.setLastGeneratedTests(null);
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

        // Optionally show a notification or insert a file with tests
        Notification notification = new Notification("Test Generation", "Generated Tests", testCode, NotificationType.INFORMATION);
        Notifications.Bus.notify(notification);
    }

    private static String extractCodeForElement(PsiElement element) {
        return element.getText();
    }


}
