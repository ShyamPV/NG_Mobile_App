package com.shyam.ngmobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.shyam.ngmobile.Enums.MemberStatus;
import com.shyam.ngmobile.Model.Member;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity {

    EditText memberType, expiryDate, amountDue;
    Button statement, pay;
    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
    Member member;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        member = Utils.getCurrentMember();

        memberType = findViewById(R.id.payment_member_type);
        expiryDate = findViewById(R.id.payment_member_expiry_date);
        amountDue = findViewById(R.id.payment_member_amount);
        pay = findViewById(R.id.btn_make_payment);
        statement = findViewById(R.id.generate_statement);

        memberType.setText(member.getMemberType().toString());
        expiryDate.setText(format.format(member.getMemberExpiryDate()));
        amountDue.setText("Ksh 15,700/=");

        pay.setOnClickListener(view -> Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show());
        statement.setOnClickListener(view -> Toast.makeText(this, "Statement Generated", Toast.LENGTH_SHORT).show());

    }

    @Override
    public void onBackPressed() {
        if (member.getAccountStatus() == MemberStatus.Defaulted) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        } else {
            finish();
        }
    }
}