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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ApplicationStatusStudent extends AppCompatActivity {

    @BindView(R.id.rvStudApplications)
    RecyclerView ApplicationsRV;


    private FirebaseFirestore FStore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_status_student);

        ButterKnife.bind(this);

        FStore = FirebaseFirestore.getInstance();

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("");
        progressDialog.setMessage("Loading Applications...");
        progressDialog.setCancelable(false);
        progressDialog.show();


        Query query = FStore.collection("Application").whereEqualTo("student_num",UserIDStatic.getInstance().getUserId()).orderBy("docId",Query.Direction.DESCENDING);

        query.get().addOnCompleteListener(task -> {

          if(task.isSuccessful()) {


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
                      for(int i = 0; i < tasks.size(); i++)
                      {
                          DocumentSnapshot VacDetails = tasks.get(i).getResult();
                          if(VacDetails.exists())
                          {
                              Application application = applicationList.get(i);
                              application.setModule(VacDetails.getString("module"));
                              application.setType(VacDetails.getString("type"));
                              application.setDescription(VacDetails.getString("description"));
                              application.setPersonName(VacDetails.getString("lecturer"));
                          }
                      }

                      runOnUiThread(()-> setAdapter(applicationList, progressDialog));
                  });




              }

          }

        });


    }

    private void setAdapter(List<Application> applicationList, ProgressDialog p)
    {
        p.dismiss();
        ApplicationAdapterStudent applicationAdapterStudent = new ApplicationAdapterStudent(applicationList, this);
        ApplicationsRV.setAdapter(applicationAdapterStudent);

        LinearLayoutManager layoutManager = new LinearLayoutManager(ApplicationStatusStudent.this);
        ApplicationsRV.setLayoutManager(layoutManager);
    }
}