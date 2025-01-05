package com.pytestarchitect;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.ide.DataManager;
import com.intellij.idea.IdeaLogger;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.net.URL;
import java.util.Collection;
import java.util.List;

public class PluginLineMarkerProvider implements LineMarkerProvider{
    private static final Icon GUTTER_ICON = IconLoader.getIcon("/icons/magicResolveDark.svg", PluginLineMarkerProvider.class);
    IdeaLogger log;

    @Override
    public @Nullable LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        if (element instanceof PyClass || element instanceof PyFunction) {
            return new LineMarkerInfo<>(
                    element,
                    element.getTextRange(),
                    GUTTER_ICON,
                    psiElement -> "Generate Tests",
                    (e, elt) -> triggerGenerateTests(element),
                    GutterIconRenderer.Alignment.LEFT,
                    () -> "Generate Tests"
            );
        }
        return null;
    }


    private void triggerGenerateTests(PsiElement element) {
        Project project = element.getProject();
        if (project == null) {
            log.warn("Project not found.");
            return;
        }

        AnAction action = ActionManager.getInstance().getAction("com.pytestarchitect.GenerateTestAction");
        if (action == null) {
            log.warn("Generate Test action not found.");
            return;
        }

        DataContext dataContext = SimpleDataContext.getSimpleContext(CommonDataKeys.PSI_ELEMENT.getName(), element, DataManager.getInstance().getDataContext());
        AnActionEvent actionEvent = AnActionEvent.createFromDataContext(
                ActionPlaces.UNKNOWN,
                null,
                dataContext
        );

        action.actionPerformed(actionEvent);
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> elements,
                                       @NotNull Collection<? super LineMarkerInfo<?>> result) {
    }

}
