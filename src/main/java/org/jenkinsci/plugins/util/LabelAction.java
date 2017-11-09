package org.jenkinsci.plugins.util;

public enum LabelAction {

    ADD("add", "to"),
    REMOVE("remove", "from");

    private String text;
    private String preposition;

    LabelAction(final String text, final String preposition) {
        this.text = text;
        this.preposition = preposition;
    }

    public String getPreposition() {
        return preposition;
    }

    @Override
    public String toString() {
        return text;
    }
}

