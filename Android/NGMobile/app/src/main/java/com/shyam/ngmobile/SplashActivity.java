package com.shyam.ngmobile;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shyam.ngmobile.Enums.MemberStatus;
import com.shyam.ngmobile.Model.Member;
import com.shyam.ngmobile.Services.Utils;

import java.util.Calendar;
import java.util.Objects;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private CollectionReference memberRef;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        memberRef = db.collection("member");

        if (mAuth.getUid() != null) getFirestoreUser(mAuth.getUid());
        else openLogin();

    }

    private void getFirestoreUser(String userID) {
        memberRef.document(userID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Member member = Objects.requireNonNull(task.getResult()).toObject(Member.class);
                validateMember(member);
            } else {
                openLogin();
            }
        });
    }


    private void validateMember(Member member) {
        if (member.getAccountStatus() == MemberStatus.Cancelled) {
            openLogin();
        } else if (Calendar.getInstance().getTime().after(member.getMemberExpiryDate())) {
            memberRef.document(member.getUserID()).update("accountStatus", MemberStatus.Defaulted);
            member.setAccountStatus(MemberStatus.Defaulted);
            Utils.setCurrentMember(member);
            openPaymentActivity();
        } else {
            Utils.setCurrentMember(member);
            openHomeActivity();
        }
    }

    private void openHomeActivity() {
        Utils.gotoActivity(this, MainActivity.class);
    }

    private void openPaymentActivity() {
        Utils.gotoActivity(this, PaymentActivity.class);
    }

    private void openLogin() {
        Utils.logoutUser(this);
    }

}