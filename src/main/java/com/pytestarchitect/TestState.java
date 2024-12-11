package com.pytestarchitect;

public class TestState {
    private static String lastExtractedCode;

    public static void setLastExtractedCode(String code) {
        lastExtractedCode = code;
    }

    public static String getLastExtractedCode() {
        return lastExtractedCode;
    }
}
