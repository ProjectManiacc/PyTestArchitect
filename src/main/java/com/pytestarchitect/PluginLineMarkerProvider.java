package com.pytestarchitect;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.*;
import com.intellij.codeInsight.daemon.*;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;
import java.util.Collection;
import java.util.List;

public class PluginLineMarkerProvider implements LineMarkerProvider{
    private static final Icon TEST_ICON = com.intellij.icons.AllIcons.RunConfigurations.TestState.Run;

    @Override
    @Nullable
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (element instanceof PyClass) {
            return createMarker(element, "Generate tests for class");
        }
        else if (element instanceof PyFunction) {
            return createMarker(element, "Generate tests for function");
        }
        return null;
    }

    private LineMarkerInfo<?> createMarker(PsiElement element, String tooltip) {
        return new LineMarkerInfo<>(
                element,
                element.getTextRange(),
                TEST_ICON,
                psi -> tooltip,
                (mouse, elt) -> onMarkerClick(elt),
                GutterIconRenderer.Alignment.LEFT,
                () -> tooltip
        );
    }

    private void onMarkerClick(PsiElement element) {
        GenerateTestAction.triggerForElement(element);
    }

}
