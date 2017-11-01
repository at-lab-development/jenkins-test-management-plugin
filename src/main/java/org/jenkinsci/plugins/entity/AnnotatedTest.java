package org.jenkinsci.plugins.entity;

import java.util.Map;

/**
 * This class used for Java source code parsing. The main goal is storing
 * basic test method information such as method name, @Test annotation parameters
 * and its corresponding JIRA Test Case key (for instance: EPMFARMATS-123).
 */
public class AnnotatedTest {

    private String methodName;
    private String jiraTestKey;
    private Map<String, String> testAnnotationParameters;

    public AnnotatedTest(String methodName, String jiraTestKey, Map<String, String> testAnnotationParameters) {
        this.methodName = methodName;
        this.jiraTestKey = jiraTestKey;
        this.testAnnotationParameters = testAnnotationParameters;
    }

    public String getJiraTestKey() {
        return jiraTestKey;
    }

    public void setJiraTestKey(String jiraTestKey) {
        this.jiraTestKey = jiraTestKey;
    }

    public Map<String, String> getTestAnnotationParameters() {
        return testAnnotationParameters;
    }

    public void setTestAnnotationParameters(Map<String, String> testAnnotationParameters) {
        this.testAnnotationParameters = testAnnotationParameters;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public String toString() {
        return "{ Method: " + methodName + ", JIRATestKey: " + jiraTestKey + ", @Test params:" + testAnnotationParameters.toString() + " }";
    }
}
