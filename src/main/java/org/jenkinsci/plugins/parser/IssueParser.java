package org.jenkinsci.plugins.parser;

import org.jenkinsci.plugins.entity.Issue;
import org.jenkinsci.plugins.entity.Issues;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.List;

public class IssueParser {
    private List<Issue> issues;

    public List<Issue> getIssues(File xmlFile) {
        try {
        JAXBContext jaxbContext = JAXBContext.newInstance(Issues.class);
        Unmarshaller u = jaxbContext.createUnmarshaller();
        issues = ((Issues)u.unmarshal(xmlFile)).getIssues();

        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return issues;
    }




}
