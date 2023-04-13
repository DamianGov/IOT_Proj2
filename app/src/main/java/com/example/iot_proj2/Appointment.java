package com.example.iot_proj2;

public class Appointment {
    private long docId;
    private String staff_num, start_time, status, stud_num, lecturerName, lecturerEmail, studentName, studentEmail;

    public String getLecturerName() {
        return lecturerName;
    }

    public void setLecturerName(String lecturerName) {
        this.lecturerName = lecturerName;
    }

    public String getLecturerEmail() {
        return lecturerEmail;
    }

    public void setLecturerEmail(String lecturerEmail) {
        this.lecturerEmail = lecturerEmail;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public Appointment() {
    }

    public Appointment(long docId, String staff_num, String start_time, String status, String stud_num) {
        this.docId = docId;
        this.staff_num = staff_num;
        this.start_time = start_time;
        this.status = status;
        this.stud_num = stud_num;
    }

    public long getDocId() {
        return docId;
    }

    public void setDocId(long docId) {
        this.docId = docId;
    }

    public String getStaff_num() {
        return staff_num;
    }

    public void setStaff_num(String staff_num) {
        this.staff_num = staff_num;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStud_num() {
        return stud_num;
    }

    public void setStud_num(String stud_num) {
        this.stud_num = stud_num;
    }
}
