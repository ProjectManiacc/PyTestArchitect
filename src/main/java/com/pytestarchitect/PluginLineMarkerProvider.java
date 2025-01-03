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
        System.out.println("Inspecting element: {}" +  element.getText());

        if (element instanceof PyClass || element instanceof PyFunction) {
            System.out.println("Element eligible for LineMarker: " + element.getText());

            return new LineMarkerInfo<>(
                    element,
                    element.getTextRange(),
                    GUTTER_ICON,
                    psiElement -> "Generate Tests",
                    (e, elt) -> triggerGenerateTests(elt),
                    GutterIconRenderer.Alignment.LEFT,
                    () -> "Generate Tests"
            );
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
