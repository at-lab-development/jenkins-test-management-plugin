package org.jenkinsci.plugins;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.jira.tm.model.TestCase;
import com.jira.tm.model.TestRun;

import static com.jayway.restassured.RestAssured.given;

public class RESTClient {
    private String username;
    private String password;
    private String jiraUrl;
    private final String TM_API_RELATIVE_PATH = "/jira/rest/tm/1.0";
    private RequestSpecification requestSpecification;

    public RESTClient(String jiraUrl, String username, String password) {
        this.jiraUrl = jiraUrl;
        this.username = username;
        this.password = password;
        requestSpecification = new RequestSpecBuilder().setBaseUri(jiraUrl + TM_API_RELATIVE_PATH).build();
    }

    private RequestSpecification getGivenAuth() {
        return given().spec(requestSpecification).auth().basic(username, password);
    }

    public int createTestRun(String projectKey, String testRunSummary) {
        return createTestRun(new TestRun(projectKey, testRunSummary));
    }

    public int createTestRun(TestRun testRun) {
        Response response  = getGivenAuth().body(testRun).with().contentType("application/json").post().andReturn();
        //todo check status code 201 (or 403,412,500 -> errorMessage)
        return response.as(TestRun.class).getTestRunId();
    }

    public TestCase createTestCase(String projectKey, int testRunId, String originalTestCaseKey) {
        return createTestCase(new TestCase(projectKey, testRunId, originalTestCaseKey));
    }

    public TestCase createTestCase(TestCase testCase) {
        Response response  = getGivenAuth().body(testCase).with().contentType("application/json").post().andReturn();
        //todo check status code 201 (or 403,412 -> errorMessage)
        return response.as(TestCase.class);
    }
}
