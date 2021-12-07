package com.shyam.ngmobile.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shyam.ngmobile.Model.Member;
import com.shyam.ngmobile.PaymentActivity;
import com.shyam.ngmobile.R;
import com.shyam.ngmobile.Services.Utils;

import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AccountFragment extends Fragment {

    private static final String MEMBER_ID = "MEMBER_ID";
    private FirebaseAuth mAuth;
    private CollectionReference memberRef;

    View view;
    Member member;
    Button btnMyWallet, btnUpdateProfile;
    EditText postAddressText, cityText, countryText, zipCodeText, phoneNumberText, memberNoText,
            memberTypeText, emailText, passwordText, confirmPasswordText;
    TextView memberName;
    private SweetAlertDialog pDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull @org.jetbrains.annotations.NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_account, container, false);

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

        pDialog = new SweetAlertDialog(view.getContext(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setTitle("Updating...");
        pDialog.getProgressHelper().setBarColor(ContextCompat.getColor(requireContext(), R.color.ng_blue));
        pDialog.setCancelable(true);

        btnMyWallet = view.findViewById(R.id.member_wallet);
        btnMyWallet.setOnClickListener(view1 -> {
            Intent intent = new Intent(getContext(), PaymentActivity.class);
            intent.putExtra(MEMBER_ID, member.getUserID());
            startActivity(intent);
        });

        btnUpdateProfile = view.findViewById(R.id.member_update_profile);
        btnUpdateProfile.setOnClickListener(view -> {
            if (validInput()) {
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
        memberTypeText.setText(member.getMemberType());
        emailText.setText(member.getEmail());

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
}
