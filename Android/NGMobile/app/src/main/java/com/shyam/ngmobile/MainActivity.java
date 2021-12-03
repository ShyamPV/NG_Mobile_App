package com.shyam.ngmobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.shyam.ngmobile.Fragment.AccountFragment;
import com.shyam.ngmobile.Fragment.HomeFragment;


public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Utils.getCurrentMember() != null) {
            setup();
        } else {
            Toast.makeText(getApplicationContext(), "No Member found", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private void setup() {

        bottomNav = findViewById(R.id.bottom_navigation);
        toolbar = findViewById(R.id.main_toolbar);
        toolbar.setTitle("Nairobi Gymkhana");

        bottomNav.setOnItemSelectedListener(navListener);
        if (Utils.currentMember.isFirstTimeLogin()) {
            bottomNav.setSelectedItemId(R.id.nav_account);
        } else {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    private final NavigationBarView.OnItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;

        switch (item.getItemId()) {
            case R.id.nav_home:
                selectedFragment = new HomeFragment();
                break;
            case R.id.nav_account:
                selectedFragment = new AccountFragment();
                break;
//            case R.id.nav_about:
//                selectedFragment =  new AboutFragment();
//                break;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, selectedFragment).commit();

        return true;
    };

    @Override
    public void onBackPressed() {
        if (bottomNav.getSelectedItemId() != R.id.nav_home) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        } else {
            Intent exitApp = new Intent(Intent.ACTION_MAIN);
            exitApp.addCategory(Intent.CATEGORY_HOME);
            exitApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(exitApp);
        }
    }
}
