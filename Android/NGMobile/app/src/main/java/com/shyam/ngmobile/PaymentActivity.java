package com.shyam.ngmobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shyam.ngmobile.Enums.MemberStatus;
import com.shyam.ngmobile.Model.Member;
import com.shyam.ngmobile.Model.Subscription;
import com.shyam.ngmobile.Model.Transaction;
import com.shyam.ngmobile.Services.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity {

    private static final String FULL_MEMBER = "Full Member";
    private static final String ORDINARY = "Ordinary Member";
    private static final String LADY = "Lady Member";
    private static final String JUNIOR = "Junior Member";
    private static final String UPCOUNTRY = "Upcountry Member";
    private static final String GYM_PAYMENT = "Gym Subscription Renewal";
    private static final String SUBS_PAYMENT = "Membership Subs Payment";
    private static final String PAYBILL = "542542";
    private static final String SUBS_ACC = "000550";
    private static final String PREPAID_ACC = "000551";


    private EditText memberTypeText, expiryDateText, gymExpiryDate, amountDue;
    private Spinner gymAmountSpinner;
    private Button statement, pay;
    private SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
    private Member member;
    private Subscription subscription;

    private CollectionReference subsRef;
    private CollectionReference memberRef;
    private CollectionReference transactionRef;
    private LinkedHashMap<String, Double> gymAmountsHash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        setup();
    }

    private void setup() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        subsRef = db.collection("subscription");
        memberRef = db.collection("member");
        transactionRef = db.collection("transaction");

        member = Utils.getCurrentMember();

        String subMemberType = getSubMemberType();

        memberTypeText = findViewById(R.id.payment_member_type);
        expiryDateText = findViewById(R.id.payment_member_expiry_date);
        gymExpiryDate = findViewById(R.id.payment_gym_expiry);
        amountDue = findViewById(R.id.payment_member_amount);
        pay = findViewById(R.id.btn_make_payment);
        statement = findViewById(R.id.generate_statement);
        gymAmountSpinner = findViewById(R.id.gym_amount_spinner);

        memberTypeText.setText(member.getMemberType());
        expiryDateText.setText(format.format(member.getMemberExpiryDate()));
        if (member.getGymExpiryDate() != null)
            gymExpiryDate.setText(format.format(member.getGymExpiryDate()));

        setGymAmount();

        pay.setOnClickListener(view -> {
            if (subscription == null && gymAmountSpinner.getSelectedItemPosition() == 0) {
                Utils.displayMessage(this, "Error!",
                        "Please select the gym option");
            } else if (gymAmountSpinner.getSelectedItemPosition() == 0) {
                //
                double amount = subscription.getSubsTotal();
                processPayment(amount, SUBS_PAYMENT);
            } else {
                double amount = gymAmountsHash.get(gymAmountSpinner.getSelectedItem().toString());
                processPayment(amount, GYM_PAYMENT);
            }
        });

        statement.setOnClickListener(view -> Utils.generateMemberStatement(member, subscription));

        if (!subMemberType.equals("")) {
            getSubscription(subMemberType);
        }
    }

    private void processPayment(double amount, String paymentType) {
        // TODO show pDialog
        // TODO "test demo" Remove disable button
        pay.setEnabled(false);
        String mpesaAccountRef;
        if (paymentType.equals(SUBS_PAYMENT)) {
            mpesaAccountRef = SUBS_ACC + "#" + member.getMembershipNo();
        } else {
            mpesaAccountRef = PREPAID_ACC + "#" + member.getMembershipNo();
        }
        // TODO Payment via mpesa


        // TODO if payment Successful
        updateDates(paymentType);
    }

    private void updateDates(String paymentType) {
        if (paymentType.equals(SUBS_PAYMENT)) {
            updateMemberExpiryDate();
        } else {
            updateGymExpiryDate();
        }
    }

    private void updateMemberExpiryDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(member.getMemberExpiryDate());
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
        Date newMemberExpiryDate = calendar.getTime();

        memberRef.document(member.getUserID()).update("memberExpiryDate", newMemberExpiryDate);
        member.setMemberExpiryDate(newMemberExpiryDate);
        expiryDateText.setText(format.format(newMemberExpiryDate));

        createTransactionTicket(SUBS_PAYMENT, newMemberExpiryDate);
    }

    private void updateGymExpiryDate() {
        Calendar calendar = Calendar.getInstance();
        Log.e("TIME NOW: ", calendar.getTime().toString());
        Log.e("TIME CHECK: ", String.valueOf(member.getGymExpiryDate().after(calendar.getTime())));
        if (member.getGymExpiryDate().after(calendar.getTime()))
            calendar.setTime(member.getGymExpiryDate());

        switch (gymAmountSpinner.getSelectedItem().toString()) {
            case "Annual:Ksh 28,000/= (Single)":
            case "Annual:Ksh 40,000/= (Couple)":
                calendar.add(Calendar.YEAR, 1);
                break;
            case "Semi Annual: Ksh 16,000/= (Single)":
            case "Semi Annual: Ksh 24,000/= (Couple)":
                calendar.add(Calendar.MONTH, 6);
                break;
            case "Quarterly:Ksh 9,000/= (Single and Over 18 only)":
                calendar.add(Calendar.MONTH, 3);
                break;
            case "Monthly:Ksh 3,500/= (Single Only)":
                calendar.add(Calendar.MONTH, 1);
                break;
            case "Daily:Ksh 400/=":
                calendar.add(Calendar.DATE, 1);
                break;
        }
        Date newGymExpiryDate = calendar.getTime();

        memberRef.document(member.getUserID()).update("gymExpiryDate", newGymExpiryDate);
        member.setGymExpiryDate(newGymExpiryDate);
        gymExpiryDate.setText(format.format(newGymExpiryDate));

        createTransactionTicket(GYM_PAYMENT, newGymExpiryDate);
    }

    private void createTransactionTicket(String paymentType, Date newExpiryDate) {

        Transaction transaction = new Transaction("", Calendar.getInstance().getTime(),
                paymentType, newExpiryDate, true, member.getMemberType(),
                member.getFullName());

        transactionRef.add(transaction).addOnCompleteListener(task -> {
            // TODO close pDialog
            // TODO display complete message
            pay.setEnabled(true);
            Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show();
            Utils.updateCurrentMember(member);
        });

    }

    private String getSubMemberType() {
        switch (member.getMemberType()) {
            case ORDINARY:
            case UPCOUNTRY:
                return FULL_MEMBER;
            case LADY:
                return LADY;
            case JUNIOR:
                return JUNIOR;
            default:
                return "";
        }
    }

    private void getSubscription(String subMemberType) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(member.getMemberExpiryDate().getTime());

        int subsYear = calendar.get(Calendar.YEAR);

        subsRef.whereEqualTo("subsYear", subsYear)
                .whereEqualTo("memberType", subMemberType)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                subscription = task.getResult().getDocuments().get(0).toObject(Subscription.class);
                assert subscription != null;
                amountDue.setText(String.format("Ksh %s/=", subscription.getSubsTotal()));

                statement.setVisibility(View.VISIBLE);
            } else {
                amountDue.setText(R.string.zero_amount);
                statement.setVisibility(View.GONE);
            }
        });

    }

    private void setGymAmount() {
        gymAmountsHash = new LinkedHashMap<>();
        gymAmountsHash.put("Please select (optional)", (double) 0);
        gymAmountsHash.put("Annual:Ksh 28,000/= (Single)", (double) 28000);
        gymAmountsHash.put("Annual:Ksh 40,000/= (Couple)", (double) 40000);
        gymAmountsHash.put("Semi Annual: Ksh 16,000/= (Single)", (double) 16000);
        gymAmountsHash.put("Semi Annual: Ksh 24,000/= (Couple) ", (double) 24000);
        gymAmountsHash.put("Quarterly:Ksh 9,000/= (Single and Over 18 only)", (double) 9000);
        gymAmountsHash.put("Monthly:Ksh 3,500/= (Single Only)", (double) 3500);
        gymAmountsHash.put("Daily:Ksh 400/=", (double) 400);

        setSpinnerAdapterList(gymAmountsHash);
    }

    private void setSpinnerAdapterList(HashMap<String, Double> gymAmountsHash) {
        String[] gymOption = new String[gymAmountsHash.size()];
        int i = 0;
        for (String key : gymAmountsHash.keySet()) {
            gymOption[i] = key;
            i++;
        }

        gymAmountSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, gymOption));
    }

    @Override
    public void onBackPressed() {
        if (member.getAccountStatus() == MemberStatus.Defaulted) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        } else {
            finish();
        }
    }
}