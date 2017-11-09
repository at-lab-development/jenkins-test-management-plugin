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

public class IssuesExecutor {
    private TestManagementService service;
    private PrintStream logger;
    private List<Issue> issues;

    public IssuesExecutor(TestManagementService service, PrintStream stream) {
        this.service = service;
        this.logger = stream;
    }

    public void execute(List<Issue> issues) {
        try {
            for (Issue issue : issues) {
                logger.println("-----REPORTING " + issue.getIssueKey().toUpperCase() + " ISSUE INFO-----");
                service.postTestResults(issue, logger);

                List<Comment> comments = service.getComments(issue.getIssueKey());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.DATE, -3);  //todo synch with jenkins settings
                Date expirationDate = calendar.getTime();
                for (Comment comment : comments) {
                    if (comment.getCreated().before(expirationDate)) {
                        service.deleteComment(issue.getIssueKey(), comment.getId());
                    }
                }
                logger.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void execute(File file) {
        IssueParser parser = new IssueParser();
        issues = parser.getIssues(file);
        execute(issues);
    }


}
