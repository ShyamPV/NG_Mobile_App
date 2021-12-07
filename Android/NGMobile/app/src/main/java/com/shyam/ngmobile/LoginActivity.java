package com.shyam.ngmobile;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.shyam.ngmobile.Enums.MemberStatus;
import com.shyam.ngmobile.Fragment.LoginFragment;
import com.shyam.ngmobile.Model.Member;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class LoginActivity extends AppCompatActivity {

    private static final String LOGIN_FRAGMENT = "LOGIN_FRAGMENT";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.login_container, new LoginFragment(), LOGIN_FRAGMENT).commit();
    }




    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(LOGIN_FRAGMENT);

        if (fragment != null && fragment.isVisible()) {
            Intent exitApp = new Intent(Intent.ACTION_MAIN);
            exitApp.addCategory(Intent.CATEGORY_HOME);
            exitApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(exitApp);
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.login_container, new LoginFragment(), LOGIN_FRAGMENT).commit();
        }
    }
}