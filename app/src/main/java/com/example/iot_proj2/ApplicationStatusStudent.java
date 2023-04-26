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

public class ApplicationStatusStudent extends AppCompatActivity {

    @BindView(R.id.rvStudApplications)
    RecyclerView ApplicationsRV;

    private NavigationView nav_View;
    private FirebaseFirestore FStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_status_student);

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
                case R.id.mStudentProfile: {
                    Intent intent = new Intent(this, ProfileStudent.class);
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
                case R.id.mStudentVacancyBoard: {
                    Intent intent = new Intent(this, VacancyBoardStudent.class);
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


        Query query = FStore.collection("Application").whereEqualTo("student_num", UserIDStatic.getInstance().getUserId()).orderBy("docId", Query.Direction.DESCENDING);

        query.get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {


                QuerySnapshot StudApplications = task.getResult();
                List<Application> applicationList = new ArrayList<>();
                if (!StudApplications.isEmpty() && StudApplications != null) {

                    List<Task<DocumentSnapshot>> tasks = new ArrayList<>();

                    for (DocumentSnapshot documentSnapshot : StudApplications.getDocuments()) {
                        Application application = documentSnapshot.toObject(Application.class);

                        DocumentReference docVac = FStore.collection("Vacancy").document(application.getVacancy_id());
                        Task<DocumentSnapshot> documentSnapshotTask = docVac.get();
                        tasks.add(documentSnapshotTask);
                        applicationList.add(application);
                    }

                    Tasks.whenAllSuccess(tasks.toArray(new Task[tasks.size()])).addOnSuccessListener(objects -> {
                        for (int i = 0; i < tasks.size(); i++) {
                            DocumentSnapshot VacDetails = tasks.get(i).getResult();
                            if (VacDetails.exists()) {
                                Application application = applicationList.get(i);
                                application.setModule(VacDetails.getString("module"));
                                application.setType(VacDetails.getString("type"));
                                application.setDescription(VacDetails.getString("description"));
                                application.setPersonName(VacDetails.getString("lecturer"));
                                application.setSemester(VacDetails.getString("semester"));
                                application.setSalary(VacDetails.getString("salary"));
                            }
                        }

                        runOnUiThread(() -> setAdapter(applicationList, progressDialog));
                    });


                } else {
                    runOnUiThread(() -> setAdapter(applicationList, progressDialog));
                }

            } else {
                runOnUiThread(() -> setAdapter(new ArrayList<>(), progressDialog));
            }

        });


    }

    private void setAdapter(List<Application> applicationList, ProgressDialog p) {
        p.dismiss();
        ApplicationAdapterStudent applicationAdapterStudent = new ApplicationAdapterStudent(applicationList, this);
        ApplicationsRV.setAdapter(applicationAdapterStudent);

        LinearLayoutManager layoutManager = new LinearLayoutManager(ApplicationStatusStudent.this);
        ApplicationsRV.setLayoutManager(layoutManager);
    }

    @Override
    public void onBackPressed() {
        return;
    }
}