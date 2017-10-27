package org.jenkinsci.plugins;

import hudson.remoting.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.plugins.entity.Issue;

import java.io.IOException;
import java.util.List;

public class TestManagementService {

    private final String TM_API_RELATIVE_PATH = "jira/rest/tm/1.0";
    private final String username;
    private final String password;
    private String baseUrl;
    HttpClient client;

    public String getAuthorization() {
        return "Basic ".concat(Base64.encode(username.concat(":").concat(password).getBytes()));
    }

    public TestManagementService(String jiraUrl, String username, String password) {
        this.username = username;
        this.password = password;
        this.baseUrl = jiraUrl + (jiraUrl.endsWith("/") ? "" : "/") + TM_API_RELATIVE_PATH;
        client = HttpClients.createDefault();
    }

    public void updateTestCaseStatus(Issue issue) throws IOException {
        HttpPut put = new HttpPut(baseUrl + "/testcase/" + issue.getIssueKey());
        put.addHeader("Authorization", getAuthorization());
        put.setHeader("Content-type", "application/json");
        put.setEntity(new StringEntity("{\"status\": \"" + issue.getStatus() + "\"}"));

        HttpResponse response = client.execute(put);

        int responseCode = response.getStatusLine().getStatusCode();
        if (responseCode != 204) {
            throw new RuntimeException("Cannot update Test Case status. Response code: " + responseCode
                    + ". Body: " + EntityUtils.toString(response.getEntity(), "UTF-8"));
        }
    }

    public void updateTestCaseStatus(List<Issue> issues) throws IOException {
        for (Issue issue : issues) {
            updateTestCaseStatus(issue);
        }
    }
}
