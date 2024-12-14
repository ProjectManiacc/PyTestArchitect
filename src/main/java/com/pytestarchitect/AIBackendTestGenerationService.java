package com.pytestarchitect;

import java.util.logging.Logger;

public class AIBackendTestGenerationService implements TestGenerationService {
    private final AIClient client;
    private static final Logger logger = Logger.getLogger(AIBackendTestGenerationService.class.getName());

    public AIBackendTestGenerationService(AIClient client) {
        this.client = client;
    }

    @Override
    public String generateTests(String sourceCode){
        try {
            String testCode = client.generateTests(sourceCode);

            if (testCode == null || testCode.isEmpty()) {
                logger.warning("Failed to generate tests");
                return null;
            }
            return testCode;
        } catch (Exception e) {
            logger.severe("Failed to generate tests: " + e.getMessage());
            e.printStackTrace();

        }
    }
}
