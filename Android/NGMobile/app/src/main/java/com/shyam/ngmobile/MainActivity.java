package com.shyam.ngmobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.shyam.ngmobile.Fragment.AccountFragment;
import com.shyam.ngmobile.Fragment.HomeFragment;
import com.shyam.ngmobile.Model.Member;
import com.shyam.ngmobile.Services.Utils;


public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private Toolbar toolbar;
    private Member member;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            member = Utils.getCurrentMember();
            setup();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "No Member found", Toast.LENGTH_SHORT).show();
            Utils.logoutUser(this);
        }
    }

    private void setup() {

        bottomNav = findViewById(R.id.bottom_navigation);
        toolbar = findViewById(R.id.main_toolbar);
        toolbar.setTitle("Nairobi Gymkhana");
        setSupportActionBar(toolbar);

        bottomNav.setOnItemSelectedListener(navListener);
        if (Utils.currentMember.isFirstTimeLogin()) {
            bottomNav.setSelectedItemId(R.id.nav_account);
        } else {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    private final NavigationBarView.OnItemSelectedListener navListener = item -> {

        Fragment selectedFragment = null;

        if (member.isFirstTimeLogin() && item.getItemId() != R.id.nav_account) {
            Toast.makeText(this, "Please Complete Registration.", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.nav_account:
                    selectedFragment = new AccountFragment();
                    break;
//              case R.id.nav_about:
//                  selectedFragment =  new AboutFragment();
//                  break;
            }
        }
        assert selectedFragment != null;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, selectedFragment).commit();

        return true;
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tool_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            Utils.logoutUser(this);
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

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
