package com.shyam.ngmobile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.kd.dynamic.calendar.generator.ImageGenerator;

import java.util.Calendar;

public class PostDetailActivity extends AppCompatActivity {

    private String postID;
    private static final String POST_ID = "postID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        Intent intent = getIntent();

        postID = intent.getStringExtra(POST_ID);
    }
}