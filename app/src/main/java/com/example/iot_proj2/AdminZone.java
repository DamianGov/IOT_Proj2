package com.example.iot_proj2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.AdapterView;

import com.example.iot_proj2.databinding.ActivityAdminZoneBinding;
import com.example.iot_proj2.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

import java.io.Serializable;

public class AdminZone extends AppCompatActivity {

    ActivityAdminZoneBinding binding;
    private NavigationView nav_View;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminZoneBinding.inflate(getLayoutInflater());



        setContentView(binding.getRoot());

        // NAV
        final DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        findViewById(R.id.imgMenu).setOnClickListener(view -> {
            drawerLayout.openDrawer(GravityCompat.END);
        });
        final NavigationView NavView = (NavigationView) findViewById(R.id.navigationView);
        nav_View = (NavigationView) findViewById(R.id.navigationView);
        Menu navMenu = nav_View.getMenu();
        navMenu.findItem(R.id.mLecturerProfile).setVisible(false);
        navMenu.findItem(R.id.mLecturerVacancyBoard).setVisible(false);
        navMenu.findItem(R.id.mLecturerCreateVac).setVisible(false);
        navMenu.findItem(R.id.mLecturerApplicationStatus).setVisible(false);
        navMenu.findItem(R.id.mLecturerApplicationAcceptStatus).setVisible(false);
        navMenu.findItem(R.id.mLecturerAppointmentStatus).setVisible(false);
        navMenu.findItem(R.id.mStudentProfile).setVisible(false);
        navMenu.findItem(R.id.mStudentVacancyBoard).setVisible(false);
        navMenu.findItem(R.id.mStudentApplicationStatus).setVisible(false);
        navMenu.findItem(R.id.mStudentAppointmentStatus).setVisible(false);
        navMenu.findItem(R.id.mStudentCreateAppointment).setVisible(false);
        navMenu.findItem(R.id.mStudentUpdateResume).setVisible(false);
        NavView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.mLogOut){
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                finish();
            }


            drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        });


        replaceFragment(new LecturerFragment());

        binding.bottomNavigationViewAdmin.setOnItemSelectedListener(item -> {

            switch (item.getItemId())
            {
                case R.id.studentMenuOption: replaceFragment(new StudentFragment());
                    break;
                default:replaceFragment(new LecturerFragment());
                    break;
            }

            return true;
        });
    }

    private void replaceFragment(Fragment fragment)
    {
        FragmentManager fragmentManager= getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.frameLayoutAdmin, fragment);
        fragmentTransaction.commit();
    }


    @Override
    public void onBackPressed()
    {
        return;
    }
}