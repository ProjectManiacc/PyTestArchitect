package com.pytestarchitect;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AIBackendTestGenerationServiceTest {

    @Test
    public void testGeneratesTestsUsesAIClient(){
        AIClient mockClient = new AIClient() {
            @Override
            public String generateTestsFromCode(String sourceCode) {
                return "def test_generated_by_ai():\n    assert foo() == 42\n";
            }

        };

        TestGenerationService service = new AIBackendTestGenerationService(mockClient);

        String code = "def foo():\n    return 42\n";
        String generatedTests = service.generateTests(code);

        assertNotNull(generatedTests);
        assertTrue(generatedTests.contains("def test_generated_by_ai():"));
    }


}
