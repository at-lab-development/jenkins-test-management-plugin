package org.jenkinsci.plugins.parser;

import java.util.GregorianCalendar;

public class CommentParser {

    public static int parseCreationMonth(String date) {
        return Integer.parseInt(date.substring(5, 7));
    }

}
