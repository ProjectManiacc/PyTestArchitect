package com.pytestarchitect;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.startup.ProjectActivity;
import org.jetbrains.annotations.NotNull;

public class PluginStartupActivity implements StartupActivity.DumbAware {
    private static boolean wasRun = false;

    @Override
    public void runActivity(@NotNull Project project) {
        wasRun = true;
        System.out.println("PluginStartupActivity was triggered");
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("OpenAI API Key not found. Please set the OPENAI_API_KEY environment variable.");
            GenerateTestAction.setTestGenerationService(new DummyTestGenerationService());
            return;
        }
        AIClient aiClient = new RealAIClient(apiKey);
        TestGenerationService aiBackendService = new AIBackendTestGenerationService(aiClient);
        GenerateTestAction.setTestGenerationService(aiBackendService);
        System.out.println("OpenAI API Key found. Using AI Backend for test generation.");
    }

    public static boolean wasExecuted() {
        return wasRun;
    }

    public static void resetExecutionFlag() {
        wasRun = false;
    }
}
