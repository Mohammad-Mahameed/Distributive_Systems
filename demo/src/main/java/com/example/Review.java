package com.example;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Review {
    @JsonProperty("id")
    private String id;

    @JsonProperty("link")
    private String link;

    @JsonProperty("title")
    private String reviewTitle;

    @JsonProperty("text")
    private String reviewText;

    @JsonProperty("rating")
    private int rating;

    @JsonProperty("author")
    private String author;

    @JsonProperty("date")
    private String date;

    private HtmlColor htmlColor;
    private Boolean sarcasm;
    private List<String> entity;
    private int sentiment;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getReviewTitle() {
        return reviewTitle;
    }

    public void setReviewTitle(String reviewTitle) {
        this.reviewTitle = reviewTitle;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setHtmlColor(HtmlColor htmlColor){
        this.htmlColor = htmlColor;
    }

    public HtmlColor getHtmlColor(){
        return this.htmlColor;
    }

    public void setSarcasm(Boolean sarcasm){
        this.sarcasm = sarcasm;
    }

    public Boolean getSarcasm(){
        return this.sarcasm;
    }

    public void setEntity(List<String> entity){
        this.entity = entity;
    }

    public List<String> getEntity(){
        return this.entity;
    }

    public String getEntityString(){
        if(this.entity == null)
            return "[]";
        String res = "[";
        for(String eString : this.entity){
            res = res + eString + ", ";
        }

        if(res.length() <= 3){
            return "[]";
        }
        res = res.substring(0, res.length()-2);
        res = res + "]";
        return res;
    }

    public void setSentiment(int sentiment){
        this.sentiment = sentiment;
    }

    public int getSentiment(){
        return this.sentiment;
    }
}
