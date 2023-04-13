package com.example.iot_proj2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AppointmentStatusLecturer extends AppCompatActivity {

    @BindView(R.id.rvLecAppointments)
    RecyclerView LecAppointments;


    private FirebaseFirestore FStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_status_lecturer);

        ButterKnife.bind(this);

        FStore = FirebaseFirestore.getInstance();

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("");
        progressDialog.setMessage("Loading Appointments...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Query query = FStore.collection("Appointment").whereEqualTo("staff_num",UserIDStatic.getInstance().getUserId()).orderBy("docId",Query.Direction.DESCENDING);
        DocumentReference lecDet = FStore.collection("Lecturer").document(UserIDStatic.getInstance().getUserId());

        Task<QuerySnapshot> queryTask = query.get();
        Task<DocumentSnapshot> lecGet = lecDet.get();

        Tasks.whenAllSuccess(queryTask,lecGet).addOnSuccessListener(objects -> {
            QuerySnapshot LecApp = queryTask.getResult();
            DocumentSnapshot LecDetails = lecGet.getResult();

            String lecName = LecDetails.getString("name");
            String lecEmail = LecDetails.getString("email");

            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter givenFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

            List<Appointment> appointmentList = new ArrayList<>();

            if(!LecApp.isEmpty() && LecApp != null)
            {
                List<Task<DocumentSnapshot>> taskGetStudent = new ArrayList<>();


                for(DocumentSnapshot documentSnapshot : LecApp.getDocuments())
                {
                    Appointment appointment = documentSnapshot.toObject(Appointment.class);

                    try {
                        LocalDate givenDate = LocalDate.parse(appointment.getStart_time(), givenFormat);
                        if (givenDate.isAfter(currentDate)) {

                            appointment.setLecturerName(lecName);
                            appointment.setLecturerEmail(lecEmail);

                            DocumentReference studRef = FStore.collection("Student").document(appointment.getStud_num());
                            Task<DocumentSnapshot> studSnap = studRef.get();
                            taskGetStudent.add(studSnap);

                            appointmentList.add(appointment);

                        }
                    }catch (Exception e)
                        {
                            appointment.setLecturerName(lecName);
                            appointment.setLecturerEmail(lecEmail);

                            DocumentReference studRef = FStore.collection("Student").document(appointment.getStud_num());
                            Task<DocumentSnapshot> studSnap = studRef.get();
                            taskGetStudent.add(studSnap);

                            appointmentList.add(appointment);
                        }
                }

                Tasks.whenAllSuccess(taskGetStudent.toArray(new Task[taskGetStudent.size()])).addOnSuccessListener(objects1 -> {
                        for(int i = 0; i < taskGetStudent.size(); i++)
                        {
                            DocumentSnapshot studDetails = taskGetStudent.get(i).getResult();
                                Appointment appointment = appointmentList.get(i);
                                appointment.setStudentName(studDetails.getString("name"));
                                appointment.setStudentEmail(studDetails.getString("email"));
                        }

                    runOnUiThread(() -> setAdapter(appointmentList, progressDialog));
                });

            }


        });

    }
    private void setAdapter(List<Appointment> appointmentList, ProgressDialog p)
    {
        p.dismiss();
        AppointmentAdapter appointmentAdapter = new AppointmentAdapter(appointmentList, this);
        LecAppointments.setAdapter(appointmentAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(AppointmentStatusLecturer.this);
        LecAppointments.setLayoutManager(layoutManager);
    }
}