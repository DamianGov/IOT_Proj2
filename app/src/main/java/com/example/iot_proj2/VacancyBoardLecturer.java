package com.example.iot_proj2;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VacancyBoardLecturer extends AppCompatActivity {

    @BindView(R.id.rvLecVacBoard)
    RecyclerView VacBoard;

    private NavigationView nav_View;

    private FirebaseFirestore FStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacancy_board_lecturer);

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
        navMenu.findItem(R.id.mStudentNoticeBoard).setVisible(false);
        navMenu.findItem(R.id.mStudentVacancyBoard).setVisible(false);
        navMenu.findItem(R.id.mStudentApplicationStatus).setVisible(false);
        navMenu.findItem(R.id.mStudentAppointmentStatus).setVisible(false);
        navMenu.findItem(R.id.mStudentCreateAppointment).setVisible(false);
        navMenu.findItem(R.id.mStudentUpdateResume).setVisible(false);
        NavView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();


            switch (id) {
                case R.id.mLecturerProfile: {
                    Intent intent = new Intent(this, ProfileLecturer.class);
                    startActivity(intent);
                }
                break;
                case R.id.mLecturerNoticeBoard:
                {
                    Intent intent = new Intent(this, NoticeBoardLecturer.class);
                    startActivity(intent);
                }
                break;
                case R.id.mLecturerCreateNote:
                {
                    Intent intent = new Intent(this, CreateNoticeLecturer.class);
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

        Query query = FStore.collection("Vacancy").whereEqualTo("created_by", UserIDStatic.getInstance().getUserId()).orderBy("docId", Query.Direction.DESCENDING);
        query.get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        List<Vacancy> vacancyList = new ArrayList<>();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                                Vacancy vacancy = documentSnapshot.toObject(Vacancy.class);
                                vacancyList.add(vacancy);
                            }

                            runOnUiThread(() -> setAdapter(vacancyList));
                        } else {
                            runOnUiThread(() -> setAdapter(vacancyList));
                        }
                    }
                });

    }

    private void setAdapter(List<Vacancy> vacancyList) {
        VacancyAdapter vacancyAdapter = new VacancyAdapter(vacancyList, this);
        VacBoard.setAdapter(vacancyAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(VacancyBoardLecturer.this);
        VacBoard.setLayoutManager(layoutManager);
    }

    @Override
    public void onBackPressed() {
        return;
    }
}