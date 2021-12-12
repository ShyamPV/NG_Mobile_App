package com.shyam.ngmobile;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.shyam.ngmobile.Enums.MemberStatus;
import com.shyam.ngmobile.Model.Member;
import com.shyam.ngmobile.Model.Subscription;
import com.shyam.ngmobile.Model.Transaction;
import com.shyam.ngmobile.Services.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class PaymentActivity extends AppCompatActivity {

    private static final String FULL_MEMBER = "Full Member";
    private static final String ORDINARY = "Ordinary Member";
    private static final String LADY = "Lady Member";
    private static final String JUNIOR = "Junior Member";
    private static final String UPCOUNTRY = "Upcountry Member";
    private static final String GYM_PAYMENT = "Gym Subscription Renewal";
    private static final String SUBS_PAYMENT = "Membership Subs Payment";
    private static final String CARD_PAYMENT = "Prepaid Card Recharge";
    private static final String PAYBILL = "542542";
    private static final String SUBS_ACC = "000550";
    private static final String PREPAID_ACC = "000551";
    private static final double REINSTATEMENT = 5000.0;
    private Date reinstatementDate;
    private String paymentOption;


    private EditText expiryDateText;
    private EditText gymExpiryDate;
    private EditText amountDue;
    private EditText dialogPhoneNumberText;
    private EditText dialogAmountText;
    private Spinner gymAmountSpinner;
    private Button statement, pay, btn_pay_subs, btn_pay_gym, btn_pay_card;
    private SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
    private Member member;
    private ArrayList<Subscription> subscriptionList;
    private double subsTotal;
    private SweetAlertDialog pDialog;

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

    // Setup Methods---------------------------------------------------------------------------------
    private void setup() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        subsRef = db.collection("subscription");
        memberRef = db.collection("member");
        transactionRef = db.collection("transaction");

        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setTitle("Loading...");
        pDialog.getProgressHelper().setBarColor(ContextCompat.getColor(this, R.color.ng_blue));
        pDialog.setCancelable(false);
        pDialog.show();


        member = Utils.getCurrentMember();
        reinstatementDate = getReinstatementDate();

        if (isDefaulted())
            Utils.displayMessage(this, "", "Your account has defaulted" +
                    "\nKindly pay your subscriptions.");

        String subMemberType = getSubMemberType();

        EditText memberTypeText = findViewById(R.id.payment_member_type);
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
            showPaymentDialog();
        });

        statement.setOnClickListener(view -> {
            Utils.generateMemberStatement(member);
        });

        if (!subMemberType.equals("")) {
            getSubscription(subMemberType);
        }

        pDialog.dismiss();
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
        
        subscriptionList = new ArrayList<>();
        subsTotal = 0;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(member.getMemberExpiryDate().getTime());

        int subsYear = calendar.get(Calendar.YEAR);

        subsRef.whereGreaterThanOrEqualTo("subsYear", subsYear)
                .whereEqualTo("memberType", subMemberType)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {

                for (QueryDocumentSnapshot snapshot : task.getResult()) {
                    Subscription subscription = snapshot.toObject(Subscription.class);
                    subscriptionList.add(subscription);
                    subsTotal += subscription.getSubsTotal();
                }

                if (isDefaulted()) {
                    amountDue.setText(String.format("Ksh %s/=",
                            subsTotal + REINSTATEMENT));
                } else {
                    amountDue.setText(String.format("Ksh %s/=", subsTotal));
                }

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
        if (member.getMemberType().equals(JUNIOR)) {
            gymAmountsHash.put("Annual:Ksh 28,000/= (Junior)", (double) 28000);
        }
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

    private boolean isDefaulted() {
        Calendar calendar = Calendar.getInstance();
        return member.getAccountStatus() == MemberStatus.Defaulted
                && calendar.getTime().after(reinstatementDate);
    }

    private Date getReinstatementDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(member.getMemberExpiryDate());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, Calendar.APRIL);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        return calendar.getTime();
    }


    // End Setup Methods----------------------------------------------------------------------------

    // Dialog Methods-------------------------------------------------------------------------------

    private void showPaymentDialog() {
        paymentOption = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_payment_option, null);

        setupPaymentDialogView(view);

        builder.setView(view);
        builder.setTitle("Make Mpesa Payment");
        builder.setMessage("Please select the payment option.");
        builder.setNeutralButton("Cancel", (dialog, i) -> {
            dialog.dismiss();
        });

        builder.setPositiveButton("Confirm", (dialog, i) -> {
            if (isValidInput()) {
                double amount = Double.parseDouble(dialogAmountText.getText().toString());
                String phonenumber = dialogPhoneNumberText.getText().toString().trim();
                processPayment(amount, paymentOption, phonenumber);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
        Button PB = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button NB = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        PB.setTextSize(18);
        PB.setTypeface(Typeface.DEFAULT_BOLD);
        PB.setBackgroundColor(ContextCompat.getColor(this, R.color.ng_blue));
        PB.setTextColor(ContextCompat.getColor(this, R.color.white));
        NB.setTextSize(18);
        NB.setTypeface(Typeface.DEFAULT_BOLD);
        NB.setBackgroundColor(ContextCompat.getColor(this, R.color.ng_error_red));
        NB.setTextColor(ContextCompat.getColor(this, R.color.white));

    }

    private void setupPaymentDialogView(View view) {

        dialogPhoneNumberText = view.findViewById(R.id.payment_dialog_phone);
        dialogAmountText = view.findViewById(R.id.payment_dialog_amount);
        btn_pay_subs = view.findViewById(R.id.payment_dialog_subs);
        btn_pay_gym = view.findViewById(R.id.payment_dialog_gym);
        btn_pay_card = view.findViewById(R.id.payment_dialog_card);

        dialogPhoneNumberText.setText(member.getPhoneNumber());

        if (subsTotal != 0) {
            btn_pay_subs.setOnClickListener(button -> {
                setupSubsButton();
            });
        } else {
            btn_pay_subs.setVisibility(View.GONE);
        }

        if (gymAmountSpinner.getSelectedItemPosition() != 0) {
            btn_pay_gym.setOnClickListener(button -> {
                paymentOption = GYM_PAYMENT;
                double gymAmount = gymAmountsHash.get(gymAmountSpinner.getSelectedItem().toString());
                dialogAmountText.setText(String.valueOf(gymAmount));
                dialogAmountText.setEnabled(false);
                setPaymentOption(btn_pay_gym);
            });
        } else {
            btn_pay_gym.setVisibility(View.GONE);
        }

        btn_pay_card.setOnClickListener(button -> {
            setupCardPaymentButton();
        });

        // Defaulted Members MUST Clear Subs
        if (member.getAccountStatus() == MemberStatus.Defaulted) {
            setupSubsButton();
        }

        // if both subs button & gym button are invisible
        if (btn_pay_subs.getVisibility() == View.GONE && btn_pay_gym.getVisibility() == View.GONE) {
            setupCardPaymentButton();
            btn_pay_card.setText(R.string.card_recharge_text);
        }
    }

    private void setupSubsButton() {
        paymentOption = SUBS_PAYMENT;
        if (isDefaulted()) {
            dialogAmountText.setText(String
                    .valueOf(subsTotal + REINSTATEMENT));
            btn_pay_gym.setVisibility(View.GONE);
            btn_pay_card.setVisibility(View.GONE);
        } else {
            dialogAmountText.setText(String.valueOf(subsTotal));
        }
        dialogAmountText.setEnabled(false);
        setPaymentOption(btn_pay_subs);
    }

    private void setupCardPaymentButton() {
        paymentOption = CARD_PAYMENT;
        dialogAmountText.setText("");
        dialogAmountText.setEnabled(true);
        setPaymentOption(btn_pay_card);
    }

    private boolean isValidInput() {
        boolean isValid = true;
        String message = "";
        double amount;

        try {
            amount = Double.parseDouble(dialogAmountText.getText().toString());
        } catch (Exception e) {
            amount = 0;
        }

        if (dialogPhoneNumberText.getText().toString().length() != 10) {
            message += "Please enter a valid phone number\n";
        }

        if (paymentOption == null) {
            message += "Please selected the type of payment you want to make\n";
        }

        if (amount == 0) {
            message += "Amount can not be 0";
        }


        if (!message.equals("")) {
            isValid = false;
            Utils.displayMessage(this, "Error!", message);
        }

        return isValid;
    }

    //Change background colour for
    // the selected payment type in dialog
    private void setPaymentOption(Button button) {
        btn_pay_subs.setBackground(ContextCompat.getDrawable(this, R.drawable.primary_button_blue));
        btn_pay_gym.setBackground(ContextCompat.getDrawable(this, R.drawable.primary_button_blue));
        btn_pay_card.setBackground(ContextCompat.getDrawable(this, R.drawable.primary_button_blue));

        btn_pay_subs.setEnabled(true);
        btn_pay_gym.setEnabled(true);
        btn_pay_card.setEnabled(true);

        button.setBackground(ContextCompat.getDrawable(this, R.drawable.primary_button_red));
        button.setEnabled(false);
    }

    // End Dialog Methods---------------------------------------------------------------------------

    // Make M-Pesa payment and create transaction ticket--------------------------------------------
    private void processPayment(double amount, String paymentType, String phoneNumber) {
        pDialog.setTitle("Making Payment...");
        pDialog.show();

        String mpesaAccountRef;
        if (paymentType.equals(SUBS_PAYMENT)) {
            mpesaAccountRef = SUBS_ACC + "#" + member.getMembershipNo();
        } else {
            mpesaAccountRef = PREPAID_ACC + "#" + member.getMembershipNo();
        }
        // TODO Payment via mpesa


        // TODO if payment Successful
        updateDates(paymentType, amount);
    }

    private void updateDates(String paymentType, double amount) {
        if (paymentType.equals(SUBS_PAYMENT)) {
            updateMemberExpiryDate(amount);
        } else if (paymentType.equals(GYM_PAYMENT)) {
            updateGymExpiryDate(amount);
        } else {
            createTransactionTicket(CARD_PAYMENT, null, amount);
        }
    }

    private void updateMemberExpiryDate(double amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(member.getMemberExpiryDate());
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + subscriptionList.size());
        Date newMemberExpiryDate = calendar.getTime();

        memberRef.document(member.getUserID()).update("memberExpiryDate", newMemberExpiryDate,
                "accountStatus", MemberStatus.Active);
        member.setMemberExpiryDate(newMemberExpiryDate);
        member.setAccountStatus(MemberStatus.Active);
        expiryDateText.setText(format.format(newMemberExpiryDate));

        createTransactionTicket(SUBS_PAYMENT, newMemberExpiryDate, amount);

        if (!getSubMemberType().equals("")) {
            getSubscription(getSubMemberType());
        }
    }

    private void updateGymExpiryDate(double amount) {
        Calendar calendar = Calendar.getInstance();
        if (member.getGymExpiryDate().after(calendar.getTime()))
            calendar.setTime(member.getGymExpiryDate());

        switch (gymAmountSpinner.getSelectedItem().toString()) {
            case "Annual:Ksh 28,000/= (Single)":
            case "Annual:Ksh 40,000/= (Couple)":
            case "Annual:Ksh 28,000/= (Junior)":
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

        createTransactionTicket(GYM_PAYMENT, newGymExpiryDate, amount);
    }

    private void createTransactionTicket(String paymentType, Date newExpiryDate, double amount) {

        Transaction transaction = new Transaction("", Calendar.getInstance().getTime(),
                paymentType, newExpiryDate, true, member.getUserID(),
                member.getMembershipNo(), member.getFullName(), amount);

        transactionRef.add(transaction).addOnCompleteListener(task -> {
            if (pDialog != null) pDialog.dismiss();
            Utils.displayMessage(this, "Success", "Payment done successfully");
            Utils.updateCurrentMember(member);
        });

    }

    //End Make M-Pesa payment and create transaction ticket-----------------------------------------

    @Override
    public void onBackPressed() {
        if (member.getAccountStatus() == MemberStatus.Defaulted) {
            Utils.logoutUser(this);
        } else {
            Utils.gotoActivity(this, MainActivity.class);
        }
    }
}