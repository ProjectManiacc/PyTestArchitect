package com.pytestarchitect;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

public class PluginLineMarkerProvider implements LineMarkerProvider {

    private static final Logger log = LoggerFactory.getLogger(PluginLineMarkerProvider.class);
    private static final Icon GUTTER_ICON = IconLoader.getIcon("/icons/magicResolveDark.svg", PluginLineMarkerProvider.class);

    @Override
    public @Nullable LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        if (!isSupportedElement(element)) {
            return null;
        }
        return createLineMarkerInfo(element);
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> elements,
                                       @NotNull Collection<? super LineMarkerInfo<?>> result) {
    }

    private boolean isSupportedElement(PsiElement element) {
        return element instanceof PyClass || element instanceof PyFunction;
    }

    private LineMarkerInfo<PsiElement> createLineMarkerInfo(PsiElement element) {
        return new LineMarkerInfo<>(
                element,
                element.getTextRange(),
                GUTTER_ICON,
                psiElement -> "Generate Tests",
                (event, elt) -> triggerGenerateTests(element),
                GutterIconRenderer.Alignment.LEFT,
                () -> "Generate Tests"
        );
    }

    private void triggerGenerateTests(PsiElement element) {
        try {
            Project project = getProjectFromElement(element);
            AnAction action = getGenerateTestAction();
            Editor editor = getSelectedEditor(project);
            DataContext dataContext = createDataContext(element);

            executeAction(action, dataContext);
        } catch (IllegalStateException e) {
            log.warn(e.getMessage());
        }
    }

    private Project getProjectFromElement(PsiElement element) {
        Project project = element.getProject();
        if (project == null) {
            throw new IllegalStateException("Project not found.");
        }
        return project;
    }

    private AnAction getGenerateTestAction() {
        AnAction action = ActionManager.getInstance().getAction("com.pytestarchitect.GenerateTestAction");
        if (action == null) {
            throw new IllegalStateException("Generate Test action not found.");
        }
        return action;
    }

    private Editor getSelectedEditor(Project project) {
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            throw new IllegalStateException("Editor not found.");
        }
        return editor;
    }

    private DataContext createDataContext(PsiElement element) {
        return SimpleDataContext.builder()
                .add(CommonDataKeys.PSI_ELEMENT, element)
                .build();
    }

    private void executeAction(AnAction action, DataContext dataContext) {
        ActionManager.getInstance().tryToExecute(
                action, null, null, ActionPlaces.UNKNOWN, false
        );
    }
}
