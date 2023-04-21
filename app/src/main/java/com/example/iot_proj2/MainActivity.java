package com.example.iot_proj2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.security.identity.MessageDecryptionException;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.messaging.FirebaseMessaging;

import butterknife.BindView;
import butterknife.ButterKnife;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;



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

    @BindView(R.id.tvForgotPassword)
    TextView FrgtPass;

    @BindView(R.id.tvSignUp)
    TextView SignUp;

    @BindView(R.id.imgHelp)
    ImageButton Help;


    private FirebaseFirestore FStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FStore = FirebaseFirestore.getInstance();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        UserIDStatic.getInstance().setToken(token);
                        Query tokenExists = FStore.collection("Device_Token").whereEqualTo("token",token).limit(1);

                        tokenExists.get().addOnCompleteListener(task1 -> {
                            if(task1.isSuccessful())
                            {
                                QuerySnapshot tokenSnapshot = task1.getResult();

                                if(tokenSnapshot.isEmpty())
                                {

                                    String ID = UUID.randomUUID().toString();

                                    DocumentReference documentReference = FStore.collection("Device_Token").document(ID);
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("token",token);

                                    documentReference.set(data);
                                }

                            }
                        });
                    }

                });

        ButterKnife.bind(this);




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

            // Handle Admin Details
            if(UserNumber.equals("0000000013") && Password.equals("AdminUser@99"))
            {
                Toast.makeText(this, "Welcome, Admin", Toast.LENGTH_SHORT).show();
                openAdmin();
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
                        if(checkPassword(Password, userSnap.getString("password")))
                        {

                            if (userSnap.getBoolean("restrict"))
                            {
                                Toast.makeText(this, "Your account is restricted, please contact the Administrator.", Toast.LENGTH_LONG).show();
                                return;
                            }

                            UserIDStatic.getInstance().setUserId(UserNumber);

                            Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                            if(finalUserType == "Student")
                                openVacancyStud();
                            else
                                openVacancyLect();
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

        FrgtPass.setOnClickListener(view -> {
            String UserType = "";
            if (UserStudent.isChecked())
                UserType = "Student";
            else UserType = "Lecturer";
            final EditText emailField = new EditText(view.getContext());
            AlertDialog.Builder passResetDialog = new AlertDialog.Builder(view.getContext());
            passResetDialog.setTitle("Forgot Your Password?");
            passResetDialog.setMessage("Please provide your Email to receive the link to reset your password.");
            passResetDialog.setView(emailField);


            String finalUserType = UserType;
            passResetDialog.setPositiveButton("Send Password Reset Link", (dialogInterface, i) -> {
                String resEmail = emailField.getText().toString().trim();
                if(TextUtils.isEmpty(resEmail))
                {
                    Toast.makeText(this, "Please enter your Email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(resEmail).matches()) {
                    Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show();
                    return;
                }
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("");
                progressDialog.setMessage("Please wait...");

                if(finalUserType == "Student") {
                    Query queryStudent = FStore.collection("Student").whereEqualTo("email", resEmail).limit(1);
                    queryStudent.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            QuerySnapshot snap = task.getResult();
                            if (snap != null && !snap.isEmpty()) {
                                progressDialog.show();
                                DocumentSnapshot studSnap = snap.getDocuments().get(0);
                                String studNum = studSnap.getId();
                                String name = studSnap.getString("name");
                                String email = studSnap.getString("email");

                                String secretTok = UUID.randomUUID().toString().replace("-", "");
                                FStore.collection("Student").document(studNum).update("pass_token", secretTok)
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(this, "Sending Reset Link...", Toast.LENGTH_SHORT).show();
                                            new Email(this, email, "Vacancy Portal - Reset Password",
                                                    "Hello, " + name + ".\n\nTo reset your password please go to http://iotproj1.pythonanywhere.com/reset-pass/" + secretTok + "\n\nThank you.\nKind regards,\nVacancy Team.", "The reset link has been sent", "Unable to send link", progressDialog).execute();
                                            return;
                                        });
                            }else {

                                Toast.makeText(this, "Email does not exist", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }


                    });
                }

                if(finalUserType == "Lecturer") {
                    Query queryLect = FStore.collection("Lecturer").whereEqualTo("email", resEmail).limit(1);
                    queryLect.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            QuerySnapshot snap = task.getResult();
                            if (snap != null && !snap.isEmpty()) {
                                progressDialog.show();
                                DocumentSnapshot lecSnap = snap.getDocuments().get(0);
                                String staffNum = lecSnap.getId();
                                String name = lecSnap.getString("name");
                                String email = lecSnap.getString("email");

                                String secretTok = new SecureRandom().toString().substring(0, 16);
                                FStore.collection("Lecturer").document(staffNum).update("pass_token", secretTok)
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(this, "Sending Reset Link...", Toast.LENGTH_SHORT).show();
                                            new Email(this, email, "Vacancy Portal - Reset Password",
                                                    "Hello, " + name + ".\n\nTo reset your password please go to http://iotproj1.pythonanywhere.com/reset-pass/" + secretTok + "\n\nThank you.\nKind regards,\nVacancy Team.", "The reset link has been sent", "Unable to send link", progressDialog).execute();
                                            return;
                                        });
                            }else{
                                Toast.makeText(this, "Email does not exist", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                    });
                }



            });


            passResetDialog.setNegativeButton("Cancel", (dialogInterface, i) -> {

            });
            passResetDialog.create().show();
        });


        SignUp.setOnClickListener(view -> {
            if (UserStudent.isChecked())
                openSignUpStudent();
            else
                openSignUpLect();

        });


        Help.setOnClickListener(view -> openHelp());

    }



    public void openVacancyStud()
    {
        Intent intent = new Intent(this, VacancyBoardStudent.class);
        startActivity(intent);
    }

    public void openVacancyLect()
    {
        Intent intent = new Intent(this, VacancyBoardLecturer.class);
        startActivity(intent);
    }

    public void openHelp()
    {
        Intent intent = new Intent(this, Help.class);
        startActivity(intent);
    }

    public void openSignUpStudent()
    {
        Intent intent = new Intent(this, SignUpStudent.class);
        startActivity(intent);
    }

    public void openSignUpLect()
    {
        Intent intent = new Intent(this, SignUpLecturer.class);
        startActivity(intent);
    }

    public void openAdmin()
    {
        Intent intent = new Intent(this, AdminZone.class);
        startActivity(intent);
    }



    public static boolean checkPassword(String password, String hashedPassword)
    {
        String hashedPasswordToCheck = StaticStrings.hashPassword(password);
        return hashedPassword.equals(hashedPasswordToCheck);
    }


    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    finishAffinity();
                    System.exit(0);
                })
                .setNegativeButton("No",null)
                .show();
    }
}