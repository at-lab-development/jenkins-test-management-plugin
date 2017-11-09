package org.jenkinsci.plugins;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import hudson.model.AbstractBuild;
import hudson.remoting.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.plugins.entity.Attachment;
import org.jenkinsci.plugins.entity.Comment;
import org.jenkinsci.plugins.entity.Issue;
import org.jenkinsci.plugins.entity.testmanagement.TMTest;
import org.jenkinsci.plugins.util.JiraFormatter;
import org.jenkinsci.plugins.util.LabelAction;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestManagementService {

    private final String TM_API_RELATIVE_PATH = "rest/tm/1.0";
    private final String JIRA_API_RELATIVE_PATH = "rest/api/2";
    private final String ATTACHMENT_URL = "secure/attachment/%s/%s";
    private String username;
    private String password;
    private String baseUrl;
    private CloseableHttpClient client;
    //private AbstractBuild<?, ?> build;
    private int buildNumber;
    private String workspace;

    private String getAuthorization() {
        return "Basic ".concat(Base64.encode(username.concat(":").concat(password).getBytes()));
    }

    public TestManagementService(String jiraUrl, String username, String password, AbstractBuild<?, ?> build) {
        this(jiraUrl);
        this.username = username;
        this.password = password;
        this.buildNumber = build.number;
        this.workspace = build.getProject().getSomeWorkspace().getRemote();
    }

    public TestManagementService(String jiraUrl, String username, String password) {
        this(jiraUrl);
        this.username = username;
        this.password = password;
        this.workspace = System.getProperty("user.dir");
        this.buildNumber = 1;
    }

    public TestManagementService(String jiraUrl) {
        this.baseUrl = jiraUrl + (jiraUrl.endsWith("/") ? "" : "/");
        client = HttpClientBuilder.create().build();
    }

    private void updateTestStatus(Issue issue, PrintStream logger) throws IOException {
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

    public void manageLabel(String issueKey, String label, LabelAction action, PrintStream logger) throws IOException {
        String relativeUrl = baseUrl + JIRA_API_RELATIVE_PATH;

        HttpPut put = new HttpPut(relativeUrl + "/issue/" + issueKey );
        put.addHeader("Authorization", getAuthorization());
        put.setHeader("Content-type", "application/json");
        put.setEntity(new StringEntity("{\"update\": { \"labels\": [ {\"" + action + "\": \"" + label + "\"} ] } }"));

        HttpResponse response = client.execute(put);

        int responseCode = response.getStatusLine().getStatusCode();
        switch (responseCode) {
            case 204:
                logger.println("Successfully " + action + " label \"" + label + " " + action.getPreposition()
                        + " issue " + issueKey);
                break;
            default:
                logger.println("Cannot " + action + " label \"" + label + "\" " + action.getPreposition()
                        + " issue " + issueKey + ". Response code: " + responseCode + ". Reason: "
                        + response.getStatusLine().getReasonPhrase());
        }

        put.releaseConnection();
    }


    private Map<String, String> attach(Issue issue, PrintStream logger) throws IOException {
        if (issue.getAttachments() == null || issue.getAttachments().isEmpty())
            return null;

        Map<String, String> fileToJiraLinkMapping = new HashMap<>();
        String relativeUrl = baseUrl + JIRA_API_RELATIVE_PATH;
        HttpPost post = new HttpPost(relativeUrl + "/issue/" + issue.getIssueKey() + "/attachments");
        post.setHeader(HttpHeaders.AUTHORIZATION, getAuthorization());
        post.setHeader("X-Atlassian-Token", "no-check");

        for (String path : issue.getAttachments()) {
            FileBody fileBody = new FileBody(new File(workspace + path));
            HttpEntity entity = MultipartEntityBuilder.create()
                    .addPart("file", fileBody)
                    .build();
            post.setEntity(entity);

            logger.println("Start execute: " + post);

            HttpResponse response = client.execute(post);
            int responseCode = response.getStatusLine().getStatusCode();
            switch (responseCode) {
                case 200:
                    logger.println("File: \"" + fileBody.getFilename() + "\" has been attached successfully.");
                    Attachment[] attachments = new Gson().fromJson(EntityUtils.toString(response.getEntity()),
                            Attachment[].class);
                    if (attachments.length > 0) {
                        Attachment attachment = attachments[0];
                        String link = baseUrl + String.format(ATTACHMENT_URL, attachment.getId(), attachment.getFilename());
                        fileToJiraLinkMapping.put(path, link);
                    }
                    break;
                case 413:
                    logger.println("File: \"" + fileBody.getFilename() + "\" is too big.");
                    break;
                case 403:
                    logger.println("Attachments is disabled or if you don't have permission to add attachments to " +
                            "this issue.");
                    break;
                default:
                    logger.println("Cannot attach file: \"" + fileBody.getFilename() + "\". Status code: "
                            + responseCode);
            }

            post.releaseConnection();
        }
        return fileToJiraLinkMapping;
    }

    public void postTestResults(Issue issue, PrintStream logger) throws IOException {
        updateTestStatus(issue, logger);
        Map<String, String> filesToJiraLinks = attach(issue, logger);
        String commentBody = JiraFormatter.parseIssue(issue, filesToJiraLinks, buildNumber, getTestStatus(issue.getIssueKey()));
        String relativeUrl = baseUrl + JIRA_API_RELATIVE_PATH;
        HttpPost post = new HttpPost(relativeUrl + "/issue/" + issue.getIssueKey() + "/comment");
        post.setHeader(HttpHeaders.AUTHORIZATION, getAuthorization());
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        StringEntity entity = new StringEntity("{ \"body\": " + "\"" + commentBody + "\" }");
        post.setEntity(entity);

        HttpResponse response = client.execute(post);
        int responseCode = response.getStatusLine().getStatusCode();
        switch (responseCode) {
            case 201:
                logger.println("Test execution results for issue " + issue.getIssueKey() + " were successfully " +
                        "attached as comment.");
                break;
            case 400:
                logger.println("Cannot attach test results: input is invalid (e.g. missing required fields, invalid " +
                        "values, and so forth). Request body: " + EntityUtils.toString(post.getEntity()));
                break;
            default:
                logger.println("Cannot attach test results. Status code: " + responseCode);
        }
        post.releaseConnection();
    }

    int checkConnection() {
        String relativeUrl = baseUrl + JIRA_API_RELATIVE_PATH + "/mypermissions";

        try {
            return client.execute(new HttpGet(relativeUrl)).getStatusLine().getStatusCode();
        } catch (IOException e) {
            return 0;
        }
    }

    private String getTestStatus(String issueKey) throws IOException {
        String status = null;
        String relativeUrl = baseUrl + TM_API_RELATIVE_PATH;
        HttpGet get = new HttpGet(relativeUrl + "/testcase/" + issueKey);
        get.setHeader(HttpHeaders.AUTHORIZATION, getAuthorization());
        String entityBody = EntityUtils.toString(client.execute(get).getEntity());
        Gson gson = new Gson();
        try {
            TMTest tmTest = gson.fromJson(entityBody, TMTest.class);
            status = tmTest.getStatus();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        get.releaseConnection();
        return status;
    }

    public List<Comment> getComments(String issueKey) throws IOException {
        String relativePath = baseUrl + JIRA_API_RELATIVE_PATH;
        HttpGet get = new HttpGet(relativePath + "/issue/" + issueKey + "/comment");
        get.setHeader(HttpHeaders.AUTHORIZATION, getAuthorization());
        HttpResponse response = client.execute(get);
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(EntityUtils.toString(response.getEntity()), JsonObject.class);
        get.releaseConnection();
        return Arrays.asList(gson.fromJson(jsonObject.get("comments"), Comment[].class));//TODO NullPointer EX
    }

    public boolean deleteComment(String issueKey, int commentId) throws IOException {
        String relativeUrl = baseUrl + JIRA_API_RELATIVE_PATH;
        HttpDelete delete = new HttpDelete(relativeUrl + "/issue/" + issueKey + "/comment/" + commentId);
        delete.setHeader(HttpHeaders.AUTHORIZATION, getAuthorization());
        HttpResponse response = client.execute(delete);
        return response.getStatusLine().getStatusCode() == 204;
    }


}
