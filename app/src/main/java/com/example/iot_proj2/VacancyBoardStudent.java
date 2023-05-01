package com.example.iot_proj2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VacancyBoardStudent extends AppCompatActivity {

    @BindView(R.id.rvStudVacBoard)
    RecyclerView VacBoard;

    private FirebaseFirestore FStore;

    private NavigationView nav_View;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacancy_board_student);

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
        navMenu.findItem(R.id.mLecturerCreateNote).setVisible(false);
        navMenu.findItem(R.id.mLecturerNoticeBoard).setVisible(false);
        navMenu.findItem(R.id.mLecturerVacancyBoard).setVisible(false);
        navMenu.findItem(R.id.mLecturerCreateVac).setVisible(false);
        navMenu.findItem(R.id.mLecturerApplicationStatus).setVisible(false);
        navMenu.findItem(R.id.mLecturerApplicationAcceptStatus).setVisible(false);
        navMenu.findItem(R.id.mLecturerAppointmentStatus).setVisible(false);
        NavView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            switch (id) {
                case R.id.mStudentProfile: {
                    Intent intent = new Intent(this, ProfileStudent.class);
                    startActivity(intent);
                }
                break;
                case R.id.mStudentNoticeBoard:
                {
                    Intent intent = new Intent(this, Notice_Board_Student.class);
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

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("");
        progressDialog.setMessage("Loading Vacancies...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Query queryVac = FStore.collection("Vacancy").whereEqualTo("status", "1").orderBy("docId", Query.Direction.DESCENDING);
        DocumentReference docRefStud = FStore.collection("Student").document(UserIDStatic.getInstance().getUserId());

        Task<QuerySnapshot> querySnapshotVac = queryVac.get();
        Task<DocumentSnapshot> documentSnapshotStud = docRefStud.get();

        Tasks.whenAllSuccess(querySnapshotVac, documentSnapshotStud).addOnSuccessListener(objects -> {
            QuerySnapshot querySnapVac = querySnapshotVac.getResult();
            DocumentSnapshot documentSnapStud = documentSnapshotStud.getResult();
            List<Vacancy> vacancyList = new ArrayList<>();

            if ((querySnapVac != null && !querySnapVac.isEmpty()) && documentSnapStud.exists()) {
                List<Task<QuerySnapshot>> tasks = new ArrayList<>();
                for (DocumentSnapshot documentSnapshot : querySnapVac.getDocuments()) {
                    Vacancy vacancy = documentSnapshot.toObject(Vacancy.class);

                    Query queryExistApp = FStore.collection("Application").whereEqualTo("student_num", UserIDStatic.getInstance().getUserId())
                            .whereEqualTo("vacancy_id", Long.toString(vacancy.getDocId()));
                    Task<QuerySnapshot> queryExistingApplications = queryExistApp.get();
                    tasks.add(queryExistingApplications); // Add the task to the list of tasks
                }

                // Use Tasks.whenAllSuccess to wait for all tasks to complete
                Tasks.whenAllSuccess(tasks.toArray(new Task[tasks.size()])).addOnSuccessListener(objects2 -> {
                    for (int i = 0; i < tasks.size(); i++) {
                        QuerySnapshot ExistingApp = tasks.get(i).getResult();
                        if (ExistingApp.size() == 0) {
                            vacancyList.add(querySnapVac.getDocuments().get(i).toObject(Vacancy.class));
                        }
                    }

                    // Call setAdapter method after all iterations are fully complete
                    // Note: It's important to perform UI related tasks on the main thread using runOnUiThread or similar methods
                    runOnUiThread(() -> {
                        setAdapter(vacancyList, progressDialog);
                    });
                });
            } else {
                runOnUiThread(() -> {
                    setAdapter(vacancyList, progressDialog);
                });
            }


        });


    }

    private void setAdapter(List<Vacancy> vacancyList, ProgressDialog progressDialog) {
        progressDialog.dismiss();
        VacancyAdapterStudent vacancyAdapterStudent = new VacancyAdapterStudent(vacancyList, this);
        VacBoard.setAdapter(vacancyAdapterStudent);
        LinearLayoutManager layoutManager = new LinearLayoutManager(VacancyBoardStudent.this);
        VacBoard.setLayoutManager(layoutManager);
    }

    @Override
    public void onBackPressed() {
        return;
    }
}
