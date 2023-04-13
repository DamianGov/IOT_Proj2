package com.example.iot_proj2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AppointmentStatusStudent extends AppCompatActivity {

    @BindView(R.id.rvStudAppointments)
    RecyclerView StudAppointments;


    private FirebaseFirestore FStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_status_student);

        ButterKnife.bind(this);

        FStore = FirebaseFirestore.getInstance();

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("");
        progressDialog.setMessage("Loading Appointments...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Query query = FStore.collection("Appointment").whereEqualTo("stud_num",UserIDStatic.getInstance().getUserId()).orderBy("docId",Query.Direction.DESCENDING);;
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

            if (!StudAppointments.isEmpty() && StudAppointments != null)
            {
                List<Task<DocumentSnapshot>> taskGetLecturer = new ArrayList<>();

                for (DocumentSnapshot documentSnapshot : StudAppointments.getDocuments()) {
                    Appointment appointment = documentSnapshot.toObject(Appointment.class);

                    try {
                        LocalDate givenDate  = LocalDate.parse(appointment.getStart_time(),givenFormat);
                        if(givenDate.isAfter(currentDate))
                        {
                            appointment.setStudentEmail(studEmail);
                            appointment.setStudentName(studName);

                            DocumentReference lecRef = FStore.collection("Lecturer").document(appointment.getStaff_num());
                            Task<DocumentSnapshot> lecSnap = lecRef.get();
                            taskGetLecturer.add(lecSnap);

                            appointmentList.add(appointment);
                        }
                    } catch (Exception e)
                    {
                        appointment.setStudentEmail(studEmail);
                        appointment.setStudentName(studName);

                        DocumentReference lecRef = FStore.collection("Lecturer").document(appointment.getStaff_num());
                        Task<DocumentSnapshot> lecSnap = lecRef.get();
                        taskGetLecturer.add(lecSnap);

                        appointmentList.add(appointment);
                    }


                }

                Tasks.whenAllSuccess(taskGetLecturer.toArray(new Task[taskGetLecturer.size()])).addOnSuccessListener(objects1 -> {
                    for(int i = 0; i < taskGetLecturer.size(); i++)
                    {
                        DocumentSnapshot lecDetails = taskGetLecturer.get(i).getResult();
                            Appointment appointment = appointmentList.get(i);
                            appointment.setLecturerEmail(lecDetails.getString("email"));
                            appointment.setLecturerName(lecDetails.getString("name"));

                    }

                    runOnUiThread(() -> setAdapter(appointmentList, progressDialog));
                });
            }
        });


    }
    private void setAdapter(List<Appointment> appointmentList, ProgressDialog p)
    {
        p.dismiss();
        AppointmentAdapterStudent appointmentAdapterStudent = new AppointmentAdapterStudent(appointmentList, this);
        StudAppointments.setAdapter(appointmentAdapterStudent);

        LinearLayoutManager layoutManager = new LinearLayoutManager(AppointmentStatusStudent.this);
        StudAppointments.setLayoutManager(layoutManager);
    }
}