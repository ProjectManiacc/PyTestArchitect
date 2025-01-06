package com.pytestarchitect;

public class TestState {
    private static String lastExtractedCode;
    private static String lastGeneratedTests;
    private static String lastNotification;

    public static void setLastExtractedCode(String code) {
        lastExtractedCode = code;
    }

    public static String getLastExtractedCode() {
        return lastExtractedCode;
    }

    public static void setLastGeneratedTests(String tests) {
        lastGeneratedTests = tests;
    }

    public static String getLastGeneratedTests() {
        return lastGeneratedTests;
    }

    public static void setLastNotification(String notification) {
        lastNotification = notification;
    }

    public static String getLastNotification() {
        return lastNotification;
    }
}
