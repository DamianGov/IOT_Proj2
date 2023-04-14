package com.example.iot_proj2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import butterknife.BindView;
import butterknife.ButterKnife;


public class CreateAppointmentStudent extends AppCompatActivity {
    @BindView(R.id.edtCreateAppointmentDate)
    EditText DateApp;

    @BindView(R.id.spnCreateAppointmentTime)
    Spinner TimeApp;

    @BindView(R.id.spnCreateAppointmentLecturer)
    Spinner Lecturer;

    @BindView(R.id.btnSubmitBooking)
    Button Submit;


    private ArrayList<String> lecturerNames = new ArrayList<>();
    private ArrayList<String> lecturerNumbers = new ArrayList<>();

    private boolean ValidDate = false;

    private NavigationView nav_View;

    private FirebaseFirestore FStore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_appointment_student);

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

            switch (id)
            {
                case R.id.mStudentProfile: {
                    Intent intent = new Intent(this, ProfileStudent.class);
                    startActivity(intent);
                }
                break;
                case R.id.mStudentApplicationStatus:
                {
                    Intent intent = new Intent(this, ApplicationStatusStudent.class);
                    startActivity(intent);
                }
                break;
                case R.id.mStudentAppointmentStatus:{
                    Intent intent = new Intent(this, AppointmentStatusStudent.class);
                    startActivity(intent);
                }
                break;
                case R.id.mStudentUpdateResume:{
                    Intent intent = new Intent(this, ResumeStudent.class);
                    startActivity(intent);
                }
                break;
                case R.id.mStudentVacancyBoard:{
                    Intent intent = new Intent(this, VacancyBoardStudent.class);
                    startActivity(intent);
                }
                break;
                case R.id.mLogOut:{
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                break;
            }

            drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        });



        ArrayAdapter<String> adapterTime = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, StaticStrings.TimeStringForView);
        TimeApp.setAdapter(adapterTime);

        FStore = FirebaseFirestore.getInstance();

        CollectionReference lecturerCol = FStore.collection("Lecturer");

        lecturerCol.get().addOnCompleteListener(task -> {
          if (task.isSuccessful())
            {
                lecturerNames = new ArrayList<>();
                lecturerNumbers = new ArrayList<>();

                for (QueryDocumentSnapshot document : task.getResult())
                {
                    lecturerNames.add(document.getString("name"));
                    lecturerNumbers.add(document.getId());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lecturerNames);
                Lecturer.setAdapter(adapter);
            }
        });



        Submit.setOnClickListener(view -> {
                if(!ValidDate)
                {
                    Toast.makeText(this, "Please choose a Date", Toast.LENGTH_SHORT).show();
                    DateApp.setError("");
                    return;
                }
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("");
            progressDialog.setMessage("Creating Appointment...");
            progressDialog.setCancelable(false);
            progressDialog.show();

                String dateAppointment = DateApp.getText().toString();
                String timeAppointment = TimeApp.getSelectedItem().toString();

                String joinedDateTime = dateAppointment + " " +  StaticStrings.TimeString[TimeApp.getSelectedItemPosition()];

                String lecturerStaffNum = lecturerNumbers.get(Lecturer.getSelectedItemPosition());

                String lecturerName = Lecturer.getSelectedItem().toString();

                Query AppointmentsQuery = FStore.collection("Appointment").whereEqualTo("staff_num",lecturerStaffNum).whereEqualTo("start_time",joinedDateTime).whereEqualTo("status","approved");
                Query getLatestId = FStore.collection("Appointment").orderBy("docId", Query.Direction.DESCENDING).limit(1);
                DocumentReference StudentDetails = FStore.collection("Student").document(UserIDStatic.getInstance().getUserId());

                Task<QuerySnapshot> appointmentsQuery = AppointmentsQuery.get();
                Task<QuerySnapshot> latestId = getLatestId.get();
                Task<DocumentSnapshot> stud = StudentDetails.get();


            Tasks.whenAllSuccess(appointmentsQuery, latestId, stud).addOnSuccessListener(objects -> {
                QuerySnapshot AppTaken = appointmentsQuery.getResult();
                QuerySnapshot LatestID = latestId.getResult();
                DocumentSnapshot Student = stud.getResult();

                String studEmail = Student.getString("email");
                String studName = Student.getString("name");


                if(AppTaken.size() != 0)
                {
                    progressDialog.dismiss();
                    runOnUiThread(() -> Toast.makeText(this, "Chosen Date and Time is already taken.", Toast.LENGTH_SHORT).show());
                    return;
                }

                int maxId =  1;
                if(!LatestID.isEmpty() && LatestID != null)
                {
                    DocumentSnapshot ID = LatestID.getDocuments().get(0);
                    long id = ID.getLong("docId");
                    maxId = (int) id + 1;
                }

                DocumentReference newAppointment =  FStore.collection("Appointment").document(Integer.toString(maxId));
                Map<String, Object> data = new HashMap<>();
                data.put("docId",maxId);
                data.put("staff_num",lecturerStaffNum);
                data.put("start_time",joinedDateTime);
                data.put("status","pending");
                data.put("stud_num", UserIDStatic.getInstance().getUserId());

                newAppointment.set(data).addOnSuccessListener(unused -> {
                    new Thread(()->{
                        SendEmail(studEmail,"Vacancy Portal - Appointment Submitted","Hello, "+studName+".\n\nYour appointment with "+lecturerName+" on "+dateAppointment+" at "+timeAppointment+" has been submitted, and is waiting approval from the lecturer.\n\nThank you.\nKind regards,\nVacancy Team.");
                    }).start();

                    progressDialog.dismiss();
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Appointment has been created", Toast.LENGTH_SHORT).show();

                        openAppointmentStatus();

                    });
                });


            });


        });


    }

    public void showDatePicker(View view) {
        DatePickerDialog datePickerDialog;
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(this,R.style.MyDatePickerStyle , (view1, year1, month1, dayOfMonth) -> {
            String formatedMonth = String.format(Locale.getDefault(),"%02d",(month1 + 1));
            String formatedDay = String.format(Locale.getDefault(),"%02d",dayOfMonth);
            DateApp.setText(year1 +"/"+formatedMonth+"/"+formatedDay);

            Calendar calendar1 = Calendar.getInstance();
            calendar1.set(year1, month1, dayOfMonth);
            int dayOfWeek = calendar1.get(Calendar.DAY_OF_WEEK);

            if(dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY)
            {
                Toast.makeText(this, "The Lecturer is unavailable on weekends", Toast.LENGTH_SHORT).show();
                DateApp.setError("");
                ValidDate = false;
                return;
            } else {
                DateApp.setError(null);
                ValidDate = true;
            }

        }, year, month, day);


        calendar.add(Calendar.DAY_OF_MONTH,1);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if(dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY)
        {
            if(dayOfWeek == Calendar.SATURDAY)
            {
                calendar.add(Calendar.DAY_OF_MONTH,2);
            } else {
                calendar.add(Calendar.DAY_OF_MONTH,1);
            }
        }

        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

        calendar.add(Calendar.WEEK_OF_YEAR, 1);
        long maxDate = calendar.getTimeInMillis();
        datePickerDialog.getDatePicker().setMaxDate(maxDate);


        datePickerDialog.show();
    }

    private void SendEmail(String email, String subject, String body)
    {
        String username = "iotgrp2023@gmail.com";
        String password = "qdqxulmrnbfkrqvg";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message messageObj = new MimeMessage(session);
            messageObj.setFrom(new InternetAddress(username));
            messageObj.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            messageObj.setSubject(subject);
            messageObj.setText(body);
            Transport.send(messageObj);
        } catch (MessagingException e) {

        }
    }

    private void openAppointmentStatus()
    {
        Intent intent = new Intent(this, AppointmentStatusStudent.class);
        startActivity(intent);
    }
    @Override
    public void onBackPressed()
    {
        return;
    }
}