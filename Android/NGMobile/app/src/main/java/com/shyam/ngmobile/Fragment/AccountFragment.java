package com.shyam.ngmobile.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.shyam.ngmobile.Enums.MemberStatus;
import com.shyam.ngmobile.Model.Member;
import com.shyam.ngmobile.Model.Subscription;
import com.shyam.ngmobile.R;
import com.shyam.ngmobile.Services.Utils;
import com.shyam.ngmobile.ViewPDFActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AccountFragment extends Fragment {

    private static final String MEMBER_ID = "MEMBER_ID";
    private static final String FULL_MEMBER = "Full Member";
    private static final String ORDINARY = "Ordinary Member";
    private static final String LADY = "Lady Member";
    private static final String JUNIOR = "Junior Member";
    private static final String UPCOUNTRY = "Upcountry Member";
    private FirebaseAuth mAuth;
    private CollectionReference memberRef;
    private StorageReference certificateStorage;

    private View view;
    private Member member;
    private Button btnMyWallet, btnUpdateProfile, btnViewVaccineCertificate;
    private EditText postAddressText, cityText, countryText, zipCodeText, phoneNumberText, memberNoText,
            memberTypeText, memberExpiryDate, emailText, passwordText, confirmPasswordText;
    private TextView memberName;
    private SweetAlertDialog pDialog;
    String folderPath = Environment.DIRECTORY_DOCUMENTS + File.separator + "Nairobi Gymkhana";

    // Remove this after payment
    private Subscription subscription;
    private CollectionReference subsRef;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
    private final SimpleDateFormat yearFormatter = new SimpleDateFormat("yyyy", Locale.ENGLISH);
    private final DecimalFormat decimalFormatter = new DecimalFormat("###,###,###.00");
    private static final double REINSTATEMENT = 5000.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull @org.jetbrains.annotations.NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_account, container, false);
        pDialog = new SweetAlertDialog(view.getContext(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setTitle("Loading");
        pDialog.getProgressHelper().setBarColor(ContextCompat.getColor(requireContext(), R.color.ng_blue));
        pDialog.setCancelable(true);
        pDialog.show();

        member = Utils.getCurrentMember();

        if (member != null) {
            setup();
        } else {
            Utils.logoutUser(requireActivity());
        }

        return view;
    }

    private void setup() {

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        memberRef = db.collection("member");
        certificateStorage = FirebaseStorage.getInstance().getReference("certificate");

        // TODO Remove this after payment is implemented
        subsRef = db.collection("subscription");

        // TODO this button will create statement util payment is implemented
        btnMyWallet = view.findViewById(R.id.member_wallet);

        if (member.getMemberType().equals(ORDINARY) ||
                member.getMemberType().equals(LADY) ||
                member.getMemberType().equals(JUNIOR) ||
                member.getMemberType().equals(UPCOUNTRY)) {
            btnMyWallet.setText(R.string.generate_statement);

            if (!getSubMemberType().equals("")) {
                getSubscription(getSubMemberType());
            }

            btnMyWallet.setOnClickListener(view1 -> {
                // TODO change to open payment activity once payment is sorted
//            Intent intent = new Intent(getContext(), PaymentActivity.class);
//            intent.putExtra(MEMBER_ID, member.getUserID());
//            startActivity(intent);
                try {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(requireContext(), "Please allow storage access in order to save statement", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    } else if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        Utils.displayMessage(requireActivity(), "Error!", "Can not save statement.\nPlease allow storage permission");
                    } else {
                        generateMemberStatement(member, subscription);
                    }

                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Could not create the statement. Please contact the club.", Toast.LENGTH_LONG).show();
                }

            });
        } else {
            btnMyWallet.setVisibility(View.GONE);
        }

        btnUpdateProfile = view.findViewById(R.id.member_update_profile);
        btnUpdateProfile.setOnClickListener(view -> {
            if (validInput()) {
                pDialog = new SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE);
                pDialog.getProgressHelper().setBarColor(ContextCompat.getColor(requireContext(), R.color.ng_blue));
                pDialog.setCancelable(true);
                pDialog.setTitle("Updating...");
                pDialog.show();
                updateMemberDetails();
            }
        });


        memberName = view.findViewById(R.id.member_name);
        postAddressText = view.findViewById(R.id.member_address_post_address);
        cityText = view.findViewById(R.id.member_address_city);
        countryText = view.findViewById(R.id.member_address_country);
        zipCodeText = view.findViewById(R.id.member_address_post_code);
        phoneNumberText = view.findViewById(R.id.member_address_phone);
        memberNoText = view.findViewById(R.id.member_membership_no);
        memberTypeText = view.findViewById(R.id.member_membership_type);
        memberExpiryDate = view.findViewById(R.id.member_membership_expiry_date);
        emailText = view.findViewById(R.id.member_email);
        passwordText = view.findViewById(R.id.member_password);
        confirmPasswordText = view.findViewById(R.id.member_confirm_password);

        memberName.setText(member.getFullName());
        postAddressText.setText(member.getPostAddress());
        cityText.setText(member.getCity());
        countryText.setText(member.getCountry());
        zipCodeText.setText(member.getZipCode());
        phoneNumberText.setText(member.getPhoneNumber());
        memberNoText.setText(member.getMembershipNo());
        memberExpiryDate.setText(dateFormatter.format(member.getMemberExpiryDate()));
        memberTypeText.setText(member.getMemberType());
        emailText.setText(member.getEmail());

        btnViewVaccineCertificate = view.findViewById(R.id.upload_view_certificate);
        validateUploadedCertificate();
    }

    private void updateMemberDetails() {
        String postAddress, city, country, zipCode, phoneNumber;

        postAddress = postAddressText.getText().toString().trim();
        city = cityText.getText().toString().trim();
        country = countryText.getText().toString().trim();
        zipCode = zipCodeText.getText().toString().trim();
        phoneNumber = phoneNumberText.getText().toString().trim();

        HashMap<String, Object> updateMember = new HashMap<>();
        updateMember.put("phoneNumber", phoneNumber);
        updateMember.put("postAddress", postAddress);
        updateMember.put("zipCode", zipCode);
        updateMember.put("city", city);
        updateMember.put("country", country);
        updateMember.put("firstTimeLogin", false);

        memberRef.document(member.getUserID()).update(updateMember).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                member.setPostAddress(postAddress);
                member.setCity(city);
                member.setCountry(country);
                member.setZipCode(zipCode);
                member.setPhoneNumber(phoneNumber);
                member.setFirstTimeLogin(false);

                if (!passwordText.getText().toString().isEmpty()) {
                    updatePassword();
                } else {
                    dismissDialog();
                    Utils.displayMessage(requireActivity(), "Success",
                            "Your Details have been updated.");
                    Utils.updateCurrentMember(member);
                }
            } else {
                dismissDialog();
                Utils.displayMessage(requireActivity(), "Error!",
                        "Sorry, Your Details have not been updated.\n" +
                                "Please try later.");
            }
        });
    }

    private void updatePassword() {
        String password = passwordText.getText().toString();
        FirebaseUser user = mAuth.getCurrentUser();

        assert user != null;
        user.updatePassword(password).addOnCompleteListener(task -> {
            dismissDialog();
            if (task.isSuccessful()) {
                Utils.displayMessage(requireActivity(), "Success",
                        "Your Details have been updated");
                Utils.updateCurrentMember(member);
            }
        });

    }

    private void dismissDialog() {
        if (pDialog != null) pDialog.dismiss();
    }

    private boolean validInput() {
        boolean valid = true;

        String postAddress, city, country, zipCode, password, confirmPassword;

        postAddress = postAddressText.getText().toString().trim();
        city = cityText.getText().toString().trim();
        country = countryText.getText().toString().trim();
        zipCode = zipCodeText.getText().toString().trim();
        password = passwordText.getText().toString().trim();
        confirmPassword = confirmPasswordText.getText().toString().trim();


        if (postAddress.isEmpty()) {
            valid = false;
            postAddressText.setError("Please enter post address");
        }

        if (city.isEmpty()) {
            valid = false;
            cityText.setError("Please enter city");
        }

        if (country.isEmpty()) {
            valid = false;
            countryText.setError("Please enter country");
        }

        if (zipCode.isEmpty()) {
            valid = false;
            zipCodeText.setError("Please enter post code");
        }

        if (member.isFirstTimeLogin() && password.isEmpty()) {
            valid = false;
            passwordText.setError("Please update your password");
        }

        if (!password.isEmpty() && confirmPassword.isEmpty()) {
            valid = false;
            confirmPasswordText.setError("Please confirm your password");
        } else if (!password.equals(confirmPassword)) {
            valid = false;
            confirmPasswordText.setError("Passwords did not match");
        }

        return valid;
    }

    // Vaccine Certificate Methods------------------------------------------------------------------
    private void validateUploadedCertificate() {
        certificateStorage.child(member.getMembershipNo())
                .getDownloadUrl().addOnSuccessListener(uri -> {
            if (uri != null) {
                btnViewVaccineCertificate.setText(R.string.viewVaccineCertificate);
                btnViewVaccineCertificate.setOnClickListener(view -> {
                    showPDFCertificate(uri);
                });
            }
            if (pDialog != null) pDialog.dismiss();
        })
                .addOnFailureListener(exception -> {
                    btnViewVaccineCertificate.setText(R.string.addVaccineCertificate);
                    btnViewVaccineCertificate.setOnClickListener(view -> {
                        showSelectCertificateDialog();
                    });
                    if (pDialog != null) pDialog.dismiss();
                });
    }

    private void showSelectCertificateDialog() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");

        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri;
            if (resultData != null) {
                uri = resultData.getData();
                showDisclaimerDialog(uri);
            }
        }
    }

    private void showDisclaimerDialog(Uri filePath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Disclaimer");
        builder.setMessage("\nThe vaccine certificate I am uploading to Nairobi Gymkhana Member's app " +
                "is provided by the Kenyan Ministry of Health and has NOT been altered in any manner.\n");
        builder.setPositiveButton("I Agree", (dialog, which) -> {
            uploadCertificateToFireBase(filePath);
        });
        builder.setNeutralButton("Cancel", (dialog, which) -> dismissDialog());

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        Button PB = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button NB = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);

        PB.setTextSize(18);
        PB.setTypeface(Typeface.DEFAULT_BOLD);
        PB.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.ng_blue));
        PB.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

        NB.setTextSize(18);
        NB.setTypeface(Typeface.DEFAULT_BOLD);
        NB.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.ng_error_red));
        NB.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

    }

    private void uploadCertificateToFireBase(Uri filePath) {
        pDialog = new SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(ContextCompat.getColor(requireContext(), R.color.ng_blue));
        pDialog.setCancelable(true);
        pDialog.setTitle("Uploading Certificate");
        pDialog.show();

        certificateStorage.child(member.getMembershipNo())
                .putFile(filePath).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                certificateStorage.child(member.getMembershipNo())
                        .getDownloadUrl()
                        .addOnSuccessListener(uri1 -> {
                            btnViewVaccineCertificate.setText(R.string.viewVaccineCertificate);
                            btnViewVaccineCertificate.setOnClickListener(view -> {
                                showPDFCertificate(uri1);
                            });
                            if (pDialog != null) pDialog.dismiss();
                            Utils.displayMessage(requireActivity(),
                                    "Success", "Certificate Uploaded Successfully.");
                        });
            } else {
                if (pDialog != null) pDialog.dismiss();
            }

        });
    }

    private void showPDFCertificate(Uri path) {
        Intent intent = new Intent(requireContext(), ViewPDFActivity.class);
        intent.putExtra("URL", path.toString());
        startActivity(intent);
    }

    // END Vaccine Certificate Methods--------------------------------------------------------------

    // TODO Remove this after payment is implemented

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
            } else {
                subscription = null;
                btnMyWallet.setVisibility(View.GONE);
            }
        });

    }

    private boolean isDefaulted() {
        Date reinstatementDate = getReinstatementDate();
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


        try {
            OutputStream fos;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = requireContext().getContentResolver();
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME,
                        String.format("Statement_%s.pdf", member.getMembershipNo()));
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, folderPath);
                Uri contentUri = MediaStore.Files.getContentUri("external");
                Uri pdfURI = resolver.insert(contentUri, values);

                fos = resolver.openOutputStream(pdfURI);
            } else {
                folderPath = Environment.getExternalStorageDirectory() + File.separator + folderPath;
                File savePath = new File(folderPath);
                if (!savePath.exists()) savePath.mkdirs();
                File file = new File(savePath,
                        String.format("Statement - %s.pdf", member.getMembershipNo()));
                fos = new FileOutputStream(file);
            }
            document.writeTo(fos);

            assert fos != null;
            fos.flush();
            fos.close();

            Toast.makeText(requireContext(),
                    "Statement Created: Document/Nairobi Gymkhana/Statement_"
                            + member.getMembershipNo() + ".pdf", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("generateMemberStatement: ", e.toString());
            Toast.makeText(requireContext(),
                    "Could not Save the statement", Toast.LENGTH_LONG).show();
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
            String text7 = "CURRENT YEAR FEE NOTE/LETTER FROM THEIR INSTITUTE OF EDUCATION INDICATING THAT";
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
}
