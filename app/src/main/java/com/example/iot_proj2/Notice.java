package com.example.iot_proj2;

public class Notice {

    private String created_by, description, lecturer, time, title;
    private long docId;

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLecturer() {
        return lecturer;
    }

    public void setLecturer(String lecturer) {
        this.lecturer = lecturer;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getDocId() {
        return docId;
    }

    private boolean expired;

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    private String faculty;

    public Notice(String created_by, String description, String lecturer, String time, String title, long docId, boolean expired, String faculty) {
        this.created_by = created_by;
        this.description = description;
        this.lecturer = lecturer;
        this.time = time;
        this.title = title;
        this.docId = docId;
        this.expired = expired;
        this.faculty = faculty;
    }

    public Notice() {
    }

    public void setDocId(long docId) {
        this.docId = docId;
    }
}
