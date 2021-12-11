package com.shyam.ngmobile.Model;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Post {
    private static CollectionReference PostRef = FirebaseFirestore.getInstance().collection("post");
    private String postID;
    private String title;
    private String description;
    private Date startTime;
    private Date endTime;
    private String imageURL;
    private String documentURL;

    public Post() {
    }

    public Post(String postID, String title, String description,
                Date startTime, Date endTime, String imageURL, String documentURL) {
        this.postID = postID;
        this.title = title;
        this.description = description;
        this.endTime = endTime;
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
        return endTime;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getDocumentURL() {
        return documentURL;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public static void GetQueryPosts(Query query, final GetPostList getPostList) {
        query.get().addOnCompleteListener(task -> {
            ArrayList<Post> postArrayList = new ArrayList<>();
            DocumentSnapshot snapshot = null;

            if (task.isSuccessful() && task.getResult().size() > 0) {
                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                    Post post = documentSnapshot.toObject(Post.class);
                    setPostDates(post);
                    post.setPostID(documentSnapshot.getId());
                    postArrayList.add(post);
                }
                snapshot = task.getResult().getDocuments().get(task.getResult().size() - 1);
            }

            getPostList.OnComplete(postArrayList, snapshot);
        });
    }

    private static void setPostDates(Post post) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(post.getStartTime());
        calendar.add(Calendar.HOUR, -3);
        post.setStartTime(calendar.getTime());
        calendar.setTime(post.getEndTime());
        calendar.add(Calendar.HOUR, -3);
        post.setEndTime(calendar.getTime());

    }

    public static void GetPostByID(String postID, final GetPost getPost) {
        PostRef.document(postID).get().addOnCompleteListener(task -> {
            Post post = null;
            if (task.isSuccessful() && task.getResult().exists()) {
                post = task.getResult().toObject(Post.class);
                setPostDates(post);
            }
            getPost.OnComplete(post);
        });
    }

    public interface GetPostList {
        void OnComplete(ArrayList<Post> postList, DocumentSnapshot snapshot);
    }

    public interface GetPost {
        void OnComplete(Post post);
    }


}
