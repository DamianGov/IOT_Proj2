package com.example.iot_proj2;

public class Vacancy {
    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    private String created_by;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String description;

    public long getDocId() {
        return docId;
    }

    public void setDocId(long docId) {
        this.docId = docId;
    }

    private long docId;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    private String module;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String status;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String type;


    public String getLecturer() {
        return lecturer;
    }

    public void setLecturer(String lecturer) {
        this.lecturer = lecturer;
    }

    private String lecturer;

    private String salary, semester;

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public Vacancy() {

    }

    public Vacancy(String created_by, String description, long docId, String module, String status, String type, String lecturer, String semester, String salary) {
        this.created_by = created_by;
        this.description = description;
        this.docId = docId;
        this.module = module;
        this.status = status;
        this.type = type;
        this.lecturer = lecturer;
        this.semester = semester;
        this.salary = salary;
    }


}
