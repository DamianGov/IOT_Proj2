package com.example.iot_proj2;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileLecturer extends AppCompatActivity {

    @BindView(R.id.edtPLecName)
    EditText LecName;

    @BindView(R.id.edtPLecFullName)
    EditText LecFullName;

    @BindView(R.id.edtPLecStaffNum)
    EditText LecStaffNum;

    @BindView(R.id.edtPLecEmail)
    EditText LecEmail;

    @BindView(R.id.edtPLecFaculty)
    EditText LecFac;

    @BindView(R.id.edtPLecDepart)
    EditText LecDepar;

    @BindView(R.id.edtPLecModule)
    EditText LecModule;

    private FirebaseFirestore FStore;

    private NavigationView nav_View;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_lecturer);

        ButterKnife.bind(this);

        // NAV
        final DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        findViewById(R.id.imgMenu).setOnClickListener(view -> {
            drawerLayout.openDrawer(GravityCompat.END);
        });
        final NavigationView NavView = (NavigationView) findViewById(R.id.navigationView);
        nav_View = (NavigationView) findViewById(R.id.navigationView);
        Menu navMenu = nav_View.getMenu();
        navMenu.findItem(R.id.mStudentProfile).setVisible(false);
        navMenu.findItem(R.id.mStudentVacancyBoard).setVisible(false);
        navMenu.findItem(R.id.mStudentApplicationStatus).setVisible(false);
        navMenu.findItem(R.id.mStudentAppointmentStatus).setVisible(false);
        navMenu.findItem(R.id.mStudentCreateAppointment).setVisible(false);
        navMenu.findItem(R.id.mStudentUpdateResume).setVisible(false);
        NavView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();


            switch (id) {
                case R.id.mLecturerVacancyBoard: {
                    Intent intent = new Intent(this, VacancyBoardLecturer.class);
                    startActivity(intent);
                }
                break;
                case R.id.mLecturerCreateVac: {
                    Intent intent = new Intent(this, CreateVacancyLecturer.class);
                    startActivity(intent);
                }
                break;
                case R.id.mLecturerApplicationStatus: {
                    Intent intent = new Intent(this, ApplicationStatusLecturer.class);
                    startActivity(intent);
                }
                break;
                case R.id.mLecturerApplicationAcceptStatus: {
                    Intent intent = new Intent(this, ApplicationStatusAccepted.class);
                    startActivity(intent);
                }
                break;
                case R.id.mLecturerAppointmentStatus: {
                    Intent intent = new Intent(this, AppointmentStatusLecturer.class);
                    startActivity(intent);
                }
                break;
                case R.id.mLogOut: {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                break;
            }

            drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        });


        FStore = FirebaseFirestore.getInstance();

        String StaffNum = UserIDStatic.getInstance().getUserId();

        DocumentReference docRef = FStore.collection("Lecturer").document(StaffNum);
        docRef.addSnapshotListener((value, error) -> {
            LecName.setText(value.getString("name"));
            LecFullName.setText(value.getString("name"));
            LecStaffNum.setText(StaffNum);
            LecEmail.setText(value.getString("email"));
            LecFac.setText(value.getString("faculty"));
            LecDepar.setText(value.getString("department"));
            LecModule.setText(value.getString("module"));
        });

    }

    @Override
    public void onBackPressed() {
        return;
    }
}