package com.example.iot_proj2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_vacancy_lecturer);

        ButterKnife.bind(this);

        FStore = FirebaseFirestore.getInstance();

        String StaffNum = UserIDStatic.getInstance().getUserId();

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
                String description = Description.getText().toString().trim();
                String position;
                if (Tutor.isChecked())
                    position = "Tutor";
                else
                    position = "Teaching Assistant";

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

                    String DocID = Integer.toString(maxid);

                    DocumentReference newVac = FStore.collection("Vacancy").document(DocID);
                    Map<String, Object> dataVals = new HashMap<>();
                    dataVals.put("module",module);
                    dataVals.put("description",description);
                    dataVals.put("status","1");
                    dataVals.put("type",position);
                    dataVals.put("created_by",UserIDStatic.getInstance().getUserId());
                    dataVals.put("docId",maxid);
                    dataVals.put("lecturer",lecDoc.getString("name"));

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
}