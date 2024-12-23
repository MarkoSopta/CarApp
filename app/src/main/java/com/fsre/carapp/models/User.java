package com.fsre.carapp.models;

import java.util.Date;

public class User {
    private String firstname;
    private String lastname;
    private String email;
    private String dob;
    private Date date;
    private String profileImageUrl;

    public User() {
    }

    public User(String firstname, String lastname, String email, String dob, String profileImageUrl) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.dob = dob;
        this.date = new Date();
        this.profileImageUrl = profileImageUrl;
    }


    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
        updateDate();
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
        updateDate();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        updateDate();
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
        updateDate();
    }

    public Date getDate() {
        return date;
    }

    public void updateDate() {
        this.date = new Date();
    }
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
