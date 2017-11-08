package org.jenkinsci.plugins.entity;

/**
 * Created by Alena_Zubrevich on 11/8/2017.
 */
public class Attachment {
    private int id;
    private String filename;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        return "Attachment{" +
                "id='" + id + '\'' +
                ", filename='" + filename + '\'' +
                '}';
    }
}
