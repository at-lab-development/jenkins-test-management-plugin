package com.jira.tm.model;

public class TestRun {
    // Return as POST response
    private int testRunId;

    // Mandatory fields for POST
    private String projectKey;
    private String testRunSummary;

    public TestRun() {
    }

    public TestRun(String projectKey, String testRunSummary) {
        this.projectKey = projectKey;
        this.testRunSummary = testRunSummary;
    }

    public int getTestRunId() {
        return testRunId;
    }

    public void setTestRunId(int testRunId) {
        this.testRunId = testRunId;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getTestRunSummary() {
        return testRunSummary;
    }

    public void setTestRunSummary(String testRunSummary) {
        this.testRunSummary = testRunSummary;
    }
}
