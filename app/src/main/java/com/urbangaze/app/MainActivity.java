package com.urbangaze.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.urbangaze.app.ui.buddies.BuddiesFragment;
import com.urbangaze.app.ui.explore.ExploreFragment;
import com.urbangaze.app.ui.me.MeFragment;
import com.urbangaze.app.ui.trips.TripsFragment;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottom_nav;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        bottom_nav = findViewById(R.id.bottom_nav);

        bottom_nav.setOnItemSelectedListener(item -> {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            if (item.getItemId() == R.id.menu_explore) {
                transaction.replace(R.id.fragment_container, new ExploreFragment());
            }
            else if (item.getItemId() == R.id.menu_buddies) {
                transaction.replace(R.id.fragment_container, new BuddiesFragment());
            }
            else if (item.getItemId() == R.id.menu_trips) {
                transaction.replace(R.id.fragment_container, new TripsFragment());
            }
            else if (item.getItemId() == R.id.menu_me) {
                transaction.replace(R.id.fragment_container, new MeFragment());
            }

            transaction.commit();
            return true;
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ExploreFragment())
                .commit();

    }
}
