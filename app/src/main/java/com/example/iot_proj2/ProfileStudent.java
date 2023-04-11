package com.example.iot_proj2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileStudent extends AppCompatActivity {

    @BindView(R.id.edtPStudName)
    EditText ProfStudName;

    @BindView(R.id.edtPStudFullName)
    EditText ProfFullName;

    @BindView(R.id.edtPStudStudentNum)
    EditText ProfStudNum;

    @BindView(R.id.edtPStudEmail)
    EditText ProfEmail;

    @BindView(R.id.edtPStudID)
    EditText ProfID;

    @BindView(R.id.edtPStudFaculty)
    EditText ProfFac;

    @BindView(R.id.edtPStudCourse)
    EditText ProfCourse;

    @BindView(R.id.edtPStudPosition)
    EditText ProfPost;


    private FirebaseFirestore FStore;

    // TODO: Remove the following button
    @BindView(R.id.btnTempResume)
    Button tempResume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_student);

        ButterKnife.bind(this);

        FStore = FirebaseFirestore.getInstance();

        String StudentNum = UserIDStatic.getInstance().getUserId();

        DocumentReference docRef = FStore.collection("Student").document(StudentNum);
        docRef.addSnapshotListener((value, error) -> {
            ProfStudName.setText(value.getString("name"));
            ProfFullName.setText(value.getString("name"));
            ProfStudNum.setText(StudentNum);
            ProfEmail.setText(value.getString("email"));
            ProfID.setText(value.getString("ID"));
            ProfFac.setText(value.getString("faculty"));
            ProfCourse.setText(value.getString("course"));
        });




        DocumentReference PosTypeRef = FStore.collection("Tutor").document(StudentNum);
        PosTypeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                DocumentSnapshot snap = task.getResult();
                if(snap.exists())
                {
                    ProfPost.setText(snap.getString("type"));
                } else
                {
                    ProfPost.setText("Student");
                }
            }
        });

        // TODO: Remove the following
        tempResume.setOnClickListener(view -> {
            Intent intent = new Intent(this, VacancyBoardStudent.class);
            startActivity(intent);
        });
    }

    @Override
    public void onBackPressed()
    {
        return;
    }
}