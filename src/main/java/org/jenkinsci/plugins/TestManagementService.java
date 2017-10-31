package org.jenkinsci.plugins;

import hudson.remoting.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.plugins.entity.Issue;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class TestManagementService {

    private final String TM_API_RELATIVE_PATH = "rest/tm/1.0";
    private final String JIRA_API_RELATIVE_PATH = "rest/api/2";
    private final String JIRA_PERMISSIONS_RELATIVE_PATH = "rest/api/2/mypermissions";
    private String username;
    private String password;
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

    public TestManagementService(String jiraUrl) {
        this.baseUrl = jiraUrl + (jiraUrl.endsWith("/") ? "" : "/") + TM_API_RELATIVE_PATH;
        client = HttpClients.createDefault();
    }

    public void updateTestCaseStatus(Issue issue, PrintStream logger) throws IOException {
        HttpPut put = new HttpPut(baseUrl + "/testcase/" + issue.getIssueKey());
        put.addHeader("Authorization", getAuthorization());
        put.setHeader("Content-type", "application/json");
        put.setEntity(new StringEntity("{\"status\": \"" + issue.getStatus() + "\"}"));

        HttpResponse response = client.execute(put);

        int responseCode = response.getStatusLine().getStatusCode();
        if (responseCode == 204 )logger.println("Issue status updated: " + issue);
        else {
            logger.println("Cannot update Test Case status. Response code: " + responseCode
                    + ". Check if issue key is valid");
        }

    }

    public void attachFile(Issue issue, File file, PrintStream logger) throws IOException {
        String relativeUrl = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + JIRA_API_RELATIVE_PATH;
        HttpPost post =  new HttpPost(relativeUrl + "/" + issue.getIssueKey());
        FileBody fileBody = new FileBody(file);
        HttpEntity entity = MultipartEntityBuilder.create()
                .addPart("file", fileBody)
                .build();
        post.setHeader(HttpHeaders.AUTHORIZATION, getAuthorization());
        post.setHeader("X-Atlassian-Token", "no-check");
        post.setEntity(entity);
        HttpResponse response = client.execute(post);
        if (response.getStatusLine().getStatusCode() == 200) logger.println("File " + file.getName() + "has been attached successfully");
        else logger.println(
                "Something wrong with file " + file.getName() + ". Attaching failed. Status code: " +
                response.getStatusLine().getStatusCode()
        );
    }



    public void updateTestCaseStatus(List<Issue> issues, PrintStream logger) throws IOException {
        for (Issue issue : issues) {
            updateTestCaseStatus(issue, logger);
        }
    }

    public int checkConnection(String url)  {
        String relativeUrl = url + (url.endsWith("/") ? "" : "/") + JIRA_PERMISSIONS_RELATIVE_PATH;
        HttpGet get = new HttpGet(relativeUrl);
        try {
            return client.execute(get).getStatusLine().getStatusCode();
        } catch (IOException e) {
            return 0;
        }
    }


}
