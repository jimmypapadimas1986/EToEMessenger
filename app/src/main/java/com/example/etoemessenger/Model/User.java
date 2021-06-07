package com.example.etoemessenger.Model;

public class User {     //κλάση χρήστη

    private String id;
    private String username;
    private String ImageURL;

    public User(String id, String username, String imageUrl) {
        this.id = id;
        this.username = username;
        this.ImageURL = imageUrl;
    }

    public User() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageUrl() {
        return ImageURL;
    }

    public void setImageUrl(String imageUrl) {
        this.ImageURL = imageUrl;
    }
}
