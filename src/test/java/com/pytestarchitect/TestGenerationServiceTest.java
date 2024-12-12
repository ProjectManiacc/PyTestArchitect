package com.pytestarchitect;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestGenerationServiceTest {

    @Test
    public void testGenerateTest() {
        TestGenerationService service = new TestGenerationService();
        String code = "def foo():\n    return 42";
        String tests = service.generateTests(code);

        assertNotNull(tests);
        assertTrue(tests.contains("def test_foo():"));
    }

}
