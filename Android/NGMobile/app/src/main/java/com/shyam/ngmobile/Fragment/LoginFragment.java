package com.shyam.ngmobile.Fragment;

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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shyam.ngmobile.Enums.MemberStatus;
import com.shyam.ngmobile.MainActivity;
import com.shyam.ngmobile.Model.Member;
import com.shyam.ngmobile.PaymentActivity;
import com.shyam.ngmobile.R;
import com.shyam.ngmobile.Services.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    private CollectionReference memberRef;
    private SweetAlertDialog pDialog;

    private EditText emailText, passwordText;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragement_login, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        memberRef = db.collection("member");

        TextView btnForgotPwd = view.findViewById(R.id.login_reset_password);
        Button btnLogin = view.findViewById(R.id.btn_login);
        emailText = view.findViewById(R.id.login_email);
        passwordText = view.findViewById(R.id.login_password);

        pDialog = new SweetAlertDialog(view.getContext(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setTitle("Authenticating...");
        pDialog.getProgressHelper().setBarColor(ContextCompat.getColor(requireContext(), R.color.ng_blue));
        pDialog.setCancelable(false);

        if (mAuth.getUid() != null) getFirestoreUser(mAuth.getUid());

        btnForgotPwd.setOnClickListener(view1 -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.login_container, new ForgotPasswordFragment()).commit();
        });

        btnLogin.setOnClickListener(view1 -> {
            if (validInput()) {
                passwordText.setEnabled(false);
                pDialog.show();

                String email = emailText.getText().toString().trim();
                String password = passwordText.getText().toString();

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                        getFirestoreUser(userID);
                    } else {
                        if (pDialog != null) pDialog.dismiss();
                        passwordText.setEnabled(true);
                        Utils.displayMessage(requireActivity(), "Error!",
                                "Incorrect login credentials");
                    }
                });
            }
        });

        return view;
    }

    private void getFirestoreUser(String userID) {
        memberRef.document(userID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Member member = Objects.requireNonNull(task.getResult()).toObject(Member.class);
                validateMember(member);
            } else {
                if (pDialog != null) pDialog.dismiss();
                Utils.displayMessage(requireActivity(), "Error!",
                        "Could not get your details! Please try later.");
            }
        });
    }

    private void validateMember(Member member) {
        if (pDialog != null) pDialog.dismiss();
        if (member.getAccountStatus() == MemberStatus.Cancelled) {
            Utils.displayMessage(requireActivity(), "", "Your Membership was cancelled." +
                    "\nYou can not use this application");
            mAuth.signOut();
        } else if (member.getAccountStatus() == MemberStatus.Defaulted) {
            Utils.setCurrentMember(member);
            Utils.displayMessage(requireActivity(), "", "Your account has defaulted" +
                    "\nKindly pay your subscriptions.");
            openPaymentActivity();
        } else {
            Utils.setCurrentMember(member);
            openHomeActivity();
        }
    }

    private boolean validInput() {
        boolean isValid = true;
        String email = emailText.getText().toString().trim();
        String password = passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            isValid = false;
            emailText.setError("Enter a valid email address");
        }

        if (password.isEmpty()) {
            isValid = false;
            passwordText.setError("Enter Password");
        }

        return isValid;
    }

    private void openPaymentActivity() {
        Utils.gotoActivity(requireActivity(), PaymentActivity.class);
    }

    private void openHomeActivity() {
        Utils.gotoActivity(requireActivity(), MainActivity.class);
    }
}
