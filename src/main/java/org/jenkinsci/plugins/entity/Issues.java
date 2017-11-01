package org.jenkinsci.plugins.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "tests")
@XmlAccessorType(XmlAccessType.FIELD)
public class Issues {

    @XmlElement(name = "test")
    private List<Issue> issues;

    public Issues() {
    }

    public Issues(List<Issue> issues) {
        this.issues = issues;
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }

    @Override
    public String toString() {
        return "Issues{" +
                "issues=" + issues +
                '}';
    }
}
