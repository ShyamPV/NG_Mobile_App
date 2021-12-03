package com.shyam.ngmobile.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.shyam.ngmobile.Model.Member;
import com.shyam.ngmobile.R;

import org.jetbrains.annotations.NotNull;

public class ForgotPasswordFragment extends Fragment {

    private static final String LOGIN_FRAGMENT = "LOGIN_FRAGMENT";

    Button btnSendEmail;
    TextView btnCancel;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragement_forgot_password, container, false);

        btnCancel = view.findViewById(R.id.btn_forgot_cancel);
        btnSendEmail = view.findViewById(R.id.btn_send_email);

        btnCancel.setOnClickListener(view1 -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.login_container, new LoginFragment(), LOGIN_FRAGMENT).commit();
        });

        btnSendEmail.setOnClickListener(view1 -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.login_container, new LoginFragment(), LOGIN_FRAGMENT).commit();
        });


        return view;
    }
}
