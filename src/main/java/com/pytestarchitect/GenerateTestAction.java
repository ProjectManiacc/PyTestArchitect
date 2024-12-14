package com.pytestarchitect;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class GenerateTestAction extends AnAction {

    private static TestGenerationService testGenerationService;

    static {
          testGenerationService  = new DummyTestGenerationService();
    }

    public static void setTestGenerationService(TestGenerationService service) {
        GenerateTestAction.testGenerationService = service;
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

            Notifications.Bus.notify(
                    new Notification("Test Generation", "Generated Tests", generatedTests, NotificationType.INFORMATION),
                    project
            );
        } else {
            TestState.setLastExtractedCode(null);
            TestState.setLastGeneratedTests(null);
        }

    }
}
