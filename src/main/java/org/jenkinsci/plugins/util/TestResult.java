package org.jenkinsci.plugins.util;

public enum TestResult {

    FAILED("Failed", JiraFormatter.Color.RED),
    PASSED("Passed", JiraFormatter.Color.GREEN),
    BLOCKED("Blocked", JiraFormatter.Color.GRAY),
    UNTESTED("Untested", JiraFormatter.Color.GRAY);

    private String text;
    private JiraFormatter.Color color;

    TestResult(final String text, JiraFormatter.Color color) {
        this.text = text;
        this.color = color;
    }

    public JiraFormatter.Color getColor() {
        return color;
    }

    public static JiraFormatter.Color getColor(String value) {
        return valueOf(TestResult.class, value.toUpperCase()).getColor();
    }

    @Override
    public String toString() {
        return text;
    }
}
