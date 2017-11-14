package org.jenkinsci.plugins.util;

public enum TestResult {

    FAILED("Failed", "red"),
    PASSED("Passed", "green"),
    BLOCKED("Blocked", "gray"),
    UNTESTED("Untested", "gray");

    private final String text;
    private final String color;

    TestResult(final String text, String color) {
        this.text = text;
        this.color = color;
    }

    private String getColor() {
        return color;
    }

    public static String getColor(String value) {
        return valueOf(TestResult.class, value.toUpperCase()).getColor();
    }

    @Override
    public String toString() {
        return text;
    }
}
