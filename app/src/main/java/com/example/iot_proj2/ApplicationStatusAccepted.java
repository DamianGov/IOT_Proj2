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

public class ApplicationStatusAccepted extends AppCompatActivity {
    @BindView(R.id.rvLecAcceptedApplications)
    RecyclerView ApplicationsRV;

    private NavigationView nav_View;

    private FirebaseFirestore FStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_status_accepted);

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
                case R.id.mLecturerVacancyBoard: {
                    Intent intent = new Intent(this, VacancyBoardLecturer.class);
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

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("");
        progressDialog.setMessage("Loading Applications...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Query query = FStore.collection("Vacancy").whereEqualTo("status", "0").whereEqualTo("created_by", UserIDStatic.getInstance().getUserId());
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot completedVac = task.getResult();
                List<Vacancy> vacancyList = new ArrayList<>();
                List<Application> applicationList = new ArrayList<>();

                if (completedVac != null && !completedVac.isEmpty()) {

                    for (DocumentSnapshot documentSnapshot : completedVac.getDocuments()) {
                        Vacancy vacancy = documentSnapshot.toObject(Vacancy.class);
                        vacancyList.add(vacancy);
                    }


                    Query getAcceptedApp = FStore.collection("Application").whereEqualTo("status", "accepted");
                    getAcceptedApp.get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            QuerySnapshot accApp = task1.getResult();

                            if (accApp != null && !accApp.isEmpty()) {

                                List<Task<DocumentSnapshot>> tasksNames = new ArrayList<>();
                                List<Application> tempList = new ArrayList<>();
                                for (Vacancy vacancy : vacancyList) {
                                    for (DocumentSnapshot documentSnapshot : accApp.getDocuments()) {
                                        Application application = documentSnapshot.toObject(Application.class);

                                        if (application.getVacancy_id().equals(Long.toString(vacancy.getDocId()))) {
                                            application.setModule(vacancy.getModule());
                                            application.setType(vacancy.getType());
                                            application.setDescription(vacancy.getDescription());
                                            application.setSemester(vacancy.getSemester());
                                            application.setSalary(vacancy.getSalary());
                                            DocumentReference nameStud = FStore.collection("Student").document(application.getStudent_num());
                                            Task<DocumentSnapshot> studSnap = nameStud.get();
                                            tasksNames.add(studSnap);


                                            tempList.add(application);
                                        }
                                    }
                                }

                                Tasks.whenAllSuccess(tasksNames.toArray(new Task[tasksNames.size()])).addOnSuccessListener(obj -> {
                                    for (int z = 0; z < tasksNames.size(); z++) {
                                        DocumentSnapshot name = tasksNames.get(z).getResult();
                                        Application app = tempList.get(z);
                                        app.setPersonName(name.getString("name"));
                                        applicationList.add(app);
                                    }
                                    runOnUiThread(() -> setAdapter(applicationList, progressDialog));
                                });


                            } else {
                                runOnUiThread(() -> setAdapter(applicationList, progressDialog));
                            }


                        }

                    });


                } else {
                    runOnUiThread(() -> setAdapter(applicationList, progressDialog));
                }
            }
        });

    }

    private void setAdapter(List<Application> applicationList, ProgressDialog p) {
        p.dismiss();
        ApplicationAdapterAccepted applicationAdapter = new ApplicationAdapterAccepted(applicationList, this);
        ApplicationsRV.setAdapter(applicationAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(ApplicationStatusAccepted.this);
        ApplicationsRV.setLayoutManager(layoutManager);
    }

    @Override
    public void onBackPressed() {
        return;
    }
}