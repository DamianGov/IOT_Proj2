package com.example.iot_proj2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import butterknife.BindView;
import butterknife.ButterKnife;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btnLogin)
    Button btnLogin;

    @BindView(R.id.edtUNumberLogin)
    EditText edtUserNumber;

    @BindView(R.id.edtUPasswordLogin)
    EditText edtPassword;

    @BindView(R.id.rbStudent)
    RadioButton UserStudent;

    @BindView(R.id.rbLecturer)
    RadioButton UserLect;

    private FirebaseFirestore FStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        FStore = FirebaseFirestore.getInstance();


        btnLogin.setOnClickListener(view -> {

            String UserType = "";
            if (UserStudent.isChecked())
                UserType = "Student";
            else UserType = "Lecturer";

            String UserNumber = edtUserNumber.getText().toString();
            String Password = edtPassword.getText().toString();

            if(TextUtils.isEmpty(UserNumber))
            {
                edtUserNumber.setError("Enter your User Number");
                return;
            }

            if(!TextUtils.isDigitsOnly(UserNumber))
            {
                edtUserNumber.setError("Invalid User Number");
                return;
            }
            if(TextUtils.isEmpty(Password))
            {
                edtPassword.setError("Enter your Password");
                return;
            }

            if(Password.length() < 6)
            {
                edtPassword.setError("Password is too short. The Password must be more than 6 characters");
                return;
            }

            if(Password.length() > 20)
            {
                edtPassword.setError("Password is too long. The Password must be less than 20 characters");
                return;
            }

            DocumentReference userDoc = FStore.collection(UserType).document(UserNumber);

            String finalUserType = UserType;
            userDoc.get().addOnCompleteListener(task -> {
                if (task.isSuccessful())
                {
                    DocumentSnapshot userSnap = task.getResult();
                    if(userSnap.exists())
                    {
                        if (BCrypt.checkpw(Password, userSnap.getString("password")))
                        {
                            UserIDStatic.getInstance().setUserId(UserNumber);
                            UserIDStatic.getInstance().setUserType(finalUserType);
                            Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            // TODO: Go to vacancy
                            if(finalUserType == "Student")
                                openProfileStud();
                            else
                                openProfileLect();
                        }
                        else{
                            Toast.makeText(MainActivity.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Error: " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                    return;
                }
            });


        });


    }
    // TODO: Needs to change to Student Vacancy
    public void openProfileStud()
    {
        Intent intent = new Intent(this, ProfileStudent.class);
        startActivity(intent);
    }

    public void openProfileLect()
    {
        Intent intent = new Intent(this, ProfileLecturer.class);
        startActivity(intent);
    }
}