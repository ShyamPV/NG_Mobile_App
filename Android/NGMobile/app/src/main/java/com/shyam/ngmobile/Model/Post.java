package com.shyam.ngmobile.Model;

import java.util.Date;

public class Post {
    private String postID;
    private String title;
    private String description;
    private Date startTime;
    private Date EndTime;
    private String imageURL;
    private String documentURL;

    public Post() {
    }

    public Post(String postID, String title, String description,
                Date startTime, String imageURL, String documentURL) {
        this.postID = postID;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.imageURL = imageURL;
        this.documentURL = documentURL;
    }

    public String getPostID() {
        return postID;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public java.util.Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return EndTime;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getDocumentURL() {
        return documentURL;
    }
}
