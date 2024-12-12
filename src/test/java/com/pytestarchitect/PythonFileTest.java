package com.pytestarchitect;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.testFramework.TestActionEvent;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.Test;

public class PythonFileTest extends LightPlatformCodeInsightFixture4TestCase {


    @Test
    public void testExtractPythonCodeFromFile(){
        myFixture.setTestDataPath("src/test/testData");
        myFixture.configureByFile("python_example.py");
        var action = com.intellij.openapi.actionSystem.ActionManager.getInstance().getAction("com.pytestarchitect.GenerateTestAction");
        DataContext dataContext = dataId -> {
            if (CommonDataKeys.PROJECT.is(dataId)) {
                return myFixture.getProject();
            }
            if (CommonDataKeys.EDITOR.is(dataId)) {
                return myFixture.getEditor();
            }
            return null;
        };
        var event = new TestActionEvent(dataContext, action);
        action.actionPerformed(event);
        var extractedCode = TestState.getLastExtractedCode();
        assertEquals("def foo(x):\n    return x * 2", extractedCode);
    }

}
