package com.example;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Book {
    @JsonProperty("title")
    private String title;

    @JsonProperty("reviews")
    private List<Review> reviews;

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
}
