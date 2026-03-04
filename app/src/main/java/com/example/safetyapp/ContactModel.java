package com.example.safetyapp;

// Model class for storing contact details
public class ContactModel {
    private String name;
    private String phone;

    // Constructor
    public ContactModel(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    // Getter methods
    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    // Setter methods
    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}