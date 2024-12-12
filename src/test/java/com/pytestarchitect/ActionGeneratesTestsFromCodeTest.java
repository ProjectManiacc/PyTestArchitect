package com.pytestarchitect;

import com.intellij.openapi.actionSystem.*;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.TestActionEvent;
public class ActionGeneratesTestsFromCodeTest extends BasePlatformTestCase {

    @Test
    public void testActionGeneratesTestsFromCode(){
        var pythonFile = myFixture.configureByText("example.py", "def foo():\n    return 42");
        var action = ActionManager.getInstance().getAction("com.pytestarchitect.GenerateTestAction");
        assertNotNull("Action needs to be registered in plugin.xml", action);

        DataContext dataContext = dataId -> {
            if (CommonDataKeys.PROJECT.is(dataId)) {
                return myFixture.getProject();
            }
            if (CommonDataKeys.EDITOR.is(dataId)) {
                return myFixture.getEditor();
            }
            return null;
        };
        AnActionEvent event = AnActionEvent.createFromDataContext(ActionPlaces.UNKNOWN, null, dataContext);
        action.actionPerformed(event);

        String generatedTests = TestState.getLastGeneratedTests();
        assertNotNull(generatedTests);
        assertTrue(generatedTests.contains("def test_foo():"));
    }
}
