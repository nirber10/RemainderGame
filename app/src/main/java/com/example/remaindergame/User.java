package com.example.remaindergame;

public class User {
    private String name;
    private String email;

    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public User() {}

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
