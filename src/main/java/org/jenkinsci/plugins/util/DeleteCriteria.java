package org.jenkinsci.plugins.util;

import java.util.Calendar;

public class DeleteCriteria {

    public static int parse(String criteria) {
        switch (criteria) {
            case ("Month"):
                return Calendar.MONTH;
            case ("Year"):
                return Calendar.YEAR;
            case ("Week"):
                return Calendar.WEEK_OF_YEAR;
            case ("Day"):
                return Calendar.DATE;
            default:
                return Calendar.MONTH;
        }
    }

}
