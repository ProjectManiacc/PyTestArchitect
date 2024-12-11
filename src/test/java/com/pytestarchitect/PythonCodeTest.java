package com.pytestarchitect;

import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.TestActionEvent;
import com.intellij.openapi.actionSystem.ActionManager;

import static org.junit.Assert.assertEquals;

public class PythonCodeTest extends BasePlatformTestCase {
    public void testExtractPythonCode() {
        var pythonFile = myFixture.configureByText("test.py", "def test():\n    print('Hello, World!')");
        var action = ActionManager.getInstance().getAction("com.pytestarchitect.GenerateTestAction");
        AnActionEvent event = new createTestEvent(dataId -> {
            if (CommonDataKeys.PROJECT.is(dataId)) {
                return myFixture.getProject();
            }
            if (CommonDataKeys.EDITOR.is(dataId)) {
                return myFixture.getEditor();
            }
            return null;
        });

        action.actionPerformed(event);

        String extractedCode = TestState.getLastExtractedCode();
        assertEquals("def test():\n    print('Hello, World!')", extractedCode);
    }
}
