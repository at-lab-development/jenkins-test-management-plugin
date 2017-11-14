package org.jenkinsci.plugins.api;

import org.jenkinsci.plugins.entity.Comment;
import org.jenkinsci.plugins.entity.Issue;
import org.jenkinsci.plugins.util.JiraFormatter;
import org.jenkinsci.plugins.util.LabelAction;

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
 * @author      Uladzimir Pryhazhanau
 * @author      Alena Zubrevich
 */
public class TestManagementService {
    private final JiraService jira;
    private int buildNumber;
    private PrintStream logger;

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

    public void postTestResults(Issue issue, boolean addLabel) throws IOException {
        String oldStatus = jira.getTestStatus(issue.getIssueKey());
        Map<String, String> filesToJiraLinks = jira.attach(issue.getIssueKey(), issue.getAttachments());
        jira.updateTestStatus(issue.getIssueKey(), issue.getStatus());
        String commentBody = JiraFormatter.parseIssue(issue, filesToJiraLinks, buildNumber, oldStatus);
        jira.addComment(issue.getIssueKey(), commentBody, addLabel);
    }

    public void removeExpiredComments(String issueKey, Date expirationDate) throws IOException {
        List<Comment> comments = jira.getComments(issueKey);

        if (comments == null) return;

        Pattern attachmentPattern = Pattern.compile("(?<=secure/attachment/)\\d+(?=/)");
        int commentCounter = 0;
        int attachmentCounter = 0;
        for (Comment comment : comments) {
            if (comment.getBody().contains(JiraFormatter.getTitle()) && comment.getCreated().before(expirationDate)) {
                //Remove all attachments
                Matcher matcher = attachmentPattern.matcher(comment.getBody());
                while (matcher.find()) {
                    int attachmentId = Integer.valueOf(matcher.group());
                    if (jira.removeAttachment(attachmentId)) attachmentCounter++;
                }

                //Remove label
                jira.manageLabel(issueKey, jira.getLabelForDate(comment.getCreated()), LabelAction.REMOVE);

                //Remove comment
                if (jira.removeComment(issueKey, comment.getId())) commentCounter++;
            }
        }
        if (commentCounter > 0) {
            logger.print(commentCounter + " expired comments ");
            if (attachmentCounter > 0) logger.print("with " + attachmentCounter + " attachments ");
            logger.println("were removed.");
        }
    }
}
