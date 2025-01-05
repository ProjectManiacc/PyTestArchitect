package com.pytestarchitect;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
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

    @Override
    public @Nullable LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        if (element instanceof PyFunction) {
            PsiElement functionName = ((PyFunction) element).getNameIdentifier();
            if (functionName != null) {
                return new LineMarkerInfo<>(
                        functionName,
                        functionName.getTextRange(),
                        GUTTER_ICON,
                        psiElement -> "Generate Tests for " + ((PyFunction) element).getName(),
                        (e, elt) -> triggerGenerateTests(element),
                        GutterIconRenderer.Alignment.LEFT,
                        () -> "Generate Tests"
                );
            }
        } else if (element instanceof PyClass) {
            PsiElement className = ((PyClass) element).getNameIdentifier();
            if (className != null) {
                return new LineMarkerInfo<>(
                        className,
                        className.getTextRange(),
                        GUTTER_ICON,
                        psiElement -> "Generate Tests for class " + ((PyClass) element).getName(),
                        (e, elt) -> triggerGenerateTests(element),
                        GutterIconRenderer.Alignment.LEFT,
                        () -> "Generate Tests"
                );
            }
        }
        return null;
    }


    private void triggerGenerateTests(PsiElement element) {
        ApplicationManager.getApplication().invokeLater(() -> {
            GenerateTestAction.triggerForElement(element);
        });    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> elements,
                                       @NotNull Collection<? super LineMarkerInfo<?>> result) {
    }

}
