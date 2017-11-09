package org.jenkinsci.plugins.util;

import org.jenkinsci.plugins.TestManagementService;
import org.jenkinsci.plugins.entity.Comment;
import org.jenkinsci.plugins.entity.Issue;
import org.jenkinsci.plugins.parser.IssueParser;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IssuesExecutor {
    private TestManagementService service;
    private PrintStream logger;

    public IssuesExecutor(TestManagementService service, PrintStream stream) {
        this.service = service;
        this.logger = stream;
    }

    public void execute(List<Issue> issues, String deleteCriteria, String dateCriteria) {
        try {
            for (Issue issue : issues) {
                logger.println("-----REPORTING " + issue.getIssueKey().toUpperCase() + " ISSUE INFO-----");
                service.postTestResults(issue, logger);

                List<Comment> comments = service.getComments(issue.getIssueKey());
                Calendar calendar = Calendar.getInstance();
                if (dateCriteria != null && deleteCriteria != null && comments != null) {
                    calendar.add(Integer.parseInt(dateCriteria), -Integer.parseInt(deleteCriteria));
                    Date expirationDate = calendar.getTime();
                    for (Comment comment : comments) {
                        if (comment.getCreated().before(expirationDate)) {

                            if (service.removeComment(issue.getIssueKey(), comment.getId()))
                                logger.println("Old report has been successfully deleted." +
                                        "Created in " + comment.getCreated() +
                                        ". Expired date is " + expirationDate);

                            //Remove all attachments
                            Pattern orderReferencePattern = Pattern.compile("(?<=secure/attachment/)\\d+(?=/)");
                            Matcher matcher = orderReferencePattern.matcher(comment.getBody());
                            while (matcher.find()) {
                                int attachmentId = Integer.valueOf(matcher.group());
                                if (service.removeAttachment(attachmentId)) {
                                    logger.println("Attachment has been removed successfully");
                                }
                            }

                        }
                    }
                }
                logger.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void execute(File file, String deleteCriteria, String dateCriteria) {
        IssueParser parser = new IssueParser();
        execute(parser.getIssues(file), deleteCriteria, dateCriteria);
    }


}
