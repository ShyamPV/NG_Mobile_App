package com.shyam.ngmobile.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.shyam.ngmobile.Enums.MemberStatus;
import com.shyam.ngmobile.Enums.MembershipType;
import com.shyam.ngmobile.MainActivity;
import com.shyam.ngmobile.Model.Member;
import com.shyam.ngmobile.PaymentActivity;
import com.shyam.ngmobile.R;
import com.shyam.ngmobile.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class LoginFragment extends Fragment {

    EditText emailText;
    Button btnLogin;
    TextView btnForgotPwd;

    private ArrayList<Member> membersList = new ArrayList<>();

    Calendar date = Calendar.getInstance();

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragement_login, container, false);

        getMembersList();
        btnForgotPwd = view.findViewById(R.id.login_reset_password);
        btnLogin = view.findViewById(R.id.btn_login);
        emailText = view.findViewById(R.id.login_email);

        btnForgotPwd.setOnClickListener(view1 -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.login_container, new ForgotPasswordFragment()).commit();
        });

        btnLogin.setOnClickListener(view1 -> {
            // TODO REMOVE THIS DUMMY CODE
            if (!emailText.getText().toString().isEmpty()) {
                String email = emailText.getText().toString().trim();
                Member currentMember = null;
                for (Member member : membersList) {
                    if (member.getEmail().equals(email)) {
                        currentMember = member;
                        break;
                    }
                }
                if (currentMember != null) {
                    if (currentMember.getAccountStatus() == MemberStatus.Active) {
                        Utils.setCurrentMember(currentMember);
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        startActivity(intent);
                    } else if (currentMember.getAccountStatus() == MemberStatus.Defaulted) {
                        Toast.makeText(getContext(), "Your account is defaulted, Please pay subs!", Toast.LENGTH_SHORT).show();
                        Utils.setCurrentMember(currentMember);
                        Intent intent = new Intent(getContext(), PaymentActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getContext(), "Your membership is cancelled.\nYou cannot use this application.", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(getContext(), "No member found with entered email!", Toast.LENGTH_SHORT).show();
                }

            } else {
                emailText.setError("Please enter email");
            }
            // END OF DUMMY CODE
        });

        return view;
    }

    // DUMMY MEMBERS
    private void getMembersList() {
        date.set(2022, 1, 28);
        Date activeDate = date.getTime();

        //Active returning Member
        membersList.add(new Member("activeMember", "A-02-0001", "Active Member",
                "0712345678", "active@member.com", MembershipType.Ordinary_Member,
                MemberStatus.Active, activeDate, activeDate, false));

        //New returning Member
        membersList.add(new Member("activeMember", "A-02-0001", "Active New Member",
                "0712345678", "new_active@member.com", MembershipType.Ordinary_Member,
                MemberStatus.Active, activeDate, activeDate, true));

        //Defaulted Member

        date.set(2021, 1, 28);
        Date defaultedDate = date.getTime();
        membersList.add(new Member("activeMember", "A-02-0001", "Defaulted Member",
                "0712345678", "defaulted@member.com", MembershipType.Ordinary_Member,
                MemberStatus.Defaulted, defaultedDate, defaultedDate, false));

        //Defaulted Member
        membersList.add(new Member("activeMember", "A-02-0001", "Defaulted Member",
                "0712345678", "cancelled@member.com", MembershipType.Ordinary_Member,
                MemberStatus.Cancelled_Membership, defaultedDate, defaultedDate, false));
    }
}
