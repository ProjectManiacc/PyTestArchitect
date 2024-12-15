package com.pytestarchitect;

import com.intellij.testFramework.LightPlatformTestCase;
import org.junit.Test;

public class PluginStartupActivityTest  extends LightPlatformTestCase {
    @Test
    public void testStartupActivityWithApiKey() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        PluginStartupActivity pluginStartupActivity = new PluginStartupActivity();
        pluginStartupActivity.runActivity(getProject());

        TestGenerationService currentService = GenerateTestAction.getTestGenerationService();
        assertNotNull("TestGenerationService should not be null",currentService);
        assertTrue("TestGenerationService should be an instance of AIBackendTestGenerationService",currentService instanceof AIBackendTestGenerationService);

        AIBackendTestGenerationService aiService = (AIBackendTestGenerationService) currentService;
        assertTrue("AIBackendTestGenerationService should use RealAIClient",aiService.client instanceof RealAIClient);
    }


}
