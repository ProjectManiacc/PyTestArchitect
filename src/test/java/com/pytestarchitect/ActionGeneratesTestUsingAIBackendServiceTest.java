package com.pytestarchitect;

import com.intellij.openapi.actionSystem.*;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.Test;

public class ActionGeneratesTestUsingAIBackendServiceTest extends BasePlatformTestCase {

    @Test
    public void testActionGeneratesTestUsingAIBackendService(){
        myFixture.configureByText("example.py", "def foo():\n    return 42\n");
        AIClient mockClient = new AIClient() {
            @Override
            public String generateTests(String sourceCode) {
                return "def test_generated_by_ai():\n    assert foo() == 42\n";
            }
        };
        TestGenerationService service = new AIBackendTestGenerationService(mockClient);
        GenerateTestAction.setTestGenerationService(service);

        var action = ActionManager.getInstance().getAction("com.pytestarchitect.GenerateTestAction");
        assertNotNull(action);
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
        assertTrue(generatedTests.contains("def test_generated_by_ai():"));

    }
}
