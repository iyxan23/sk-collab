package com.ihsan.sketch.collab;

public class OnlineProject {

    private String title;
    private String version;
    private String author;
    private String description;
    boolean isopen;
    boolean isExpanded;
    private String key;

    public OnlineProject(String title, String version, String author, boolean isopen, boolean isExpanded, String key) {
        this.title = title;
        this.version = version;
        this.author = author;
        this.isopen = isopen;
        this.isExpanded = isExpanded;
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public String getVersionProject() {
        return version;
    }

    public String getAuthor() {
        return author;
    }

    public String getOpen() {
        return isopen ? "Open-Sourced" : "Not Open-Source";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setVersionProject(String version) {
        this.version = version;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setIsopen(boolean isopen) {
        this.isopen = isopen;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
