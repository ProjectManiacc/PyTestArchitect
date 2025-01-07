package com.pytestarchitect;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PluginLineMarkerProviderTest {

    private PluginLineMarkerProvider provider;
    private PsiElement mockElement;
    private Project mockProject;
    private Editor mockEditor;
    private AnAction mockAction;
    private FileEditorManager mockFileEditorManager;

    @Before
    public void setUp() {
        provider = new PluginLineMarkerProvider();

        mockElement = mock(PsiElement.class);
        mockProject = mock(Project.class);
        mockEditor = mock(Editor.class);
        mockAction = mock(AnAction.class);
        mockFileEditorManager = mock(FileEditorManager.class);

        when(FileEditorManager.getInstance(mockProject)).thenReturn(mockFileEditorManager);
    }


    @Test
    public void testGetLineMarkerInfo_UnsupportedElement() {
        when(mockElement.getContainingFile()).thenReturn(mock(PsiFile.class));

        LineMarkerInfo<PsiElement> markerInfo = provider.getLineMarkerInfo(mockElement);

        assertNull("LineMarkerInfo should be null for unsupported element", markerInfo);
    }


    @Test
    public void testCollectSlowLineMarkers_NoOp() {
        List<PsiElement> elements = new ArrayList<>();
        Collection<LineMarkerInfo<?>> result = new ArrayList<>();

        provider.collectSlowLineMarkers(elements, result);

        assertTrue("Result collection should remain empty", result.isEmpty());
    }
}
