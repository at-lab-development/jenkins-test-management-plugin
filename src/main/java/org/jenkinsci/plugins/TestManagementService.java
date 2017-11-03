package org.jenkinsci.plugins;

import com.google.gson.Gson;
import hudson.model.AbstractBuild;
import hudson.remoting.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.plugins.entity.Issue;
import org.jenkinsci.plugins.entity.testmanagement.TMTest;
import org.jenkinsci.plugins.util.JiraFormatter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class TestManagementService {

    private final String TM_API_RELATIVE_PATH = "rest/tm/1.0";
    private final String JIRA_API_RELATIVE_PATH = "rest/api/2";
    private final String JIRA_PERMISSIONS_RELATIVE_PATH = JIRA_API_RELATIVE_PATH + "/mypermissions";
    private String username;
    private String password;
    private String baseUrl;
    private CloseableHttpClient client;
    private AbstractBuild<?, ?> build;

    private String getAuthorization() {
        return "Basic ".concat(Base64.encode(username.concat(":").concat(password).getBytes()));
    }

    public TestManagementService(String jiraUrl, String username, String password, AbstractBuild<?, ?> build) {
        this(jiraUrl);
        this.build = build;
        this.username = username;
        this.password = password;
    }

    public TestManagementService(String jiraUrl) {
        this.baseUrl = jiraUrl + (jiraUrl.endsWith("/") ? "" : "/");
        client = HttpClientBuilder.create().build();
    }

    public void updateTestStatus(Issue issue, PrintStream logger) throws IOException {
        String relativeUrl = baseUrl + TM_API_RELATIVE_PATH;

        HttpPut put = new HttpPut(relativeUrl + "/testcase/" + issue.getIssueKey());
        put.addHeader("Authorization", getAuthorization());
        put.setHeader("Content-type", "application/json");
        put.setEntity(new StringEntity("{\"status\": \"" + issue.getStatus() + "\"}"));

        HttpResponse response = client.execute(put);

        int responseCode = response.getStatusLine().getStatusCode();
        if (responseCode == 204)
            logger.println("Issue status updated: " + issue);
        else
            logger.println("Cannot update Test Case status. Response code: " + responseCode
                    + ". Check if issue key is valid");
        put.releaseConnection();
    }

    public void attach(Issue issue, PrintStream logger) throws IOException {
        if (issue.getAttachments() != null && !issue.getAttachments().isEmpty()) {
            String relativeUrl = baseUrl + JIRA_API_RELATIVE_PATH;
            HttpPost post = new HttpPost(relativeUrl + "/issue/" + issue.getIssueKey() + "/attachments");
            post.setHeader(HttpHeaders.AUTHORIZATION, getAuthorization());
            post.setHeader("X-Atlassian-Token", "no-check");
            FileBody fileBody;
            HttpEntity entity;
            HttpResponse response;
            for (String path :
                    issue.getAttachments()) {
                fileBody = new FileBody(new File(build.getProject().getSomeWorkspace() + path));
                entity = MultipartEntityBuilder.create()
                        .addPart("file", fileBody)
                        .build();
                post.setEntity(entity);

                logger.println("Starting execute");
                logger.println(post);

                response = client.execute(post);
                if (response.getStatusLine().getStatusCode() == 200)
                    logger.println("File: \"" + fileBody.getFilename() + "\" has been attached successfully");
                else logger.println(
                        "Something wrong with file " + fileBody.getFilename() + ". Attaching failed. Status code: " +
                                response.getStatusLine().getStatusCode()
                );
                post.releaseConnection();
            }

        }
    }

    public void postBuildInfo(Issue issue, PrintStream logger) throws IOException {
        String commentBody = JiraFormatter.parseIssue(issue, build.number, getTestStatus(issue));
        String relativeUrl = baseUrl + JIRA_API_RELATIVE_PATH;
        HttpPost post = new HttpPost(relativeUrl + "/issue/" + issue.getIssueKey() + "/comment");
        logger.println(relativeUrl + "/issue/" + issue.getIssueKey() + "/comment");
        HttpResponse response;
        post.setHeader(HttpHeaders.AUTHORIZATION, getAuthorization());
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        StringEntity entity = new StringEntity("{ \"body\": " + "\"" +commentBody + "\" }");
        post.setEntity(entity);
        response = client.execute(post);
        if (response.getStatusLine().getStatusCode() == 201)
            logger.println("Comment: \"" + "\" has been successfully posted");
        else if (response.getStatusLine().getStatusCode() == 400)
            logger.println("Comment post failed: missing required fields, invalid values, and so forth");
        else logger.println(
                "Comment post failed. Status code: " + response.getStatusLine().getStatusCode()
        );
        post.releaseConnection();
    }

    public void updateTestStatus(List<Issue> issues, PrintStream logger) throws IOException {
        for (Issue issue : issues) {
            updateTestStatus(issue, logger);
        }
    }

    public int checkConnection() {
        String relativeUrl = baseUrl + JIRA_PERMISSIONS_RELATIVE_PATH;

        try {
            return client.execute(new HttpGet(relativeUrl)).getStatusLine().getStatusCode();
        } catch (IOException e) {
            return 0;
        }
    }

    public String getTestStatus(Issue issue) throws IOException {
        String relativeUrl = baseUrl + TM_API_RELATIVE_PATH;
        HttpGet get = new HttpGet(relativeUrl + "/" +issue.getIssueKey());
        Gson gson = new Gson();
        HttpResponse response = client.execute(get);
        get.releaseConnection();
        TMTest tmTest = gson.fromJson(EntityUtils.toString(response.getEntity()), TMTest.class);
        return tmTest.getStatus();
    }


}
