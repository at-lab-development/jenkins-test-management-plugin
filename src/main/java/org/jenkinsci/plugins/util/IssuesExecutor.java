package org.jenkinsci.plugins.util;

import org.jenkinsci.plugins.api.TestManagementService;
import org.jenkinsci.plugins.entity.Issue;
import org.jenkinsci.plugins.entity.Issues;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * IssuesExecutor is a basic class responsible for issues parsing, execution
 * and publishing.
 *
 * @author Uladzimir Pryhazhanau
 */
public class IssuesExecutor {
    private final TestManagementService service;
    private final PrintStream logger;

    public IssuesExecutor(TestManagementService service, PrintStream logger) {
        this.service = service;
        this.logger = logger;
    }

    private List<Issue> parse(File xmlFile) {
        List<Issue> issues = null;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Issues.class);
            Unmarshaller u = jaxbContext.createUnmarshaller();
            issues = ((Issues) u.unmarshal(xmlFile)).getIssues();
        } catch (JAXBException e) {
            logger.println("Cannot read file: " + xmlFile.getPath() + ". Reason: " + e.getMessage());
        }
        return issues;
    }

    public void execute(List<Issue> issues, String deleteCriteria, String dateCriteria, String label) {
        Date expirationDate = null;
        if (dateCriteria != null && deleteCriteria != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Integer.valueOf(dateCriteria), -Integer.parseInt(deleteCriteria));
            expirationDate = calendar.getTime();
        }

        try {
            for (Issue issue : issues) {
                logger.println("-----REPORTING " + issue.getIssueKey().toUpperCase() + " ISSUE INFO-----");
                service.postTestResults(issue, label);
                if (expirationDate != null) service.removeExpiredComments(issue.getIssueKey(), expirationDate);
                logger.println();
            }
        } catch (IOException e) {
            logger.println("Cannot update issues. Error message: " + e.getMessage());
        }
    }

    public void execute(File file, String deleteCriteria, String dateCriteria, String label) {
        execute(parse(file), deleteCriteria, dateCriteria, label);
    }
}
