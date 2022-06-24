package com.shyam.ngmobile;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.shyam.ngmobile.Model.Member;
import com.shyam.ngmobile.Services.Utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ViewPDFActivity extends AppCompatActivity {

    private Member member;
    private StorageReference certificateRef;

    private String certificateURI;
    private PDFView pdfView;
    private SweetAlertDialog pDialog;
    private Button btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pdf);

        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setTitle("Loading...");
        pDialog.getProgressHelper().setBarColor(ContextCompat.getColor(this, R.color.ng_blue));
        pDialog.setCancelable(false);
        pDialog.show();

        certificateRef = FirebaseStorage.getInstance().getReference("certificate");

        member = Utils.getCurrentMember();

        certificateURI = getIntent().getStringExtra("URL");
        pdfView = findViewById(R.id.pdf_view);

        if (certificateURI == null) {
            Toast.makeText(this, "Certificate Not found. Please contact the club", Toast.LENGTH_SHORT).show();
            finish();
        }

        new RetrivePDFfromUrl().execute(certificateURI);

        btnDelete = findViewById(R.id.btn_delete_certificate);
        btnDelete.setOnClickListener(view -> {
            showConfirmDeleteDialog();
        });

    }

    class RetrivePDFfromUrl extends AsyncTask<String, Void, InputStream> {
        @Override
        protected InputStream doInBackground(String... strings) {
            // we are using inputstream
            // for getting out PDF.
            InputStream inputStream = null;
            try {
                URL url = new URL(strings[0]);
                // below is the step where we are
                // creating our connection.
                HttpURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                if (urlConnection.getResponseCode() == 200) {
                    // response is success.
                    // we are getting input stream from url
                    // and storing it in our variable.
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                }

            } catch (IOException e) {
                // this is the method
                // to handle errors.
                e.printStackTrace();
                return null;
            }
            return inputStream;
        }

        @Override
        protected void onPostExecute(InputStream inputStream) {
            // after the execution of our async
            // task we are loading our pdf in our pdf view.
            pdfView.fromStream(inputStream).load();
            pDialog.dismiss();
        }
    }

    private void showConfirmDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Certificate");
        builder.setMessage("Please confirm that you want to delete the vaccination certificate.");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            deleteCertificate();
        });
        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        Button PB = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button NB = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);

        PB.setTextSize(18);
        PB.setTypeface(Typeface.DEFAULT_BOLD);
        PB.setBackgroundColor(ContextCompat.getColor(this, R.color.ng_error_red));
        PB.setTextColor(ContextCompat.getColor(this, R.color.white));

        NB.setTextSize(18);
        NB.setTypeface(Typeface.DEFAULT_BOLD);
        NB.setBackgroundColor(ContextCompat.getColor(this, R.color.ng_blue));
        NB.setTextColor(ContextCompat.getColor(this, R.color.white));

    }

    private void deleteCertificate() {
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(ContextCompat.getColor(this, R.color.ng_blue));
        pDialog.setCancelable(true);
        pDialog.setTitle("Deleting Certificate");

        certificateRef.child(member.getMembershipNo()).delete().addOnCompleteListener(task -> {
            pDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(this, "Certificate Deleted!", Toast.LENGTH_LONG).show();
                Utils.gotoActivity(this, MainActivity.class);
            } else {
                Utils.displayMessage(this, "Error", "Certificate was not deleted.");
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}