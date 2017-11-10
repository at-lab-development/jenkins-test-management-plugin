package org.jenkinsci.plugins.util;

public enum LabelAction {

    ADD("add"),
    REMOVE("remove");

    private String text;

    LabelAction(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}

