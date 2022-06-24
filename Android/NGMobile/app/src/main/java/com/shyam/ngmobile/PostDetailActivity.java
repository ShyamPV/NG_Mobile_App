package com.shyam.ngmobile;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.shyam.ngmobile.Model.Post;
import com.shyam.ngmobile.Services.Utils;
import com.squareup.picasso.Picasso;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
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
            btnGetDocument.setOnClickListener(view -> new DownloadFile().execute());
        } else {
            btnGetDocument.setVisibility(View.GONE);
        }

        titleText.setText(post.getTitle());
        dateText.setText(dateFormat.format(post.getStartTime()));
        String postTime = "From " + timeFormat.format(post.getStartTime())
                + " To " + timeFormat.format(post.getEndTime());
        timeText.setText(postTime);

        descriptionText.setText(post.getDescription());
        Linkify.addLinks(descriptionText,Linkify.ALL);

        pDialog.dismiss();
    }

    class DownloadFile extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            String folderPath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/Documents/Nairobi Gymkhana/";
            File savePath = new File(folderPath);

            try {
                savePath.mkdirs();
                File file = new File(savePath, post.getTitle() + ".pdf");
                file.delete();

                URL url = new URL(post.getDocumentURL());
                InputStream inputStream = url.openStream();

                DataInputStream dataInputStream = new DataInputStream(inputStream);
                byte[] buffer = new byte[1024];
                int length;

                FileOutputStream fos = new FileOutputStream(file);
                while ((length = dataInputStream.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }

                inputStream.close();
                fos.flush();
                fos.close();


            } catch (Exception e) {
                Log.e("doInBackground: ", e.getMessage());
                e.getStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            Toast.makeText(PostDetailActivity.this,
                    "Download Complete: Saved in /Documents/Nairobi Gymkhana/"
                            + post.getTitle() + ".pdf", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        Intent Home = new Intent(this,MainActivity.class);
        startActivity(Home);
    }
}