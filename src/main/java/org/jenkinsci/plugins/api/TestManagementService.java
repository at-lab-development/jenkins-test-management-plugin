package org.jenkinsci.plugins.api;

import org.jenkinsci.plugins.entity.Comment;
import org.jenkinsci.plugins.entity.Issue;
import org.jenkinsci.plugins.util.JiraFormatter;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * TestManagementService is a key class which provides high-level Jira API operations
 * with a focus on plugin purposes. This class is based on JiraService class
 * functionality.
 *
 * @author Uladzimir Pryhazhanau
 * @author Alena Zubrevich
 */
public class TestManagementService {
    private final JiraService jira;
    private final int buildNumber;
    private final PrintStream logger;

    public TestManagementService(String jiraUrl, String username, String password, String workspace, int buildNumber,
                                 PrintStream logger) {
        this.jira = new JiraService(jiraUrl, username, password, workspace, logger);
        this.buildNumber = buildNumber;
        this.logger = logger;
    }

    public TestManagementService(String jiraUrl, String username, String password) {
        this.logger = new PrintStream(System.out);
        this.jira = new JiraService(jiraUrl, username, password, System.getProperty("user.dir"), logger);
        this.buildNumber = 1;
    }

    public static int checkConnection(String jiraUrl, String username, String password) {
        return JiraService.checkConnection(jiraUrl, username, password);
    }

    public void postTestResults(Issue issue, String label) throws IOException {
        String oldStatus = jira.getTestStatus(issue.getIssueKey());
        Map<String, String> filesToJiraLinks = jira.attach(issue.getIssueKey(), issue.getAttachments());

        if (!issue.getStatus().equals(oldStatus))
            jira.updateTestStatus(issue.getIssueKey(), issue.getStatus());

        String commentBody = JiraFormatter.parseIssue(issue, filesToJiraLinks, buildNumber, oldStatus, label);
        jira.addComment(issue.getIssueKey(), commentBody, label);
    }

    /**
     * Removes all report comments before specified date (excluding date passed as parameter)
     *
     * @param issueKey       the issue key for comments removing
     * @param expirationDate the date until which all comments are considered to be expired
     *                       (excluded this specified date)
     * @throws IOException exception throwing in the case of HttpClient problems
     */
    public void removeExpiredComments(String issueKey, Date expirationDate) throws IOException {
        List<Comment> comments = jira.getComments(issueKey);

        if (comments == null) return;

        Pattern attachmentIdPattern = Pattern.compile("(?<=secure/attachment/)\\d+(?=/)");
        Pattern hiddenLabel = Pattern.compile("(?<=\\{anchor:).+(?=}\\{panel)");
        int commentCounter = 0;
        int attachmentCounter = 0;
        for (Comment comment : comments) {
            if (comment.getBody().contains(JiraFormatter.getTitle()) && comment.getCreated().before(expirationDate)) {
                //Remove all attachments
                Matcher attachmentsMatcher = attachmentIdPattern.matcher(comment.getBody());
                while (attachmentsMatcher.find()) {
                    int attachmentId = Integer.valueOf(attachmentsMatcher.group());
                    if (jira.removeAttachment(attachmentId)) attachmentCounter++;
                }

                //Remove label
                Matcher labelMatcher = hiddenLabel.matcher(comment.getBody());
                if (labelMatcher.find()) jira.removeLabel(issueKey, labelMatcher.group());

                //Remove comment
                if (jira.removeComment(issueKey, comment.getId())) commentCounter++;
            }
        }
        // Log results
        if (commentCounter > 0) {
            logger.print(commentCounter + " expired comments ");
            if (attachmentCounter > 0) logger.print("with " + attachmentCounter + " attachments ");
            logger.println("were removed.");
        }
    }
}
