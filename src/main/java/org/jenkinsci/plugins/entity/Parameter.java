package org.jenkinsci.plugins.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;


@XmlAccessorType(XmlAccessType.FIELD)
public class Parameter {

    @XmlElement(name = "title")
    private String title;
    @XmlElement(name = "value")
    private String value;

    public Parameter() {
    }

    public Parameter(String title, String value) {
        this.title = title;
        this.value = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Parameter{" +
                "title='" + title + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
