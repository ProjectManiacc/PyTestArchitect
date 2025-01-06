package com.pytestarchitect;

import com.intellij.openapi.project.Project;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class PluginStartupActivityTest {

    private Project mockProject;

    @Before
    public void setUp() {
        PluginStartupActivity.resetExecutionFlag();
        mockProject = mock(Project.class);
        when(mockProject.getName()).thenReturn("Mock Project");
    }

    @Test
    public void testSystemEnvHasOpenAIKey() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        assertNotNull(
                "The environment variable OPENAI_API_KEY should exist in System.getenv. Ensure it is properly set.",
                apiKey
        );
    }

    @Test
    public void testDummyServiceWhenApiKeyIsMissing() {
        PluginStartupActivity activity = new PluginStartupActivity() {
            @Override
            protected String getApiKey() {
                return null;
            }
        };
        activity.runActivity(mockProject);

        assertTrue(
                "The test generation service should be DummyTestGenerationService when OPENAI_API_KEY is missing.",
                GenerateTestAction.getTestGenerationService() instanceof DummyTestGenerationService
        );
    }

    @Test
    public void testDummyServiceWhenApiKeyIsEmpty() {
        PluginStartupActivity activity = new PluginStartupActivity() {
            @Override
            protected String getApiKey() {
                return "";
            }
        };
        activity.runActivity(mockProject);

        assertTrue(
                "The test generation service should be DummyTestGenerationService when OPENAI_API_KEY is empty.",
                GenerateTestAction.getTestGenerationService() instanceof DummyTestGenerationService
        );
    }

    @Test
    public void testAIBackendServiceWhenApiKeyIsValid() {
        PluginStartupActivity activity = new PluginStartupActivity() {
            @Override
            protected String getApiKey() {
                return "valid_api_key";
            }
        };
        activity.runActivity(mockProject);

        assertTrue(
                "The test generation service should be AIBackendTestGenerationService when a valid OPENAI_API_KEY is provided.",
                GenerateTestAction.getTestGenerationService() instanceof AIBackendTestGenerationService
        );
    }

    @After
    public void tearDown() {
        PluginStartupActivity.resetExecutionFlag();
    }
}
