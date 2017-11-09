package org.jenkinsci.plugins.entity;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "test")
@XmlAccessorType(XmlAccessType.FIELD)
public class Issue {

    @XmlElement(name = "key", required = true)
    private String issueKey;
    @XmlElement(name = "status", required = true)
    private String status;
    @XmlElement(name = "summary")
    private String summary;
    @XmlElement(name = "time", required = true)
    private String time;
    @XmlElementWrapper(name = "attachments", nillable = true)
    @XmlElement(name = "attachment", nillable = true)
    private List<String> attachments;
    @XmlElementWrapper(name = "parameters", nillable = true)
    @XmlElement(name = "parameter", nillable = true)
    private List<Parameter> parameters;

    public Issue(String issueKey, String status, String summary, List<String> attachments, List<Parameter> parameters) {
        this.issueKey = issueKey;
        this.status = status;
        this.summary = summary;
        this.attachments = attachments;
        this.parameters = parameters;
    }

    public Issue() {
    }

    public Issue(String issueKey, String status, String summary, String time, List<String> attachments, List<Parameter> parameters) {
        this.issueKey = issueKey;
        this.status = status;
        this.summary = summary;
        this.time = time;
        this.attachments = attachments;
        this.parameters = parameters;
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

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Issue issue = (Issue) o;

        if (issueKey != null ? !issueKey.equals(issue.issueKey) : issue.issueKey != null) return false;
        if (status != null ? !status.equals(issue.status) : issue.status != null) return false;
        if (summary != null ? !summary.equals(issue.summary) : issue.summary != null) return false;
        if (time != null ? !time.equals(issue.time) : issue.time != null) return false;
        if (attachments != null ? !attachments.equals(issue.attachments) : issue.attachments != null) return false;
        return parameters != null ? parameters.equals(issue.parameters) : issue.parameters == null;
    }

    @Override
    public int hashCode() {
        int result = issueKey != null ? issueKey.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (summary != null ? summary.hashCode() : 0);
        result = 31 * result + (time != null ? time.hashCode() : 0);
        result = 31 * result + (attachments != null ? attachments.hashCode() : 0);
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Issue{" +
                "issueKey='" + issueKey + '\'' +
                ", status='" + status + '\'' +
                ", summary='" + summary + '\'' +
                ", time='" + time + '\'' +
                ", attachments=" + attachments +
                ", parameters=" + parameters +
                '}';
    }
}
