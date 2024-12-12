package com.pytestarchitect;

public class AIBackendTestGenerationService implements TestGenerationService {
    private final AIClient client;

    public AIBackendTestGenerationService(AIClient client) {
        this.client = client;
    }

    @Override
    public String generateTests(String sourceCode){
        return client.generateTests(sourceCode);
    }
}
