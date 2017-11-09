package org.jenkinsci.plugins.util;

public class DeleteCriteria {

    public static int parse(String criteria) {
        switch (criteria) {
            case ("Month"):
                return 2;
            case ("Year"):
                return 1;
            case ("Week"):
                return 3;
            case ("Day"):
                return 5;
            default:
                return 2;
        }
    }

}
