package org.jenkinsci.plugins.util;

import org.jenkinsci.plugins.entity.Issue;
import org.jenkinsci.plugins.entity.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JiraFormatter {

    public enum Color { RED, GRAY, GREEN }
    private static final String LINE_SEPARATOR = System.lineSeparator();//"\\\\ ";

    public static String bold(String str) {
        return "*" + str + "*";
    }

    public static String color(String str, Color color) {
        return String.format("{color:%s}%s{color}", color.toString().toLowerCase(), str);
    }

    public static String attachmentLink(String attachmentName) {
        return "[^" + attachmentName + "]";
    }

    /**
     * Returns string representation of two column Jira table created using Jira
     * Text Formatting Notation:
     * @link https://jira.atlassian.com/secure/WikiRendererHelpAction.jspa?section=all
     *
     * Example:
     * ||header 1||header 2||
     * |col A1|col A2|
     * |col B1|col B2|
     *
     * @param  heading1 first column header
     * @param  heading2 second column header
     * @param  params the list of values with titles (title will be put in the first column)
     * @return      string with formatted table
     */
    public static String twoColumnTable(String heading1, String heading2, List<Parameter> params) {
        final String TITLE_SEPARATOR = "||";
        final String VALUE_SEPARATOR = "|";

        StringBuilder builder = new StringBuilder();

        //Form header
        builder.append(TITLE_SEPARATOR).append(heading1).append(TITLE_SEPARATOR).append(heading2).append(TITLE_SEPARATOR)
                .append(LINE_SEPARATOR);

        //Form body
        for (Parameter param : params) {
            builder.append(VALUE_SEPARATOR).append(param.getTitle()).append(VALUE_SEPARATOR).append(param.getValue())
                        .append(VALUE_SEPARATOR).append(LINE_SEPARATOR);
        }

        return builder.toString();
    }

    public static String createPanel(String title, String content) {
        return String.format("{panel:title=%s|borderStyle=dashed|borderColor=#ccc|titleBGColor=#F7D6C1}%s{panel}",
                title, content);
    }

    public static Color chooseColor(String status) {
        return status.equals(TestResult.FAILED.toString()) ? Color.RED
                : status.equals(TestResult.PASSED.toString())  ? Color.GREEN : Color.GRAY;
    }

    public static String extractFileName(String link) {
        return link.contains("/") ? link.substring(link.lastIndexOf('/') + 1) : link;
    }

    public static String replaceLinks(String text, List<String> links) {
        String formattedText = text;
        for (String link : links) {
            String fileName = extractFileName(link);
            formattedText = formattedText.replaceAll(fileName, attachmentLink(fileName));
        }
        return formattedText;
    }

    public static String parseIssue (Issue issue) {
        Color statusColor = chooseColor(issue.getStatus());
        String title = "Test Management Plugin Auto-generated Report";
        StringBuilder contentBuilder = new StringBuilder(LINE_SEPARATOR);

        contentBuilder.append(bold("Build:")).append(" no...").append(LINE_SEPARATOR); //todo Build number or sth like that...
        contentBuilder.append(bold("Status:")).append(" ").append(color(issue.getStatus(), statusColor))
                .append(LINE_SEPARATOR); //todo check previous status

        if (issue.getSummary() != null) {
            contentBuilder.append(bold("Summary:")).append(" ")
                    .append(replaceLinks(issue.getSummary(), issue.getAttachments())).append(LINE_SEPARATOR);
        }

        contentBuilder.append(bold("Time elapsed:")).append(" ").append(issue.getTime()).append(LINE_SEPARATOR);

        if (issue.getParameters() != null) {
            contentBuilder.append(LINE_SEPARATOR).append(bold("Parameters")).append(LINE_SEPARATOR)
                    .append(twoColumnTable("Title", "Value", issue.getParameters()));
        }

        if (issue.getAttachments() != null) {
            List<Parameter> attachments = new ArrayList<>();
            for (String attachment : issue.getAttachments()) {
                attachments.add(new Parameter(attachmentLink(extractFileName(attachment)), "system"));
            }
            contentBuilder.append(LINE_SEPARATOR).append(bold("Attachments")).append(LINE_SEPARATOR)
                    .append(twoColumnTable("Attachment", "Created by", attachments));
        }

        return createPanel(title, contentBuilder.toString());
    }

    public static void main(String[] args) {
        List<String> attachs = new ArrayList();
        attachs.add("/target/stacktrace-TEST-7.txt");
        attachs.add("/target/scr_819587617750581.png");

        List<Parameter> params = new ArrayList();
        params.add(new Parameter("Room count", "2"));
        params.add(new Parameter("Price", "300"));

        Issue issue = new Issue("TTT-111", "Failed", "Failed due to: java.lang.RuntimeException: WTF!. Full stack " +
                "trace attached as 'stacktrace-TEST-7.txt'. Screenshot attached as 'scr_819587617750581.png'", "7.09 s",
                attachs, params);
        System.out.println(parseIssue(issue));
    }
}
