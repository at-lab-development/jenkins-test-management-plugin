package org.jenkinsci.plugins.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Label {
    private String prefix;
    private LabelOption additionalInfo;

    public Label(String prefix, LabelOption additionalInfo) {
        this.prefix = prefix != null ? prefix : "";
        this.additionalInfo = additionalInfo;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix != null ? prefix : "";
    }

    public boolean needDate() {
        return additionalInfo != null && additionalInfo.equals(LabelOption.BUILD_DATE);
    }

    private String formatWith(Date date) {
        if (!needDate())
            throw new IllegalArgumentException("Cannot format label with date. It must contain " + additionalInfo);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        return prefix + formatter.format(date);
    }

    public String formatWithTodayDate() {
        return formatWith(new Date());
    }

    public String formatWith(int buildNumber) {
        return prefix + buildNumber;
    }
}
