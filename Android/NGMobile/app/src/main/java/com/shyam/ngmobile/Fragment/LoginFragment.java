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
import com.shyam.ngmobile.Model.Subscription;
import com.shyam.ngmobile.PaymentActivity;
import com.shyam.ngmobile.R;
import com.shyam.ngmobile.Services.Utils;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    private CollectionReference memberRef;
    private SweetAlertDialog pDialog;

    private EditText emailText, passwordText;

    // TODO Remove this after payment is working
    private static final String ORDINARY = "Ordinary Member";
    private static final String UPCOUNTRY = "Upcountry Member";

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
            Utils.displayMessage(requireActivity(), "Error!", "Your Membership was cancelled." +
                    "\nYou can not use this application");
            mAuth.signOut();
        } else if (Calendar.getInstance().getTime().after(member.getMemberExpiryDate())) {
            memberRef.document(member.getUserID()).update("accountStatus", MemberStatus.Defaulted);
            member.setAccountStatus(MemberStatus.Defaulted);
            getSubsAmount(getDefaultedDate(member), member);
            // TODO Change thi once payment is implemented
//            Utils.setCurrentMember(member);
//            openPaymentActivity();
            mAuth.signOut();
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
            Utils.displayMessage(requireActivity(), "Error!", message);
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

    private void openPaymentActivity() {
        Utils.gotoActivity(requireActivity(), PaymentActivity.class);
    }

    private void openHomeActivity() {
        Utils.gotoActivity(requireActivity(), MainActivity.class);
    }
}
