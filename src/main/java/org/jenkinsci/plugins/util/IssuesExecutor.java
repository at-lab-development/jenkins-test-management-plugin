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

    public IssuesExecutor(TestManagementService service, PrintStream stream) {
        this.service = service;
        this.logger = stream;
    }

    public void execute(List<Issue> issues, String deleteCriteria, String dateCriteria, boolean addLabel) {
        Date expirationDate = null;
        if (dateCriteria != null && deleteCriteria != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Integer.valueOf(dateCriteria), -Integer.parseInt(deleteCriteria));
            expirationDate = calendar.getTime();
        }

        try {
            for (Issue issue : issues) {
                logger.println("-----REPORTING " + issue.getIssueKey().toUpperCase() + " ISSUE INFO-----");
                service.postTestResults(issue, addLabel);

                List<Comment> comments = service.getComments(issue.getIssueKey());
                if (comments != null && expirationDate != null) {
                    service.removeExpiredComments(issue.getIssueKey(), expirationDate);
                }
                logger.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void execute(File file, String deleteCriteria, String dateCriteria, boolean addLabel) {
        IssueParser parser = new IssueParser();
        execute(parser.getIssues(file), deleteCriteria, dateCriteria, addLabel);
    }


}
