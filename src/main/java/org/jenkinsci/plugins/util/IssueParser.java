package org.jenkinsci.plugins.util;

import org.jenkinsci.plugins.entity.Issue;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IssueParser {
    private List<Issue> issues;

    private DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

    public List<Issue> getIssues(File xmlFile) {
        try {
            issues = new ArrayList<>();
            NodeList nodes;
            DocumentBuilder dBuilder;
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            nodes = doc.getElementsByTagName("test");
            for (int i = 0; i < nodes.getLength(); i++) {
                NodeList testResults = nodes.item(i).getChildNodes();
                String key = testResults.item(1).getTextContent();
                String status = testResults.item(3).getTextContent();
                issues.add(new Issue(key, status));
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return issues;
    }


}
