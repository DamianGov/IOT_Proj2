package com.example.iot_proj2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Notice_Board_Student extends AppCompatActivity {

    @BindView(R.id.rvStudNoticeBoard)
    RecyclerView NoticeBoard;

    private NavigationView nav_View;

    private FirebaseFirestore FStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_board_student);

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
                case R.id.mStudentVacancyBoard: {
                    Intent intent = new Intent(this, VacancyBoardStudent.class);
                    startActivity(intent);
                }
                break;
                case R.id.mStudentProfile: {
                    Intent intent = new Intent(this, ProfileStudent.class);
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

        DocumentReference docStud = FStore.collection("Student").document(UserIDStatic.getInstance().getUserId());

        docStud.get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                DocumentSnapshot student = task.getResult();

                Query query = FStore.collection("Note").whereEqualTo("faculty",student.getString("faculty")).whereEqualTo("expired",false).orderBy("docId", Query.Direction.DESCENDING);

                query.get().addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful())
                    {
                        QuerySnapshot notes = task1.getResult();
                        List<Notice> noticeList = new ArrayList<>();
                        if(!notes.isEmpty())
                        {
                            for (DocumentSnapshot documentSnapshot : notes.getDocuments())
                            {
                                Notice notice = documentSnapshot.toObject(Notice.class);
                                noticeList.add(notice);
                            }
                            runOnUiThread(() -> setAdapter(noticeList));
                        } else {
                            runOnUiThread(() -> setAdapter(noticeList));
                        }
                    }
                });
            }
        });

    }
    private void setAdapter(List<Notice> noticeList) {
        NoticeAdapterStudent noticeAdapterStudent = new NoticeAdapterStudent(noticeList, this);
        NoticeBoard.setAdapter(noticeAdapterStudent);

        LinearLayoutManager layoutManager = new LinearLayoutManager(Notice_Board_Student.this);
        NoticeBoard.setLayoutManager(layoutManager);
    }

    @Override
    public void onBackPressed() {
        return;
    }
}