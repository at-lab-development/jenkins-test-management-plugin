package org.jenkinsci.plugins.entity;

public class Issue {

    private final String issueKey;
    private final String status;

    public Issue(String issueKey, String result) {
        this.issueKey = issueKey;
        this.status = result;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Issue{" + "issueKey='" + issueKey + '\'' + ", status='" + status + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Issue issue = (Issue) o;

        if (issueKey != null ? !issueKey.equals(issue.issueKey) : issue.issueKey != null) return false;
        return status != null ? status.equals(issue.status) : issue.status == null;
    }

    @Override
    public int hashCode() {
        int result1 = issueKey != null ? issueKey.hashCode() : 0;
        result1 = 31 * result1 + (status != null ? status.hashCode() : 0);
        return result1;
    }
}
