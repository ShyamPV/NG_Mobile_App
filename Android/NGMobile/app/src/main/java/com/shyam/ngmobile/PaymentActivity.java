package com.shyam.ngmobile;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shyam.ngmobile.Enums.MemberStatus;
import com.shyam.ngmobile.Model.Member;
import com.shyam.ngmobile.Model.Subscription;
import com.shyam.ngmobile.Model.Transaction;
import com.shyam.ngmobile.Services.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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
    private Date today;

    private EditText expiryDateText;
    private EditText gymExpiryDate;
    private EditText amountDue;
    private EditText dialogPhoneNumberText;
    private EditText dialogAmountText;
    private Spinner gymAmountSpinner;
    private Button statement, pay, btn_pay_subs, btn_pay_gym, btn_pay_card;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
    private final SimpleDateFormat yearFormatter = new SimpleDateFormat("yyyy", Locale.ENGLISH);
    private final DecimalFormat decimalFormatter = new DecimalFormat("###,###,###.00");

    private CollectionReference subsRef;
    private CollectionReference memberRef;
    private CollectionReference transactionRef;
    private LinkedHashMap<String, Double> gymAmountsHash;
    private String folderPath;
    private Member member;
    private Subscription subscription;
    private SweetAlertDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        setup();
    }

    // Setup Methods--------------------------------------------------------------------------------
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
        today = Calendar.getInstance().getTime();


        if (today.after(member.getMemberExpiryDate())) {
            Utils.displayMessage(this, "", "Your account has defaulted" +
                    "\nKindly pay your subscriptions.");
        }

        String subMemberType = getSubMemberType();

        EditText memberTypeText = findViewById(R.id.payment_member_type);
        expiryDateText = findViewById(R.id.payment_member_expiry_date);
        gymExpiryDate = findViewById(R.id.payment_gym_expiry);
        amountDue = findViewById(R.id.payment_member_amount);
        pay = findViewById(R.id.btn_make_payment);
        statement = findViewById(R.id.generate_statement);
        gymAmountSpinner = findViewById(R.id.gym_amount_spinner);

        memberTypeText.setText(member.getMemberType());
        expiryDateText.setText(dateFormatter.format(member.getMemberExpiryDate()));
        if (member.getGymExpiryDate() != null)
            gymExpiryDate.setText(dateFormatter.format(member.getGymExpiryDate()));

        setGymAmount();

        pay.setOnClickListener(view -> {
            showPaymentDialog();
        });

        statement.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                generateMemberStatement(member, subscription);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
            }
        });

        if (!subMemberType.equals("")) {
            getSubscription(subMemberType);
        }

        pDialog.dismiss();
    }

    // Subscription---------------------------------------------------------------------------------
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
        subscription = null;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(member.getMemberExpiryDate().getTime());

        int subsYear = calendar.get(Calendar.YEAR);

        subsRef.whereEqualTo("subsYear", subsYear)
                .whereEqualTo("memberType", subMemberType)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {

                subscription = task.getResult().getDocuments().get(0).toObject(Subscription.class);

                if (isDefaulted()) {
                    amountDue.setText(String.format("Ksh %s/=",
                            subscription.getSubsTotal() + REINSTATEMENT));
                } else {
                    amountDue.setText(String.format("Ksh %s/=", subscription.getSubsTotal()));
                }

                statement.setVisibility(View.VISIBLE);
            } else {
                amountDue.setText(R.string.zero_amount);
                statement.setVisibility(View.GONE);
            }
        });

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
    // End Subscription-----------------------------------------------------------------------------

    // GYM------------------------------------------------------------------------------------------
    private void setGymAmount() {
        gymAmountsHash = new LinkedHashMap<>();
        gymAmountsHash.put("Please select (optional)", (double) 0);

        if (isAnnualGymAvailable(today)) {
            gymAmountsHash.put("Annual:Ksh 28,000/= (Single)", (double) 28000);
            gymAmountsHash.put("Annual:Ksh 40,000/= (Couple)", (double) 40000);
            if (member.getMemberType().equals(JUNIOR)) {
                gymAmountsHash.put("Annual:Ksh 28,000/= (Junior)", (double) 28000);
            }
        }

        if (isSemiAnnualGymAvailable(today)) {
            gymAmountsHash.put("Semi Annual: Ksh 16,000/= (Single)", (double) 16000);
            gymAmountsHash.put("Semi Annual: Ksh 24,000/= (Couple) ", (double) 24000);
        }

        if (isQuarterlyGymAvailable(today)) {
            gymAmountsHash.put("Quarterly:Ksh 9,000/= (Single and Over 18 only)", (double) 9000);
        }

        if (isMonthlyGymAvailable(today)) {
            gymAmountsHash.put("Monthly:Ksh 3,500/= (Single Only)", (double) 3500);
        }

        gymAmountsHash.put("Daily:Ksh 400/=", (double) 400);

        setSpinnerAdapterList(gymAmountsHash);
    }

    private boolean isAnnualGymAvailable(Date today) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);

        return today.after(calendar.getTime());
    }

    private boolean isSemiAnnualGymAvailable(Date today) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, 30);
        calendar.set(Calendar.MONTH, Calendar.JUNE);

        return today.before(calendar.getTime());
    }

    private boolean isQuarterlyGymAvailable(Date today) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, 30);
        calendar.set(Calendar.MONTH, Calendar.SEPTEMBER);

        return today.before(calendar.getTime());
    }

    private boolean isMonthlyGymAvailable(Date today) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);

        return today.before(calendar.getTime());
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

    // End GYM--------------------------------------------------------------------------------------


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

        if (subscription != null) {
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
                    .valueOf(subscription.getSubsTotal() + REINSTATEMENT));
            btn_pay_gym.setVisibility(View.GONE);
            btn_pay_card.setVisibility(View.GONE);
        } else {
            dialogAmountText.setText(String.valueOf(subscription.getSubsTotal()));
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
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
        Date newMemberExpiryDate = calendar.getTime();

        memberRef.document(member.getUserID()).update("memberExpiryDate", newMemberExpiryDate,
                "accountStatus", MemberStatus.Active);
        member.setMemberExpiryDate(newMemberExpiryDate);
        member.setAccountStatus(MemberStatus.Active);
        expiryDateText.setText(dateFormatter.format(newMemberExpiryDate));

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
                calendar.set(Calendar.DATE, 31);
                calendar.set(Calendar.MONTH, Calendar.DECEMBER);
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
        gymExpiryDate.setText(dateFormatter.format(newGymExpiryDate));

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

    // Generate PDF Document------------------------------------------------------------------------
    private void generateMemberStatement(Member member, Subscription subscription) {

        int pageWidth = 1000;
        int pageHeight = 1414;

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument
                .PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();

        pdfNGLogo(canvas);
        pdfNGAddress(canvas);
        pdfMemberDetails(member, canvas);
        pdfSubsAmount(subscription, canvas);
        pdfSubsDetail(member, canvas);


        document.finishPage(page);
        folderPath = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/Documents/Nairobi Gymkhana/";
        File savePath = new File(folderPath);

        File file = new File(savePath,
                String.format("Statement - %s.pdf", member.getMembershipNo()));

        try {
            savePath.mkdirs();
            document.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "Saved to:" + file.toString(), Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            Log.e("Generate Statement:", e.getMessage());
            e.printStackTrace();
        }

        document.close();

    }

    private void pdfNGLogo(Canvas canvas) {
        Bitmap logoImage = BitmapFactory.decodeResource(getResources(),
                R.drawable.ng_logo_pdf);
        Bitmap scaledLogo = Bitmap.createScaledBitmap(logoImage, 124, 165, true);

        Paint imagePaint = new Paint();
        int left = 780;
        int top = 50;
        canvas.drawBitmap(scaledLogo, left, top, imagePaint);
    }

    private void pdfNGAddress(Canvas canvas) {
        Paint ngAddressPaint = new Paint();
        ngAddressPaint.setTextSize(24);

        int ngAddressXPosition = 70;
        int ngAddressYPosition = 75;
        int ngAddressXEndPosition = 740;
        int ngAddressYEndPosition = 275;

        canvas.drawText("Nairobi Gymkhana", ngAddressXPosition, ngAddressYPosition, ngAddressPaint);
        canvas.drawText("P. O. Box 40895 - 00100", ngAddressXPosition, ngAddressYPosition += 30, ngAddressPaint);
        canvas.drawText("0727531457/8", ngAddressXPosition, ngAddressYPosition += 30, ngAddressPaint);
        canvas.drawText("info@nairobigymkhana.com", ngAddressXPosition, ngAddressYPosition + 30, ngAddressPaint);

        canvas.drawText("Annual Statement", ngAddressXEndPosition, ngAddressYEndPosition, ngAddressPaint);
    }

    private void pdfMemberDetails(Member member, Canvas canvas) {
        Paint ngMemberTextPaint = new Paint();
        ngMemberTextPaint.setTextSize(24);
        ngMemberTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        int ngMemberXPosition = 70;
        int ngMemberYPosition = 250;
        int ngMemberXEndPosition = 720;

        canvas.drawText("To: " + member.getFullName() + " - " + member.getMembershipNo(),
                ngMemberXPosition - 35, ngMemberYPosition, ngMemberTextPaint);
        canvas.drawText("P. O. Box " + member.getPostAddress() + " - " + member.getZipCode(),
                ngMemberXPosition, ngMemberYPosition += 30, ngMemberTextPaint);
        canvas.drawText(member.getCity(), ngMemberXPosition, ngMemberYPosition += 30,
                ngMemberTextPaint);
        canvas.drawText(member.getCountry(), ngMemberXPosition,
                ngMemberYPosition += 30, ngMemberTextPaint);
        canvas.drawText(member.getEmail(), ngMemberXPosition, ngMemberYPosition + 80,
                ngMemberTextPaint);
        if (!isDefaulted()) {
            canvas.drawText("Due By: " + dateFormatter.format(member.getMemberExpiryDate()),
                    ngMemberXEndPosition, ngMemberYPosition + 80, ngMemberTextPaint);
        }

    }

    private void pdfSubsAmount(Subscription subscription, Canvas canvas) {
        Paint ngDetailsPaint = new Paint();
        ngDetailsPaint.setTextSize(24);

        Paint linePaint = new Paint();
        linePaint.setStrokeWidth(2);

        double sportLevyAmount = subscription.getSportsLevy() + (subscription.getSportsLevy()
                * subscription.getSportsLevyVAT() / 100);

        canvas.drawText("For: Club Subscriptions "
                        + yearFormatter.format(member.getMemberExpiryDate()), 70, 520,
                ngDetailsPaint);
        canvas.drawText("Date: " + dateFormatter.format(Calendar.getInstance().getTime()),
                740, 520, ngDetailsPaint);

        canvas.drawLine(70, 550, 930, 550, linePaint);

        int tableX = 100;
        int tableXEnd = 900;
        int tableYStart = 570;
        int tableY = 570;

        ngDetailsPaint.setTypeface(Typeface.DEFAULT_BOLD);

        // Table titles
        canvas.drawLine(tableX, tableY, tableXEnd, tableY, linePaint);
        tableY += 30;
        canvas.drawText("Reference", tableX + 150, tableY, ngDetailsPaint);
        canvas.drawText("Amount", tableX + 600, tableY, ngDetailsPaint);
        ngDetailsPaint.setTypeface(Typeface.DEFAULT);
        tableY += 10;
        canvas.drawLine(tableX, tableY, tableXEnd, tableY, linePaint);
        tableY += 30;
        canvas.drawText("Subscriptions - " + subscription.getSubsYear(),
                tableX + 15, tableY, ngDetailsPaint);
        canvas.drawText(decimalFormatter.format(subscription.getSubsAmount()),
                tableX + 615, tableY, ngDetailsPaint);
        tableY += 10;
        canvas.drawLine(tableX, tableY, tableXEnd, tableY, linePaint);
        tableY += 30;
        canvas.drawText("Sports Levy - " + subscription.getSubsYear(),
                tableX + 15, tableY, ngDetailsPaint);
        canvas.drawText(decimalFormatter.format(sportLevyAmount),
                tableX + 615, tableY, ngDetailsPaint);
        tableY += 10;
        canvas.drawLine(tableX, tableY, tableXEnd, tableY, linePaint);
        if (isDefaulted()) {
            tableY += 30;
            canvas.drawText("Reinstatement Fees",
                    tableX + 15, tableY, ngDetailsPaint);
            canvas.drawText(decimalFormatter.format(REINSTATEMENT),
                    tableX + 615, tableY, ngDetailsPaint);
            tableY += 10;
            canvas.drawLine(tableX, tableY, tableXEnd, tableY, linePaint);
        }
        //Vertical Lines
        canvas.drawLine(tableX, tableYStart, tableX, tableY, linePaint);
        canvas.drawLine(tableX + 500, tableYStart, tableX + 500, tableY, linePaint);
        canvas.drawLine(tableXEnd, tableYStart, tableXEnd, tableY, linePaint);

        canvas.drawLine(70, 850, 930, 850, linePaint);

        tableX = 500;
        tableY = 880;

        //Totals row
        canvas.drawLine(tableX, tableY, tableXEnd, tableY, linePaint);
        tableY += 30;
        ngDetailsPaint.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText("Total:", tableX + 70, tableY, ngDetailsPaint);
        ngDetailsPaint.setTypeface(Typeface.DEFAULT);
        if (isDefaulted()) {
            canvas.drawText(decimalFormatter.format(subscription.getSubsTotal() + REINSTATEMENT),
                    tableX + 250, tableY, ngDetailsPaint);
        } else {
            canvas.drawText(decimalFormatter.format(subscription.getSubsTotal()),
                    tableX + 250, tableY, ngDetailsPaint);
        }

        tableY += 10;
        canvas.drawLine(tableX, tableY, tableXEnd, tableY, linePaint);

        // total row vertical lines
        canvas.drawLine(tableX, 880, tableX, tableY, linePaint);
        canvas.drawLine(tableX + 200, 880, tableX + 200, tableY, linePaint);
        canvas.drawLine(tableXEnd, 880, tableXEnd, tableY, linePaint);
    }

    private void pdfSubsDetail(Member member, Canvas canvas) {

        Paint paint = new Paint();
        paint.setTextSize(20);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        String defaulterText = "Defaulters will be charged Reinstatement fee of Kshs 5,000/-";

        if (member.getMemberType().equals(JUNIOR)) {
            int xPoint = 100;
            int yPoint = 1000;
            String text1 = "NB: THE SUBSCRIPTIONS FOR JUNIOR MEMBERS BEYOND THE AGE OF 21 YEARS WILL";
            String text2 = "ONLY BE RENEWED PROVIDED THAT THE JUNIOR MEMBER IS A FULL-TIME STUDENT AT";
            String text3 = "A RECOGNIZED SCHOOL, COLLAGE OR UNIVERSITY AND WILL BE REQUIRED TO PROVIDE";
            String text4 = "DOCUMENTARY EVIDENCE SHOWING CLEARLY THE STUDIES BEING PURSUED AND THE ";
            String text5 = "DURATION OF THE COURSE.";

            String text6 = "NB: JUNIOR MEMBER ARE REQUIRED TO PRESENT THEIR SCHOOL ID WITH EXPIRY DATE,";
            String text7 = "CURRENT YEAR FEE NOTE/LETTER FROM THIER INSTITUTE OF EDUCATION INDICATING THAT";
            String text8 = "AS FULL TIME STUDENTS.";

            canvas.drawText(text1, xPoint, yPoint, paint);
            canvas.drawText(text2, xPoint, yPoint += 30, paint);
            canvas.drawText(text3, xPoint, yPoint += 30, paint);
            canvas.drawText(text4, xPoint, yPoint += 30, paint);
            canvas.drawText(text5, xPoint, yPoint += 30, paint);
            canvas.drawText(text6, xPoint, yPoint += 60, paint);
            canvas.drawText(text7, xPoint, yPoint += 30, paint);
            canvas.drawText(text8, xPoint, yPoint + 30, paint);
        }
        if (member.getMemberType().equals(LADY)) {
            int xPoint = 150;
            int yPoint = 1000;
            String text1 = "KINDLY PRESENT YOUR MEMBERSHIP CARD AT THE TIME OF PAYMENT";
            canvas.drawText(text1, xPoint, yPoint, paint);
        }
        if (member.getMemberType().equals(ORDINARY) || member.getMemberType().equals(UPCOUNTRY)) {
            int xPoint = 100;
            int yPoint = 1000;
            String text1 = "KINDLY PRESENT YOUR AND SPOUSE MEMBERSHIP CARDS AT THE TIME OF PAYMENT";
            canvas.drawText(text1, xPoint, yPoint, paint);
        }

        canvas.drawText(defaulterText, 250, 1300, paint);

    }

    // End Generate PDF Document--------------------------------------------------------------------

    @Override
    public void onBackPressed() {
        if (member.getAccountStatus() == MemberStatus.Defaulted) {
            Utils.logoutUser(this);
        } else {
            Utils.gotoActivity(this, MainActivity.class);
        }
    }
}