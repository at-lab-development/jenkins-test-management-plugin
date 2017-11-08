package org.jenkinsci.plugins.util;

import org.jenkinsci.plugins.TestManagementService;
import org.jenkinsci.plugins.entity.Comment;
import org.jenkinsci.plugins.entity.Issue;
import org.jenkinsci.plugins.parser.CommentParser;
import org.jenkinsci.plugins.parser.IssueParser;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
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
            for (Issue issue :
                    issues) {
                logger.println("-----REPORTING " + issue.getIssueKey().toUpperCase() + " ISSUE INFO-----");
                service.updateTestStatus(issue, logger);
                service.postBuildInfo(issue, logger);
                service.attach(issue, logger);
                List<Comment> comments = service.getComments(issue);
                for (Comment comment :
                        comments) {
                    int oldCommentsDate = Calendar.getInstance().get(Calendar.MONTH)-3;
                    int commentCreationDate = CommentParser.parseCreationMonth(comment.getCreated());
                    //TODO integrate with jenkins values
                    if (oldCommentsDate <= commentCreationDate &&
                            comment.getBody().contains("Test Management Plugin Auto-generated Report")) {
                        service.deleteComment(issue, comment.getId());
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
