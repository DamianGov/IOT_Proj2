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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AppointmentStatusStudent extends AppCompatActivity {

    @BindView(R.id.rvStudAppointments)
    RecyclerView StudAppointments;


    private FirebaseFirestore FStore;

    private NavigationView nav_View;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_status_student);

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
        progressDialog.setMessage("Loading Appointments...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Query query = FStore.collection("Appointment").whereEqualTo("stud_num", UserIDStatic.getInstance().getUserId()).orderBy("docId", Query.Direction.DESCENDING);
        DocumentReference studentDetails = FStore.collection("Student").document(UserIDStatic.getInstance().getUserId());

        Task<QuerySnapshot> getAppointments = query.get();
        Task<DocumentSnapshot> getStudent = studentDetails.get();

        Tasks.whenAllSuccess(getAppointments, getStudent).addOnSuccessListener(objects -> {
            QuerySnapshot StudAppointments = getAppointments.getResult();
            DocumentSnapshot StudentDetails = getStudent.getResult();

            String studName = StudentDetails.getString("name");
            String studEmail = StudentDetails.getString("email");

            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter givenFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");


            List<Appointment> appointmentList = new ArrayList<>();

            if (!StudAppointments.isEmpty() && StudAppointments != null) {
                List<Task<DocumentSnapshot>> taskGetLecturer = new ArrayList<>();

                for (DocumentSnapshot documentSnapshot : StudAppointments.getDocuments()) {
                    Appointment appointment = documentSnapshot.toObject(Appointment.class);

                    try {
                        LocalDate givenDate = LocalDate.parse(appointment.getStart_time(), givenFormat);
                        if (givenDate.isAfter(currentDate)) {
                            appointment.setStudentEmail(studEmail);
                            appointment.setStudentName(studName);

                            DocumentReference lecRef = FStore.collection("Lecturer").document(appointment.getStaff_num());
                            Task<DocumentSnapshot> lecSnap = lecRef.get();
                            taskGetLecturer.add(lecSnap);

                            appointmentList.add(appointment);
                        }
                    } catch (Exception e) {
                        appointment.setStudentEmail(studEmail);
                        appointment.setStudentName(studName);

                        DocumentReference lecRef = FStore.collection("Lecturer").document(appointment.getStaff_num());
                        Task<DocumentSnapshot> lecSnap = lecRef.get();
                        taskGetLecturer.add(lecSnap);

                        appointmentList.add(appointment);
                    }


                }

                Tasks.whenAllSuccess(taskGetLecturer.toArray(new Task[taskGetLecturer.size()])).addOnSuccessListener(objects1 -> {
                    for (int i = 0; i < taskGetLecturer.size(); i++) {
                        DocumentSnapshot lecDetails = taskGetLecturer.get(i).getResult();
                        Appointment appointment = appointmentList.get(i);
                        appointment.setLecturerEmail(lecDetails.getString("email"));
                        appointment.setLecturerName(lecDetails.getString("name"));

                    }

                    runOnUiThread(() -> setAdapter(appointmentList, progressDialog));
                });
            } else {
                runOnUiThread(() -> setAdapter(appointmentList, progressDialog));
            }
        });


    }

    private void setAdapter(List<Appointment> appointmentList, ProgressDialog p) {
        p.dismiss();
        AppointmentAdapterStudent appointmentAdapterStudent = new AppointmentAdapterStudent(appointmentList, this);
        StudAppointments.setAdapter(appointmentAdapterStudent);

        LinearLayoutManager layoutManager = new LinearLayoutManager(AppointmentStatusStudent.this);
        StudAppointments.setLayoutManager(layoutManager);
    }

    @Override
    public void onBackPressed() {
        return;
    }
}