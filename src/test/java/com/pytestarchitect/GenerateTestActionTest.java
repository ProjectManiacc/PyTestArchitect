package com.pytestarchitect;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GenerateTestActionTest {

    private GenerateTestAction action;
    private AnActionEvent mockEvent;
    private Project mockProject;
    private PsiFile mockPsiFile;
    private VirtualFile mockVirtualFile;

    @Before
    public void setUp() {
        action = new GenerateTestAction();
        mockEvent = mock(AnActionEvent.class);
        mockProject = mock(Project.class);
        mockPsiFile = mock(PsiFile.class);
        mockVirtualFile = mock(VirtualFile.class);
    }

    @Test
    public void testSetAndGetTestGenerationService() {
        TestGenerationService mockService = mock(TestGenerationService.class);

        GenerateTestAction.setTestGenerationService(mockService);
        TestGenerationService retrievedService = GenerateTestAction.getTestGenerationService();

        assertSame("The set and retrieved services should be the same", mockService, retrievedService);
    }

    @Test
    public void testIsValidSyntax() {
        String validCode = "def foo():\n    return 42";
        String invalidCode = "def foo()\n    return 42";

        assertTrue("Valid syntax should return true", action.isValidSyntax(validCode));
        assertFalse("Invalid syntax should return false", action.isValidSyntax(invalidCode));
    }

    @Test
    public void testCreateAugmentedSourceCode() {
        GenerateTestAction.TestContext testContext = new GenerateTestAction.TestContext(
                "MyClass",
                "class MyClass:\n    pass",
                "my_module"
        );

        String augmentedSourceCode = action.createAugmentedSourceCode(testContext);

        assertEquals("Augmented source code should include import and source",
                "# Import path: from my_module import MyClass\nclass MyClass:\n    pass",
                augmentedSourceCode);
    }


    @Test
    public void testGetRelativeImportPath_NullProject() {
        String relativePath = action.getRelativeImportPath(null, mockVirtualFile);

        assertNull("Null project should return null for import path", relativePath);
    }

    @Test
    public void testSaveGeneratedTests_Success() throws Exception {
        Application mockApplication = mock(Application.class);
        Disposable mockDisposable = mock(Disposable.class);
        ApplicationManager.setApplication(mockApplication, mockDisposable);

        VirtualFile mockTestsDir = mock(VirtualFile.class);
        VirtualFile mockTestFile = mock(VirtualFile.class);
        when(mockProject.getBasePath()).thenReturn("/project");

        when(mockTestsDir.createChildData(any(), eq("test_myclass.py"))).thenReturn(mockTestFile);
        when(mockTestFile.getPath()).thenReturn("/project/tests/test_myclass.py");

        GenerateTestAction.saveGeneratedTests(mockProject, "MyClass", "test_code");

        verify(mockApplication).invokeLater(any(Runnable.class));
    }


}
