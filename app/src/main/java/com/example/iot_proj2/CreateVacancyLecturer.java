package com.example.iot_proj2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.firestore.local.QueryResult;

import org.checkerframework.checker.units.qual.A;
import org.w3c.dom.Document;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateVacancyLecturer extends AppCompatActivity {

    @BindView(R.id.spnLecCVModule)
    Spinner Module;

    @BindView(R.id.spnLecCVSemester)
    Spinner Semester;

    @BindView(R.id.edtLecCVSalary)
    EditText Salary;

    @BindView(R.id.edtLecCVDescript)
    EditText Description;

    @BindView(R.id.rbLecCVTutor)
    RadioButton Tutor;

    @BindView(R.id.rbLecCVTeachingAssist)
    RadioButton TAssist;

    @BindView(R.id.btnLecCVSubmit)
    Button Submit;

    private FirebaseFirestore FStore;

    private String modules;

    private NavigationView nav_View;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_vacancy_lecturer);

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
                case R.id.mLecturerProfile: {
                    Intent intent = new Intent(this, ProfileLecturer.class);
                    startActivity(intent);
                }
                break;
                case R.id.mLecturerAppointmentStatus: {
                    Intent intent = new Intent(this, AppointmentStatusLecturer.class);
                    startActivity(intent);
                }
                break;
                case R.id.mLecturerApplicationAcceptStatus: {
                    Intent intent = new Intent(this, ApplicationStatusAccepted.class);
                    startActivity(intent);
                }
                break;
                case R.id.mLecturerVacancyBoard: {
                    Intent intent = new Intent(this, VacancyBoardLecturer.class);
                    startActivity(intent);
                }
                break;
                case R.id.mLecturerApplicationStatus: {
                    Intent intent = new Intent(this, ApplicationStatusLecturer.class);
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

        FStore = FirebaseFirestore.getInstance();

        String StaffNum = UserIDStatic.getInstance().getUserId();

        String[] semester = {"Semester 1","Semester 2"};

        ArrayAdapter<String> SemAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,semester);
        Semester.setAdapter(SemAdapter);
        Semester.setPrompt("Select Semester");


        DocumentReference docRef = FStore.collection("Lecturer").document(StaffNum);

        docRef.addSnapshotListener((value, error) -> {
            modules = value.getString("module");

            String[] modulesSplit = modules.split(",");
            ArrayAdapter<String> ModAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modulesSplit);
            Module.setAdapter(ModAdapter);
            Module.setPrompt("Modules");
        });

        Submit.setOnClickListener(view -> {
                String module = Module.getSelectedItem().toString();
                String salary = Salary.getText().toString();
                String description = Description.getText().toString().trim();
                String position;
                if (Tutor.isChecked())
                    position = "Tutor";
                else
                    position = "Teaching Assistant";

                String semsterChosen;
                int SemesterIndex = Semester.getSelectedItemPosition();
                if (SemesterIndex == 0)
                    semsterChosen = "1";
                else
                    semsterChosen = "2";

                if(salary.length() == 0)
                {
                    Salary.setError("Please enter the Salary per/hour");
                    return;
                }

                salary = "R" + salary;

                if (description.length() == 0)
                {
                    Description.setError("Please enter a Description");
                    return;
                }

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("");
            progressDialog.setMessage("Creating Vacancy...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            Query getLatestId = FStore.collection("Vacancy").orderBy("docId", Query.Direction.DESCENDING).limit(1);
            DocumentReference docLec = FStore.collection("Lecturer").document(UserIDStatic.getInstance().getUserId());

                Task<QuerySnapshot> task = getLatestId.get();
                Task<DocumentSnapshot> GetLec = docLec.get();

            String finalSalary = salary;
            Tasks.whenAllSuccess(task,GetLec).addOnSuccessListener(objects -> {
                    QuerySnapshot taskResult = task.getResult();
                    DocumentSnapshot lecDoc = GetLec.getResult();
                    int maxid = 1;
                    if(!taskResult.isEmpty() && taskResult != null)
                    {
                        DocumentSnapshot documentSnapshot = taskResult.getDocuments().get(0);
                        long id = documentSnapshot.getLong("docId");
                        maxid = (int) id + 1;
                    }

                    String lecturerName = lecDoc.getString("name");
                    String DocID = Integer.toString(maxid);

                    DocumentReference newVac = FStore.collection("Vacancy").document(DocID);
                    Map<String, Object> dataVals = new HashMap<>();
                    dataVals.put("module",module);
                    dataVals.put("description",description);
                    dataVals.put("semester",semsterChosen);
                    dataVals.put("salary", finalSalary);
                    dataVals.put("status","1");
                    dataVals.put("type",position);
                    dataVals.put("created_by",UserIDStatic.getInstance().getUserId());
                    dataVals.put("docId",maxid);
                    dataVals.put("lecturer",lecturerName);

                new Thread(() -> {
                    Query allDevices = FStore.collection("Device_Token").whereNotEqualTo("token", UserIDStatic.getInstance().getToken());

                    allDevices.get().addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful())
                        {
                            QuerySnapshot devices = task1.getResult();
                            if(!devices.isEmpty())
                            {
                                for(QueryDocumentSnapshot queryDocumentSnapshot : devices)
                                {
                                    SendPushNotification.pushNotification(CreateVacancyLecturer.this,queryDocumentSnapshot.getString("token"),"New Vacancy",lecturerName+" created a vacancy. Check it out...");
                                }
                            }
                        }
                    });
                }).start();

                    newVac.set(dataVals).addOnSuccessListener(unused -> {
                        progressDialog.dismiss();

                        runOnUiThread(() -> {
                            Toast.makeText(this, "Vacancy has been created", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(CreateVacancyLecturer.this, VacancyBoardLecturer.class);
                                    startActivity(intent);
                                    finish();
                        }
                        );

                    })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                runOnUiThread(() -> Toast.makeText(this, "Error: Unable to create Vacancy", Toast.LENGTH_SHORT).show());
                                return;
                            });



                });


        });
    }
    @Override
    public void onBackPressed()
    {
        return;
    }
}