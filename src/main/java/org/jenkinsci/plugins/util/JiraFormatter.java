package org.jenkinsci.plugins.util;

import org.jenkinsci.plugins.entity.Issue;
import org.jenkinsci.plugins.entity.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JiraFormatter is a class responsible for formatting issues according to Jira
 * Text Formatting Notation for their further publishing as Jira Issue comments
 * in a readable format.
 *
 * @author Alena Zubrevich
 */
public class JiraFormatter {
    private static final String LINE_SEPARATOR = "\\r";
    private static final String TITLE = "Test Management Plugin Auto-generated Report";

    /**
     * Makes text strong (bold)
     * Example: *strong*
     *
     * @param str text for processing
     * @return formatted text
     */
    private static String strong(String str) {
        return "*" + str + "*";
    }

    /**
     * Creates a bookmark anchor inside the page.
     * Example: {anchor:anchorname}
     *
     * @param str text to be hidden (because anchors aren't displays)
     * @return formatted anchor
     */
    private static String hiddenAnchor(String str) {
        return "{anchor:" + str + "}";
    }

    /**
     * Changes the color of a block of text.
     * Example: {color:red}look ma, red text!{color}
     *
     * @param str   text for processing
     * @param color the value of Color enum
     * @return formatted text
     */
    private static String color(String str, String color) {
        return String.format("{color:%s}%s{color}", color, str);
    }

    /**
     * Having the '^' followed by the name of an attachment will lead into a link
     * to the last current issue attachment with such a name. It's useful for files
     * with unique name (for instance, if they have timestamp in their name).
     * Example: [^attachment.ext]
     *
     * @param attachmentName the exact name of attached file
     * @return attachment name formatted as link
     */
    private static String attachmentLink(String attachmentName) {
        return "[^" + attachmentName + "]";
    }

    /**
     * Creates a link to a resource, this allows us to create links to different
     * attachments with exact name
     * Example: [attachment.ext|https://jira.epam.com/jira/secure/attachment/{issue-key}/attachment.ext]
     *
     * @param attachmentName the exact name of attached file
     * @param jiraLink       the link to issue attachment
     * @return attachment name formatted as link
     */
    private static String attachmentLink(String attachmentName, String jiraLink) {
        return "[" + attachmentName + "|" + jiraLink + "]";
    }

    /**
     * Returns string representation of two column Jira table with a header row.
     * Example: ||header 1||header 2||
     * |column A1 |column A2|
     * |column B1 |column B2|
     *
     * @param heading1 first column header
     * @param heading2 second column header
     * @param params   the list of values with titles (title will be put in the first column)
     * @return string with formatted table
     */
    private static String twoColumnTable(String heading1, String heading2, List<Parameter> params) {
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

    /**
     * Embraces a block of text within a fully customizable panel. At this time this panel
     * has all configuration options set by default, but this options might be implemented
     * as custom ones later (borderStyle, borderColor, borderWidth, bgColor, titleBGColor)
     *
     * @param title   the title of panel
     * @param content the body of panel
     * @return text formatted as Jira panel
     */
    private static String createPanel(String title, String content) {
        return String.format("{panel:title=%s|borderStyle=dashed|borderColor=#ccc|titleBGColor=#F7D6C1}%s{panel}",
                title, content);
    }

    /**
     * Formats issue in accordance with Jira Text Formatting Notation
     *
     * @param issue            issue for parsing
     * @param filesToJiraLinks mapping file paths (from xml file) to attached files links (from JIRA responses)
     * @param buildNumber      specified build number (from Jenkins build or specified by user)
     * @param previousStatus   issue status from previous build
     * @return test representation of issue in accordance with Jira Text Formatting Notation
     */
    public static String parseIssue(Issue issue, Map<String, String> filesToJiraLinks, int buildNumber,
                                    String previousStatus, String label) {
        String statusColor = TestResult.getColor(issue.getStatus());
        StringBuilder contentBuilder = new StringBuilder(LINE_SEPARATOR);

        contentBuilder.append(strong("Build:")).append(" ").append(buildNumber).append(LINE_SEPARATOR);
        contentBuilder.append(strong("Status:")).append(" ");

        if (previousStatus != null && !previousStatus.equalsIgnoreCase(issue.getStatus())) {
            contentBuilder.append(color(previousStatus, TestResult.getColor(previousStatus))).append(" -> ");
        }
        contentBuilder.append(color(issue.getStatus(), statusColor)).append(LINE_SEPARATOR);

        if (issue.getSummary() != null) {
            contentBuilder.append(strong("Summary:")).append(" ");
            String summary = replaceLineSeparator(issue.getSummary());
            if (issue.getAttachments() != null) {
                contentBuilder.append(replaceLinks(summary, issue.getAttachments()));
            } else
                contentBuilder.append(summary);
            contentBuilder.append(LINE_SEPARATOR);
        }

        if (issue.getTime() != null)
            contentBuilder.append(strong("Time elapsed:")).append(" ").append(issue.getTime()).append(LINE_SEPARATOR);

        if (issue.getParameters() != null) {
            contentBuilder.append(LINE_SEPARATOR).append(strong("Parameters")).append(LINE_SEPARATOR)
                    .append(twoColumnTable("Title", "Value", issue.getParameters()));
        }

        if (issue.getAttachments() != null) {
            List<Parameter> attachments = new ArrayList<>();
            for (String attachment : issue.getAttachments()) {
                String link = filesToJiraLinks.get(attachment);
                String name = extractFileName(attachment);
                boolean system = name.matches("(?:stacktrace|scr).\\d{4}-\\d{2}-\\d{2}T.*");
                attachments.add(new Parameter(attachmentLink(name, link), system ? "system" : "user"));
            }
            contentBuilder.append(LINE_SEPARATOR).append(strong("Attachments")).append(LINE_SEPARATOR)
                    .append(twoColumnTable("Attachment", "Created by", attachments));
        }
        String formattedPanel = createPanel(TITLE, contentBuilder.toString());

        return label != null ? hiddenAnchor(label) + formattedPanel : formattedPanel;
    }

    // Auxiliary methods

    // We have to replace all standard line separators with JSON-compatible
    private static String replaceLineSeparator(String text) {
        return text.replace("\n", LINE_SEPARATOR);
    }

    private static String extractFileName(String link) {
        return link.contains("\\")
                ? link.substring(link.lastIndexOf('\\') + 1)
                : link.contains("/") ? link.substring(link.lastIndexOf('/') + 1) : link;
    }

    private static String replaceLinks(String text, List<String> links) {
        String formattedText = text;
        for (String link : links) {
            String fileName = extractFileName(link);
            formattedText = formattedText.replaceAll(fileName, attachmentLink(fileName));
        }
        return formattedText;
    }

    public static String getTitle() {
        return TITLE;
    }
}
