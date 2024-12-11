package com.pytestarchitect;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.testFramework.LightPlatformTestCase;

public class ActionTest extends LightPlatformTestCase {
    public void testAction() {
        assertNotNull("Action needs to be registered in plugin.xml",
                ActionManager.getInstance().getAction("com.pytestarchitect.GenerateTestAction"));
    }
}
