package com.shyam.ngmobile.Services;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.shyam.ngmobile.LoginActivity;
import com.shyam.ngmobile.Model.Member;
import com.shyam.ngmobile.Model.Subscription;

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
//        currentMember.setCity(member.getCity());
//        currentMember.setCountry(member.getCountry());
//        currentMember.setFirstTimeLogin(member.isFirstTimeLogin());
//        currentMember.setGymExpiryDate(member.getGymExpiryDate());
//        currentMember.setMemberExpiryDate(member.getMemberExpiryDate());
//        currentMember.setPhoneNumber(member.getPhoneNumber());
//        currentMember.setPostAddress(member.getPostAddress());
//        currentMember.setZipCode(member.getZipCode());

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
    }

    public static void logoutUser(Activity activity) {
        mAuth.signOut();
        gotoActivity(activity, LoginActivity.class);
    }

    public static void generateMemberStatement(Member member, Subscription subscription) {
        // TODO Create PDF Statement
    }
}
