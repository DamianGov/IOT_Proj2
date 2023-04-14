package com.example.iot_proj2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.AdapterView;

import com.example.iot_proj2.databinding.ActivityAdminZoneBinding;
import com.example.iot_proj2.databinding.ActivityMainBinding;

import java.io.Serializable;

public class AdminZone extends AppCompatActivity {

    ActivityAdminZoneBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminZoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


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
}