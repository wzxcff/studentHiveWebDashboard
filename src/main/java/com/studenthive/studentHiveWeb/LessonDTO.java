package com.studenthive.studentHiveWeb;

import org.bson.types.ObjectId;

public class LessonDTO {
    private String number;
    private String type;
    private String time;
    private String subject;
    private String lecturer;
    private String link;
    private ObjectId Id;

    // Constructor
    public LessonDTO(String number, String type, String time, String subject, String lecturer, String link, ObjectId Id) {
        this.number = number;
        this.type = type;
        this.time = time;
        this.subject = subject;
        this.lecturer = lecturer;
        this.link = link;
        this.Id = Id;
    }

    // Constructor without Id
    public LessonDTO(String number, String type, String time, String subject, String lecturer, String link) {
        this.number = number;
        this.type = type;
        this.time = time;
        this.subject = subject;
        this.lecturer = lecturer;
        this.link = link;
    }

    // Getters and Setters
    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getLecturer() {
        return lecturer;
    }

    public void setLecturer(String lecturer) {
        this.lecturer = lecturer;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}