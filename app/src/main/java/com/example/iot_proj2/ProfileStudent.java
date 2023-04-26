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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileStudent extends AppCompatActivity {

    @BindView(R.id.edtPStudName)
    EditText ProfStudName;

    @BindView(R.id.edtPStudFullName)
    EditText ProfFullName;

    @BindView(R.id.edtPStudStudentNum)
    EditText ProfStudNum;

    @BindView(R.id.edtPStudEmail)
    EditText ProfEmail;

    @BindView(R.id.edtPStudID)
    EditText ProfID;

    @BindView(R.id.edtPStudFaculty)
    EditText ProfFac;

    @BindView(R.id.edtPStudCourse)
    EditText ProfCourse;

    @BindView(R.id.edtPStudPosition)
    EditText ProfPost;


    private FirebaseFirestore FStore;

    private NavigationView nav_View;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_student);

        ButterKnife.bind(this);

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
        NavView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            switch (id) {
                case R.id.mStudentVacancyBoard: {
                    Intent intent = new Intent(this, VacancyBoardStudent.class);
                    startActivity(intent);
                }
                break;
                case R.id.mStudentApplicationStatus: {
                    Intent intent = new Intent(this, ApplicationStatusStudent.class);
                    startActivity(intent);
                }
                break;
                case R.id.mStudentAppointmentStatus: {
                    Intent intent = new Intent(this, AppointmentStatusStudent.class);
                    startActivity(intent);
                }
                break;
                case R.id.mStudentCreateAppointment: {
                    Intent intent = new Intent(this, CreateAppointmentStudent.class);
                    startActivity(intent);
                }
                break;
                case R.id.mStudentUpdateResume: {
                    Intent intent = new Intent(this, ResumeStudent.class);
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

        String StudentNum = UserIDStatic.getInstance().getUserId();

        DocumentReference docRef = FStore.collection("Student").document(StudentNum);
        docRef.addSnapshotListener((value, error) -> {
            ProfStudName.setText(value.getString("name"));
            ProfFullName.setText(value.getString("name"));
            ProfStudNum.setText(StudentNum);
            ProfEmail.setText(value.getString("email"));
            ProfID.setText(value.getString("ID"));
            ProfFac.setText(value.getString("faculty"));
            ProfCourse.setText(value.getString("course"));
        });


        DocumentReference PosTypeRef = FStore.collection("Tutor").document(StudentNum);
        PosTypeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot snap = task.getResult();
                if (snap.exists()) {
                    ProfPost.setText(snap.getString("type"));
                } else {
                    ProfPost.setText("Student");
                }
            }
        });


    }

    @Override
    public void onBackPressed() {
        return;
    }
}