package org.jenkinsci.plugins.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Comment {

    private int id;
    private String created;
    private String body;

    public Comment() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getCreated() {
        String dateString = created.substring(0,created.indexOf('.'));
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        parser.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = parser.parse(dateString);
        } catch (ParseException ignored) {
        }
        return date;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id='" + id + '\'' +
                ", created='" + created + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
