package com.example.smartirrigationsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.example.smartirrigationsystem.fragment.Analytics;
import com.example.smartirrigationsystem.fragment.History;
import com.example.smartirrigationsystem.fragment.Irrigation;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

public class HomeActivity extends AppCompatActivity {
    FloatingActionButton fab;
    DrawerLayout drawerLayout;
    BottomNavigationView bottomNavigationView;
    FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fab = findViewById(R.id.fab);
        drawerLayout = findViewById(R.id.drawer_layout);
        frameLayout = findViewById(R.id.frame_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,R.string.open_nav, R.string.close_nav);

        drawerLayout.addDrawerListener(toggle) ;
        toggle.syncState();

        if (savedInstanceState == null ) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new com.example.smartirrigationsystem.fragment.Home()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.home){

                    replaceFragment(new com.example.smartirrigationsystem.fragment.Home(), false);

                } else if( itemId == R.id.irrigation){

                    replaceFragment(new Irrigation(), false);

                }else if (itemId == R.id.history){

                    replaceFragment(new History(), false);

                }else { //nav profile

                    replaceFragment(new Analytics(), false);
                }

                return true;
            }
        });
        replaceFragment(new com.example.smartirrigationsystem.fragment.Home(), true);
    }
    private  void replaceFragment(Fragment fragment, boolean isAppInitialized) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (isAppInitialized){
            fragmentTransaction.add(R.id.frame_layout, fragment);
        }else {
            fragmentTransaction.replace(R.id.frame_layout, fragment);
        }
        fragmentTransaction.commit();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.topbar,menu);
        return true;
    }

}