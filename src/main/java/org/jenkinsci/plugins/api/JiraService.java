package org.jenkinsci.plugins.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hudson.remoting.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
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
import org.jenkinsci.plugins.entity.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * JiraService is a basic class which provides essential Jira REST API operations
 * for plugin functioning. At this moment, this class also provides status get/update
 * methods using Test Management API, but this methods may be moved to separate class
 * with the increase of JIRA REST API functionality afforded by this class.
 *
 * @author Uladzimir Pryhazhanau
 * @author Alena Zubrevich
 */
class JiraService {
    private final static String JIRA_API_RELATIVE_PATH = "rest/api/2";
    private final static String TEST_MANAGEMENT_RELATIVE_PATH = "rest/tm/1.0";
    private final int HTTP_CLIENT_TIMEOUT_SECONDS = 15;

    private final String username;
    private final String password;
    private final String baseUrl;
    private final String jiraApiUrl;
    private final String testManagementApiUrl;
    private final CloseableHttpClient client;
    private final String workspace;
    private final PrintStream logger;

    JiraService(String jiraUrl, String username, String password, String workspace, PrintStream logger) {
        this.username = username;
        this.password = password;
        this.workspace = workspace;
        this.logger = logger;
        this.baseUrl = formBaseUrl(jiraUrl);
        this.jiraApiUrl = baseUrl + JIRA_API_RELATIVE_PATH;
        this.testManagementApiUrl = baseUrl + TEST_MANAGEMENT_RELATIVE_PATH;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(HTTP_CLIENT_TIMEOUT_SECONDS * 1000)
                .setConnectionRequestTimeout(HTTP_CLIENT_TIMEOUT_SECONDS * 1000)
                .setSocketTimeout(HTTP_CLIENT_TIMEOUT_SECONDS * 1000)
                .build();
        client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
    }

    // Auxiliary methods

    private static String formBaseUrl(String rawJiraUrl) {
        return rawJiraUrl + (rawJiraUrl.endsWith("/") ? "" : "/");
    }

    private static String getAuthorization(String username, String password) {
        return "Basic ".concat(Base64.encode(username.concat(":").concat(password).getBytes()));
    }

    private String getAuthorization() {
        return getAuthorization(username, password);
    }

    private String addResponseInfo(HttpResponse response) {
        StatusLine statusLine = response.getStatusLine();
        return "Response code: " + statusLine.getStatusCode() + ". Reason: " + statusLine.getReasonPhrase();
    }

    // Test Management methods

    String getTestStatus(String issueKey) throws IOException {
        HttpGet get = new HttpGet(testManagementApiUrl + "/testcase/" + issueKey);
        get.setHeader(HttpHeaders.AUTHORIZATION, getAuthorization());
        HttpResponse response = client.execute(get);
        get.releaseConnection();

        int responseCode = response.getStatusLine().getStatusCode();
        if (responseCode != 200) {
            logger.println("Cannot get Test status. " + addResponseInfo(response));
            return null;
        }

        return new Gson().fromJson(EntityUtils.toString(response.getEntity()), Test.class).getStatus();
    }

    void updateTestStatus(String issueKey, String status) throws IOException {
        HttpPut put = new HttpPut(testManagementApiUrl + "/testcase/" + issueKey);
        put.setHeader(HttpHeaders.AUTHORIZATION, getAuthorization());
        put.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        put.setEntity(new StringEntity("{\"status\": \"" + status + "\"}"));

        HttpResponse response = client.execute(put);

        int responseCode = response.getStatusLine().getStatusCode();
        logger.println(responseCode == 204
                ? "Issue " + issueKey + " status updated: " + status
                : "Cannot update " + issueKey + " status. " + addResponseInfo(response));
        put.releaseConnection();
    }

    // JIRA methods

    static int checkConnection(String jiraUrl, String user, String password) {
        String relativeUrl = formBaseUrl(jiraUrl) + JIRA_API_RELATIVE_PATH + "/myself";
        try {
            HttpGet get = new HttpGet(relativeUrl);
            get.setHeader(HttpHeaders.AUTHORIZATION, getAuthorization(user, password));
            return HttpClientBuilder.create().build().execute(get).getStatusLine().getStatusCode();
        } catch (IOException e) {
            return 0;
        }
    }

    private void manageLabel(String issueKey, String label, String action) throws IOException {
        HttpPut put = new HttpPut(jiraApiUrl + "/issue/" + issueKey);
        put.setHeader(HttpHeaders.AUTHORIZATION, getAuthorization());
        put.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        put.setEntity(new StringEntity("{\"update\": { \"labels\": [ {\"" + action + "\": \"" + label + "\"} ] } }"));

        HttpResponse response = client.execute(put);

        int responseCode = response.getStatusLine().getStatusCode();
        if (responseCode == 204 && action.equals("add"))
            logger.println("Add label \"" + label + "\" to issue " + issueKey);
        put.releaseConnection();
    }

    void addLabel(String issueKey, String label) throws IOException {
        manageLabel(issueKey, label, "add");
    }

    void removeLabel(String issueKey, String label) throws IOException {
        manageLabel(issueKey, label, "remove");
    }

    Map<String, String> attach(String issueKey, List<String> attachments) throws IOException {
        if (attachments == null || attachments.isEmpty()) return null;

        Map<String, String> fileToJiraLinkMapping = new HashMap<>();
        Gson gson = new Gson();
        HttpPost post = new HttpPost(jiraApiUrl + "/issue/" + issueKey + "/attachments");
        post.setHeader(HttpHeaders.AUTHORIZATION, getAuthorization());
        post.setHeader("X-Atlassian-Token", "no-check");

        for (String path : attachments) {
            FileBody fileBody = new FileBody(new File(workspace + path));
            HttpEntity entity = MultipartEntityBuilder.create().addPart("file", fileBody).build();
            post.setEntity(entity);

            logger.println("Start execute: " + post);

            HttpResponse response = client.execute(post);
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode == 200) {
                // We have to save all links to files for further comment formatting
                logger.println("File: \"" + fileBody.getFilename() + "\" has been attached successfully.");
                Attachment fileInfo = gson.fromJson(EntityUtils.toString(response.getEntity()), Attachment[].class)[0];
                String link = baseUrl + "secure/attachment/" + fileInfo.getId() + "/" + fileInfo.getFilename();
                fileToJiraLinkMapping.put(path, link);
            } else {
                logger.println("Cannot attach file: '" + fileBody.getFilename() + "'. " + addResponseInfo(response));
            }
            post.releaseConnection();
        }
        return fileToJiraLinkMapping;
    }

    void addComment(String issueKey, String commentBody, String label) throws IOException {
        HttpPost post = new HttpPost(jiraApiUrl + "/issue/" + issueKey + "/comment");
        post.setHeader(HttpHeaders.AUTHORIZATION, getAuthorization());
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        StringEntity entity = new StringEntity("{ \"body\": " + "\"" + commentBody + "\" }");
        post.setEntity(entity);

        HttpResponse response = client.execute(post);
        int responseCode = response.getStatusLine().getStatusCode();
        if (responseCode == 201) {
            logger.println("Test execution results for issue " + issueKey + " were successfully attached as comment.\n"
                    + "Issue link: " + jiraApiUrl + "/issue/" + issueKey);
            if (label != null) addLabel(issueKey, label);
        } else {
            logger.println("Cannot attach test results. " + addResponseInfo(response));
        }
        post.releaseConnection();
    }

    List<Comment> getComments(String issueKey) throws IOException {
        HttpGet get = new HttpGet(jiraApiUrl + "/issue/" + issueKey + "/comment");
        get.setHeader(HttpHeaders.AUTHORIZATION, getAuthorization());
        HttpResponse response = client.execute(get);
        get.releaseConnection();

        int responseCode = response.getStatusLine().getStatusCode();
        if (responseCode == 200) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(EntityUtils.toString(response.getEntity()), JsonObject.class);
            int total = gson.fromJson(jsonObject.get("total"), int.class);
            if (total > 0)
                return Arrays.asList(gson.fromJson(jsonObject.get("comments"), Comment[].class));
        } else {
            logger.println("Cannot get comments for issue " + issueKey + ". " + addResponseInfo(response));
        }
        return null;
    }

    private boolean removeResource(String urlPart) throws IOException {
        HttpDelete delete = new HttpDelete(jiraApiUrl + urlPart);
        delete.setHeader(HttpHeaders.AUTHORIZATION, getAuthorization());
        HttpResponse response = client.execute(delete);
        delete.releaseConnection();
        return response.getStatusLine().getStatusCode() == 204;
    }

    boolean removeComment(String issueKey, int commentId) throws IOException {
        return removeResource("/issue/" + issueKey + "/comment/" + commentId);
    }

    boolean removeAttachment(int attachmentId) throws IOException {
        return removeResource("/attachment/" + attachmentId);
    }
}
