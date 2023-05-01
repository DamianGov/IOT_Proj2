package com.example.iot_proj2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateNoticeLecturer extends AppCompatActivity {

    @BindView(R.id.edtLecCNTitle)
    EditText Title;

    @BindView(R.id.edtLecCNDescript)
    EditText Description;

    @BindView(R.id.btnLecCNSubmit)
    Button Submit;

    private FirebaseFirestore FStore;

    private NavigationView nav_View;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_notice_lecturer);

        ButterKnife.bind(this);

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
                case R.id.mLecturerAppointmentStatus:
                {
                    Intent intent = new Intent(this, AppointmentStatusLecturer.class);
                    startActivity(intent);
                }
                break;
                case R.id.mLecturerCreateVac: {
                    Intent intent = new Intent(this, CreateVacancyLecturer.class);
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

        Submit.setOnClickListener(view -> {
           String title = Title.getText().toString().trim();
           String description = Description.getText().toString().trim();

           if (title.length() == 0)
           {
               Title.setError("Please enter a Title");
               return;
           }

           if (description.length() == 0)
           {
               Description.setError("Please enter a Description");
               return;
           }

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("");
            progressDialog.setMessage("Creating Notice...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            Query getLatestId = FStore.collection("Note").orderBy("docId", Query.Direction.DESCENDING).limit(1);
            DocumentReference docLec = FStore.collection("Lecturer").document(UserIDStatic.getInstance().getUserId());

            Task<QuerySnapshot> task = getLatestId.get();
            Task<DocumentSnapshot> GetLec = docLec.get();

            Tasks.whenAllSuccess(task, GetLec).addOnSuccessListener(objects -> {
                QuerySnapshot taskResult = task.getResult();
                DocumentSnapshot lecDoc = GetLec.getResult();

                int maxid = 1;
                if (!taskResult.isEmpty() && taskResult != null) {
                    DocumentSnapshot documentSnapshot = taskResult.getDocuments().get(0);
                    long id = documentSnapshot.getLong("docId");
                    maxid = (int) id + 1;
                }

                String lecturerName = lecDoc.getString("name");
                String DocID = Integer.toString(maxid);

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                String currentDateAndTime = dateFormat.format(calendar.getTime());


                DocumentReference newNote = FStore.collection("Note").document(DocID);
                Map<String, Object> data = new HashMap<>();
                data.put("lecturer", lecturerName);
                data.put("title", title);
                data.put("description", description);
                data.put("time",currentDateAndTime);
                data.put("created_by", UserIDStatic.getInstance().getUserId());
                data.put("expired",false);
                data.put("faculty",lecDoc.getString("faculty"));
                data.put("docId", maxid);

                new Thread(() -> {
                    Query allDevices = FStore.collection("Device_Token").whereNotEqualTo("token", UserIDStatic.getInstance().getToken());

                    allDevices.get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            QuerySnapshot devices = task1.getResult();
                            if (!devices.isEmpty()) {
                                for (QueryDocumentSnapshot queryDocumentSnapshot : devices) {
                                    if (queryDocumentSnapshot.getString("faculty").equals(lecDoc.getString("faculty"))) {
                                        SendPushNotification.pushNotification(CreateNoticeLecturer.this, queryDocumentSnapshot.getString("token"), "New Notice", lecturerName + " sent out a new notice!");
                                    }
                                }
                            }
                        }
                    });
                }).start();

                newNote.set(data).addOnSuccessListener(unused -> {
                            progressDialog.dismiss();

                            runOnUiThread(() -> {
                                        Toast.makeText(this, "Notice has been created", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(CreateNoticeLecturer.this, NoticeBoardLecturer.class);
                                        startActivity(intent);
                                        finish();
                                    }
                            );

                        })
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            runOnUiThread(() -> Toast.makeText(this, "Error: Unable to create Notice", Toast.LENGTH_SHORT).show());
                            return;
                        });
                });
            });

        }

    @Override
    public void onBackPressed() {
        return;
    }
    }

