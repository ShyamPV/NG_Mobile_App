package com.shyam.ngmobile.Fragment;

import android.os.Bundle;
import android.util.Patterns;
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
import com.shyam.ngmobile.R;
import com.shyam.ngmobile.Services.Utils;

import org.jetbrains.annotations.NotNull;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ForgotPasswordFragment extends Fragment {

    private static final String LOGIN_FRAGMENT = "LOGIN_FRAGMENT";
    private FirebaseAuth mAuth;
    private SweetAlertDialog pDialog;

    EditText emailText;
    Button btnSendEmail;
    TextView btnCancel;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragement_forgot_password, container, false);

        emailText = view.findViewById(R.id.forgot_password_email);
        btnCancel = view.findViewById(R.id.btn_forgot_cancel);
        btnSendEmail = view.findViewById(R.id.btn_send_email);

        pDialog = new SweetAlertDialog(view.getContext(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setTitle("Sending Link...");
        pDialog.getProgressHelper().setBarColor(ContextCompat.getColor(requireContext(), R.color.ng_blue));
        pDialog.setCancelable(false);

        mAuth = FirebaseAuth.getInstance();

        btnCancel.setOnClickListener(view1 -> {
            goToLogin();
        });

        btnSendEmail.setOnClickListener(view1 -> {
            if (isValidInput()) {
                pDialog.show();
                String email = emailText.getText().toString().trim();
                sendResetEmail(email);
            }
        });
        
        return view;
    }

    private void sendResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            pDialog.dismiss();
            if (task.isSuccessful()) {
                Utils.displayMessage(requireActivity(), "Success", "Reset link sent to: " + email);
                goToLogin();
            } else {
                Utils.displayMessage(requireActivity(), "Error!", "No member found with email: " + email);
            }
        });
    }

    private boolean isValidInput() {
        String email = emailText.getText().toString();
        boolean isValid = true;

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            isValid = false;
            emailText.setError("Please enter a valid email");
        }

        return isValid;
    }

    private void goToLogin() {
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.login_container, new LoginFragment(), LOGIN_FRAGMENT).commit();
    }
}
