package com.shyam.ngmobile;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.shyam.ngmobile.Enums.MemberStatus;
import com.shyam.ngmobile.Model.Member;
import com.shyam.ngmobile.Model.Subscription;
import com.shyam.ngmobile.Services.Utils;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private CollectionReference memberRef;
    // TODO remove this once payment is implemented
    private static final String ORDINARY = "Ordinary Member";
    private static final String LADY = "Lady Member";
    private static final String JUNIOR = "Junior Member";
    private static final String UPCOUNTRY = "Upcountry Member";

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        memberRef = db.collection("member");

        setupNotification();

        if (mAuth.getUid() != null) getFirestoreUser(mAuth.getUid());
        else openLogin();

    }

    private void setupNotification() {
        FirebaseMessaging.getInstance().subscribeToTopic("club_updates");
    }

    private void getFirestoreUser(String userID) {
        try {
            memberRef.document(userID).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Member member = task.getResult().toObject(Member.class);
                    validateMember(member);
                } else {
                    openLogin();
                }
            }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            Log.e("getFirestoreUser: ", e.getMessage());
        }
    }

    private void validateMember(Member member) {
        if (member.getAccountStatus() == MemberStatus.Cancelled) {
            openLogin();
        } else if (Calendar.getInstance().getTime().after(member.getMemberExpiryDate())) {
            memberRef.document(member.getUserID()).update("accountStatus", MemberStatus.Defaulted);
            member.setAccountStatus(MemberStatus.Defaulted);
            getSubsAmount(getDefaultedDate(member), member);
            // TODO Change this once payment is implemented
//            Utils.setCurrentMember(member);
//            openPaymentActivity();
            openLogin();
        } else {
            Utils.setCurrentMember(member);
            openHomeActivity();
        }
    }

    // TODO Remove this once payment is implemented-------------------------------------------------
    private void getSubsAmount(Date defaultedDate, Member member) {
        final DecimalFormat decimalFormatter = new DecimalFormat("###,###,###.00");
        String memberType;

        if (member.getMemberType().equals(ORDINARY) || member.getMemberType().equals(UPCOUNTRY)) {
            memberType = "Full Member";
        } else {
            memberType = member.getMemberType();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(member.getMemberExpiryDate());
        int year = calendar.get(Calendar.YEAR);

        FirebaseFirestore.getInstance().collection("subscription")
                .whereEqualTo("subsYear", year)
                .whereEqualTo("memberType", memberType)
                .get().addOnCompleteListener(task -> {

            String message = "Subscriptions are due. Please visit the club to pay.\n";
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                Subscription subscription = task.getResult().getDocuments().get(0).toObject(Subscription.class);
                message += "You can also pay via M-Pesa.\n" +
                        "Paybill: 542542\n" +
                        "Account No: 000550#" + member.getMembershipNo() + "\n";
                double amount = 0;
                Date today = Calendar.getInstance().getTime();
                if (today.after(defaultedDate)) {
                    amount = subscription.getSubsTotal() + 5000;
                } else {
                    amount = subscription.getSubsTotal();
                }

                message += "Amount: Ksh " + decimalFormatter.format(amount) + "/=";

            }
            Utils.displayMessage(this, "Error!", message);
        });
    }

    private Date getDefaultedDate(Member member) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(member.getMemberExpiryDate());
        calendar.set(Calendar.MONTH, Calendar.APRIL);
        calendar.set(Calendar.DATE, 1);

        return calendar.getTime();
    }

    //----------------------------------------------------------------------------------------------
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