package com.jira.tm.model;

public class TestCase {
    // Return as POST response
    private String testCaseKey;
    private int testCaseId;

    // Mandatory fields for POST
    private String projectKey;
    private int testRunId;
    private String originalTestCaseKey;

    // Optional fields for POST
    private boolean preserveFolderStructure; //"true"
    private String executionStatus; // or status ? ERR
    private String executionMessage;

    public TestCase() {
    }

    public TestCase(String projectKey, int testRunId, String originalTestCaseKey) {
        this.projectKey = projectKey;
        this.testRunId = testRunId;
        this.originalTestCaseKey = originalTestCaseKey;
    }

    public String getTestCaseKey() {
        return testCaseKey;
    }

    public void setTestCaseKey(String testCaseKey) {
        this.testCaseKey = testCaseKey;
    }

    public int getTestCaseId() {
        return testCaseId;
    }

    public void setTestCaseId(int testCaseId) {
        this.testCaseId = testCaseId;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public int getTestRunId() {
        return testRunId;
    }

    public void setTestRunId(int testRunId) {
        this.testRunId = testRunId;
    }

    public String getOriginalTestCaseKey() {
        return originalTestCaseKey;
    }

    public void setOriginalTestCaseKey(String originalTestCaseKey) {
        this.originalTestCaseKey = originalTestCaseKey;
    }

    public boolean isPreserveFolderStructure() {
        return preserveFolderStructure;
    }

    public void setPreserveFolderStructure(boolean preserveFolderStructure) {
        this.preserveFolderStructure = preserveFolderStructure;
    }

    public String getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(String executionStatus) {
        this.executionStatus = executionStatus;
    }

    public String getExecutionMessage() {
        return executionMessage;
    }

    public void setExecutionMessage(String executionMessage) {
        this.executionMessage = executionMessage;
    }
}
