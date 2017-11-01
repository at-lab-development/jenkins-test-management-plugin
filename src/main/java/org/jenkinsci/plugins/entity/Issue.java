package org.jenkinsci.plugins.entity;

import javax.xml.bind.annotation.*;
import java.io.File;
import java.util.List;

@XmlRootElement(name = "test")
@XmlAccessorType(XmlAccessType.FIELD)
public class Issue {

    @XmlElement(name = "key", required = true)
    private String issueKey;
    @XmlElement(name = "status", required = true)
    private String status;
    @XmlElementWrapper(name = "comments", nillable = true)
    @XmlElement(name = "comment", nillable = true)
    private List<String> comments;
    @XmlElementWrapper(name = "attachments", nillable = true)
    @XmlElement(name = "attachment", nillable = true)
    private List<String> attachments;

    public Issue(String issueKey, String status) {
        this.issueKey = issueKey;
        this.status = status;
    }

    public Issue(String issueKey, String status, List<String> comments, List<String> attachments) {
        this.issueKey = issueKey;
        this.status = status;
        this.comments = comments;
        this.attachments = attachments;
    }

    public Issue() {
    }

    public String getIssueKey() {
        return issueKey;
    }

    public void setIssueKey(String issueKey) {
        this.issueKey = issueKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    @Override
    public String toString() {
        return "Issue{" +
                "issueKey='" + issueKey + '\'' +
                ", status='" + status + '\'' +
                ", comments=" + comments +
                ", attachments=" + attachments +
                '}';
    }
}
