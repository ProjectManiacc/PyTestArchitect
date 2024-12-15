package com.pytestarchitect;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import org.junit.Test;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;

public class LineMarkerTest extends LightPlatformCodeInsightFixture4TestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }

    @Test
    public void testLineMarker() {
        myFixture.configureByFile("python_class_example.py");
        PsiFile file = myFixture.getFile();
        PyClass pyClass = PsiTreeUtil.findChildOfType(file, PyClass.class);
        PyFunction pyFunction = PsiTreeUtil.findChildOfType(file, PyFunction.class);

        PluginLineMarkerProvider provider = new PluginLineMarkerProvider();
        LineMarkerInfo<?> classMarker = provider.getLineMarkerInfo(pyClass);
        LineMarkerInfo<?> functionMarker = provider.getLineMarkerInfo(pyFunction);

        assertNotNull("Class marker should exist", classMarker);
        assertEquals("Generate tests for class", classMarker.getLineMarkerTooltip());

        assertNotNull("Function marker should exist", functionMarker);
        assertEquals("Generate tests for function", functionMarker.getLineMarkerTooltip());

        }

    @Test
    public void testGutterClickGeneratesTestsForClassOnly() {
        myFixture.configureByText("python_class_example.py",
                "class Foo:\n" +
                        "    def bar(self):\n" +
                        "        return 42\n");

        PsiFile file = myFixture.getFile();
        PyClass pyClass = PsiTreeUtil.findChildOfType(file, PyClass.class);
        assertNotNull(pyClass);

        GenerateTestAction.triggerForElement(pyClass);
        String generatedTests = TestState.getLastGeneratedTests();
        assertNotNull(generatedTests);
        assertTrue(generatedTests.contains("foo"));
    }
}
