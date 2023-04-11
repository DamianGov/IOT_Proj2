package com.example.iot_proj2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileLecturer extends AppCompatActivity {

    @BindView(R.id.edtPLecName)
    EditText LecName;

    @BindView(R.id.edtPLecFullName)
    EditText LecFullName;

    @BindView(R.id.edtPLecStaffNum)
    EditText LecStaffNum;

    @BindView(R.id.edtPLecEmail)
    EditText LecEmail;

    @BindView(R.id.edtPLecFaculty)
    EditText LecFac;

    @BindView(R.id.edtPLecDepart)
    EditText LecDepar;

    @BindView(R.id.edtPLecModule)
    EditText LecModule;

    private FirebaseFirestore FStore;

    // TODO: Remove this button
    @BindView(R.id.btnLecGoToCreateVac)
    Button GoToVac;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_lecturer);

        ButterKnife.bind(this);

        FStore = FirebaseFirestore.getInstance();

        String StaffNum = UserIDStatic.getInstance().getUserId();

        DocumentReference docRef = FStore.collection("Lecturer").document(StaffNum);
        docRef.addSnapshotListener((value, error) -> {
            LecName.setText(value.getString("name"));
            LecFullName.setText(value.getString("name"));
            LecStaffNum.setText(StaffNum);
            LecEmail.setText(value.getString("email"));
            LecFac.setText(value.getString("faculty"));
            LecDepar.setText(value.getString("department"));
            LecModule.setText(value.getString("module"));
        });

        // TODO: Remove this code:
        GoToVac.setOnClickListener(view -> {
            Intent intent = new Intent(this, CreateVacancyLecturer.class);
            startActivity(intent);
        });
    }
    @Override
    public void onBackPressed()
    {
        return;
    }
}