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

import com.shyam.ngmobile.LoginActivity;
import com.shyam.ngmobile.Model.Member;
import com.shyam.ngmobile.PaymentActivity;
import com.shyam.ngmobile.R;
import com.shyam.ngmobile.Utils;

public class AccountFragment extends Fragment {

    private static final String MEMBER_ID = "MEMBER_ID";
    View view;
    Member member;
    Button btnMyWallet, btnUpdateProfile;
    EditText postAddress, city, country, postCode, memberNo, memberType, email;
    TextView memberName;

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
            startActivity(new Intent(getContext(), LoginActivity.class));
        }


        return view;
    }

    private void setup() {
        btnMyWallet = view.findViewById(R.id.member_wallet);
        btnMyWallet.setOnClickListener(view1 -> {
            Intent intent = new Intent(getContext(), PaymentActivity.class);
            intent.putExtra(MEMBER_ID, member.getUserID());
            startActivity(intent);
        });

        btnUpdateProfile = view.findViewById(R.id.member_update_profile);
        btnUpdateProfile.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Details Updated Successfully", Toast.LENGTH_SHORT).show();
        });

        memberName = view.findViewById(R.id.member_name);
        postAddress = view.findViewById(R.id.member_address_post_address);
        city = view.findViewById(R.id.member_address_city);
        country = view.findViewById(R.id.member_address_country);
        postCode = view.findViewById(R.id.member_address_post_code);
        memberNo = view.findViewById(R.id.member_membership_no);
        memberType = view.findViewById(R.id.member_membership_type);
        email = view.findViewById(R.id.member_email);

        memberName.setText(member.getFullName());
        postAddress.setText(member.getPostAddress());
        city.setText(member.getCity());
        country.setText(member.getCountry());
        postCode.setText(member.getZipCode());
        memberNo.setText(member.getMembershipNo());
        memberType.setText(member.getMemberType().toString());
        email.setText(member.getEmail());

    }
}
