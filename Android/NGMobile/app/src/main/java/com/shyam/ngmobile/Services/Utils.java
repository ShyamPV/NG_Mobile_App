package com.shyam.ngmobile.Services;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.shyam.ngmobile.LoginActivity;
import com.shyam.ngmobile.Model.Member;
import com.shyam.ngmobile.Model.Subscription;
import com.shyam.ngmobile.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Utils {
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public static Member currentMember;
    public static void setCurrentMember(Member member) {
        currentMember = member;
    }

    public static Member getCurrentMember() {
        if (currentMember != null)
            return currentMember;
        throw new NullPointerException("No Logged in Member");
    }

    public static void updateCurrentMember(Member member) {
        setCurrentMember(member);
    }

    public static void gotoActivity(Activity currentActivity, Class<?> newActivityClass) {
        if (currentActivity.getClass().equals(newActivityClass)) {
            Toast.makeText(currentActivity, "Current Window", Toast.LENGTH_SHORT).show();
        } else {
            currentActivity.startActivity(new Intent(currentActivity, newActivityClass));
            currentActivity.finish();
        }
    }

    public static void displayMessage(Activity activity, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK!", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        Button PB = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        PB.setTextSize(18);
        PB.setTypeface(Typeface.DEFAULT_BOLD);
        if (title.equals("Error!")) {
            PB.setBackgroundColor(ContextCompat.getColor(activity, R.color.ng_error_red));
        } else {
            PB.setBackgroundColor(ContextCompat.getColor(activity, R.color.ng_blue));
        }
        PB.setTextColor(ContextCompat.getColor(activity, R.color.white));
    }

    public static void logoutUser(Activity activity) {

        FirebaseMessaging.getInstance().unsubscribeFromTopic("club_updates");
        mAuth.signOut();
        gotoActivity(activity, LoginActivity.class);
    }
}
