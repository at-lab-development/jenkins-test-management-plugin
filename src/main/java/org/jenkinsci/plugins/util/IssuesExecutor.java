package org.jenkinsci.plugins.util;

import org.jenkinsci.plugins.entity.Issue;
import org.jenkinsci.plugins.entity.Issues;
import org.jenkinsci.plugins.parser.IssueParser;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
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
                service.updateTestCaseStatus(issue, logger);
                service.postComments(issue, logger);
                service.attach(issue, logger);
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
