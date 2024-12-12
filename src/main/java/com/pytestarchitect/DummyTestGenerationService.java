package com.pytestarchitect;

public class DummyTestGenerationService implements TestGenerationService {
    @Override
    public String generateTests(String code) {
        return "def test_foo():\n    assert foo() == 42\n";
    }
}
