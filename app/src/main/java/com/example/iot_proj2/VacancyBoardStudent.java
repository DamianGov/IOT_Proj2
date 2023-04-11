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

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VacancyBoardStudent extends AppCompatActivity {

    @BindView(R.id.rvStudVacBoard)
    RecyclerView VacBoard;

    private FirebaseFirestore FStore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacancy_board_student);

        ButterKnife.bind(this);

        FStore = FirebaseFirestore.getInstance();

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("");
        progressDialog.setMessage("Loading Vacancies...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Query queryVac = FStore.collection("Vacancy").whereEqualTo("status","1");
        DocumentReference docRefStud = FStore.collection("Student").document(UserIDStatic.getInstance().getUserId());

        Task<QuerySnapshot> querySnapshotVac = queryVac.get();
        Task<DocumentSnapshot> documentSnapshotStud = docRefStud.get();

        Tasks.whenAllSuccess(querySnapshotVac, documentSnapshotStud).addOnSuccessListener(objects -> {
            QuerySnapshot querySnapVac = querySnapshotVac.getResult();
            DocumentSnapshot documentSnapStud = documentSnapshotStud.getResult();
            List<Vacancy> vacancyList = new ArrayList<>();

            if ((querySnapVac != null && !querySnapVac.isEmpty()) && documentSnapStud.exists())
            {
                List<Task<QuerySnapshot>> tasks = new ArrayList<>();
                for (DocumentSnapshot documentSnapshot : querySnapVac.getDocuments()) {
                    Vacancy vacancy = documentSnapshot.toObject(Vacancy.class);

                    Query queryExistApp = FStore.collection("Application").whereEqualTo("student_num", UserIDStatic.getInstance().getUserId())
                            .whereEqualTo("vacancy_id", vacancy.getDocId());
                    Task<QuerySnapshot> queryExistingApplications = queryExistApp.get();
                    tasks.add(queryExistingApplications); // Add the task to the list of tasks
                }

                // Use Tasks.whenAllSuccess to wait for all tasks to complete
                Tasks.whenAllSuccess(tasks.toArray(new Task[tasks.size()])).addOnSuccessListener(objects2 -> {
                    for (int i = 0; i < tasks.size(); i++) {
                        QuerySnapshot ExistingApp = tasks.get(i).getResult();
                        if (ExistingApp.isEmpty()) {
                            vacancyList.add(querySnapVac.getDocuments().get(i).toObject(Vacancy.class));
                        }
                    }

                    // Call setAdapter method after all iterations are fully complete
                    // Note: It's important to perform UI related tasks on the main thread using runOnUiThread or similar methods
                    runOnUiThread(() -> {
                        setAdapter(vacancyList,progressDialog);
                    });
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
    }
