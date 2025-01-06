package com.pytestarchitect;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AIBackendTestGenerationServiceTest {

    private AIClient mockClient;
    private AIBackendTestGenerationService testGenerationService;

    @Before
    public void setUp() {
        mockClient = mock(AIClient.class);
        testGenerationService = new AIBackendTestGenerationService(mockClient);
    }

    @Test
    public void testGenerateTestsSuccess() {
        String sourceCode = "def foo(): return 42";
        String expectedTestCode = "def test_foo():\n    assert foo() == 42\n";
        when(mockClient.generateTests(sourceCode)).thenReturn(expectedTestCode);

        String generatedTests = testGenerationService.generateTests(sourceCode);

        assertNotNull("Generated tests should not be null", generatedTests);
        assertEquals("Generated tests should match the expected output", expectedTestCode, generatedTests);
        verify(mockClient).generateTests(sourceCode);
    }

    @Test(expected = RuntimeException.class)
    public void testGenerateTestsEmptyResponse() {
        String sourceCode = "def foo(): return 42";
        when(mockClient.generateTests(sourceCode)).thenReturn("");

        testGenerationService.generateTests(sourceCode);
    }

    @Test(expected = RuntimeException.class)
    public void testGenerateTestsNullResponse() {
        String sourceCode = "def foo(): return 42";
        when(mockClient.generateTests(sourceCode)).thenReturn(null);

        testGenerationService.generateTests(sourceCode);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateTestsInvalidRequest() {
        String sourceCode = null;
        doThrow(new IllegalArgumentException("Invalid source code"))
                .when(mockClient)
                .generateTests(sourceCode);

        testGenerationService.generateTests(sourceCode);
    }

    @Test(expected = RuntimeException.class)
    public void testGenerateTestsClientException() {
        String sourceCode = "def foo(): return 42";
        doThrow(new RuntimeException("Client error"))
                .when(mockClient)
                .generateTests(sourceCode);

        testGenerationService.generateTests(sourceCode);
    }
}
