package com.shyam.ngmobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.shyam.ngmobile.Model.Post;
import com.shyam.ngmobile.Services.Utils;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class PostDetailActivity extends AppCompatActivity {

    private String postID;
    private static final String POST_ID = "postID";
    private Post post;
    private TextView titleText, dateText, timeText, descriptionText;
    private ImageView imageView;
    private Button btnGetDocument;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa", Locale.ENGLISH);
    private SweetAlertDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        Intent intent = getIntent();

        postID = intent.getStringExtra(POST_ID);

        setup();
    }

    private void setup() {

        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setTitle("Loading Post...");
        pDialog.getProgressHelper().setBarColor(ContextCompat.getColor(this, R.color.ng_blue));
        pDialog.setCancelable(false);
        pDialog.show();

        imageView = findViewById(R.id.post_detail_image);
        titleText = findViewById(R.id.post_detail_title);
        dateText = findViewById(R.id.post_detail_date);
        timeText = findViewById(R.id.post_detail_time);
        descriptionText = findViewById(R.id.post_details_description);
        btnGetDocument = findViewById(R.id.btn_post_detail_document);


        getPost();
    }

    private void getPost() {
        Post.GetPostByID(postID, _post -> {
            if (_post != null) {
                post = _post;
                displayPost(post);
            } else {
                pDialog.dismiss();
                Utils.displayMessage(this, "Error!", "Could not retrieve the post");
            }
        });


    }

    private void displayPost(Post post) {
        if (!post.getImageURL().equals("")) {
            Picasso.get().load(post.getImageURL()).into(imageView);
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.GONE);
        }

        if (!post.getDocumentURL().equals("")) {
            btnGetDocument.setVisibility(View.VISIBLE);
            btnGetDocument.setOnClickListener(view -> {
                downloadDocument(post.getDocumentURL());
            });
        } else {
            btnGetDocument.setVisibility(View.GONE);
        }

        titleText.setText(post.getTitle());
        dateText.setText(dateFormat.format(post.getStartTime()));
        String postTime = "From " + timeFormat.format(post.getStartTime())
                + " To " + timeFormat.format(post.getEndTime());
        timeText.setText(postTime);

        descriptionText.setText(post.getDescription());

        pDialog.dismiss();
    }

    private void downloadDocument(String documentURL) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}