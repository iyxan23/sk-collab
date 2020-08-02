package com.ihsan.sketch.collab;

import java.util.ArrayList;

public class OnlineProject {

    private String title;
    private String version;
    private String author;
    private String description;
    private String user_author_name;
    ArrayList<Commit> commits;
    ArrayList<String> collaborators_uid;
    boolean isopen;
    boolean isExpanded;
    private String key;

    public OnlineProject(String title, String version, String author, String user_author_name, boolean isopen, boolean isExpanded, String key, ArrayList<Commit> commits, ArrayList<String> collaborators_uid) {
        this.commits = commits;
        this.title = title;
        this.version = version;
        this.author = author;
        this.user_author_name = user_author_name;
        this.isopen = isopen;
        this.isExpanded = isExpanded;
        this.collaborators_uid = collaborators_uid;
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

    public String getUser_author_name() {
        return user_author_name;
    }

    public void setUser_author_name(String user_author_name) {
        this.user_author_name = user_author_name;
    }

    @Override
    public String toString() {
        return "OnlineProject{" +
                "title='" + title + '\'' +
                ", version='" + version + '\'' +
                ", author='" + author + '\'' +
                ", description='" + description + '\'' +
                ", user_author_name='" + user_author_name + '\'' +
                ", commits=" + commits +
                ", isopen=" + isopen +
                ", isExpanded=" + isExpanded +
                ", key='" + key + '\'' +
                '}';
    }
}
