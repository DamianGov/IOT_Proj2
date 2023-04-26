package com.example.iot_proj2;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignUpStudent extends AppCompatActivity {

    @BindView(R.id.btnSignUpStudBack)
    Button Back;

    @BindView(R.id.edtSignUpStudName)
    EditText Name;

    @BindView(R.id.edtSignUpStudNumber)
    EditText Number;

    @BindView(R.id.spnSignUpStudFac)
    Spinner Faculty;

    @BindView(R.id.edtSignUpStudCourse)
    EditText Course;

    @BindView(R.id.edtSignUpStudID)
    EditText ID;

    @BindView(R.id.edtSignUpStudEmail)
    EditText StudEmail;

    @BindView(R.id.edtSignUpStudPass)
    EditText Pass;

    @BindView(R.id.edtSignUpStudCPass)
    EditText ConfirmPass;

    @BindView(R.id.edtSignUpStudResume)
    EditText Resume;

    @BindView(R.id.btnSignUpStudSubmit)
    Button Submit;


    private Uri fileUri = null;
    private FirebaseFirestore FStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_student);

        ButterKnife.bind(this);

        FStore = FirebaseFirestore.getInstance();

        Back.setOnClickListener(view -> {
            Intent intent = new Intent(SignUpStudent.this, MainActivity.class);
            startActivity(intent);
            finish();
        });


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, StaticStrings.FacultyString);

        Faculty.setAdapter(adapter);
        Faculty.setPrompt("Faculty");

        Resume.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            startActivityForResult(Intent.createChooser(intent, "Select Resume (PDF)"), 1);
        });

        Submit.setOnClickListener(view ->
        {
            String name = Name.getText().toString().trim();
            String number = Number.getText().toString().trim();
            String faculty = Faculty.getSelectedItem().toString();
            String course = Course.getText().toString();
            String id = ID.getText().toString();
            String email = StudEmail.getText().toString();
            String pass = Pass.getText().toString();
            String confPass = ConfirmPass.getText().toString();

            if (TextUtils.isEmpty(name)) {
                Name.setError("Please enter your Name");
                return;
            }

            if (TextUtils.isEmpty(number)) {
                Number.setError("Please enter your Student Number");
                return;
            }

            if (number.length() != 8) {
                Number.setError("Your Student Number must have 8 digits");
                return;
            }

            if (TextUtils.isEmpty(course)) {
                Course.setError("Please enter your Course");
                return;
            }

            if (TextUtils.isEmpty(id)) {
                ID.setError("Please enter your ID");
                return;
            }

            if (id.length() != 13) {
                ID.setError("Your ID Number must have 13 digits");
                return;
            }

            if (TextUtils.isEmpty(email)) {
                StudEmail.setError("Please enter your Email");
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                StudEmail.setError("Invalid Email");
                return;
            }

            if (pass.length() < 6 || pass.length() > 20) {
                Pass.setError("Your Password must be 6 to 20 characters long");
                return;
            }

            if (!pass.equals(confPass)) {
                ConfirmPass.setError("The Password and Confirm Password do not match");
                return;
            }

            if (fileUri == null) {
                Resume.setError("Please choose your Resume file");
                return;
            } else
                Resume.setError(null);

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("");
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // Checks
            DocumentReference docStud = FStore.collection("Student").document(number);
            Query query = FStore.collection("Student").whereEqualTo("email", email);

            Task<DocumentSnapshot> docStudTask = docStud.get();
            Task<QuerySnapshot> queryTask = query.get();

            Tasks.whenAllSuccess(docStudTask, queryTask).addOnSuccessListener(results -> {
                DocumentSnapshot docStudSnapshot = docStudTask.getResult();
                QuerySnapshot querySnapshot = queryTask.getResult();

                if (docStudSnapshot.exists()) {
                    Number.setError("Student Number already exists");
                    progressDialog.dismiss();
                    return;
                }

                if (!querySnapshot.isEmpty()) {
                    StudEmail.setError("Email already exists");
                    progressDialog.dismiss();
                    return;
                }

                DocumentReference studentDoc = FStore.collection("Student").document(number);

                String hashPassword = StaticStrings.hashPassword(pass);
                Map<String, Object> data = new HashMap<>();
                data.put("ID", id);
                data.put("course", course);
                data.put("email", email);
                data.put("faculty", faculty);
                data.put("name", name);
                data.put("password", hashPassword);
                data.put("pass_token", "");
                data.put("restrict", false);

                UploadFileToDrbx(fileUri, number);
                progressDialog.setMessage("Uploading your Resume");
                studentDoc.set(data).addOnSuccessListener(documentReference -> {
                            new Email(this, email, "Welcome to DUT Vacancy Portal", "Welcome to DUT Vacancy Portal, " + name + ".\n\nYour account has been successfully created with us.\n\nThank you.\nKind regards,\nVacancy Team.", "Your account has been created", "Account Created, but error in sending email", progressDialog).execute();
                            progressDialog.setOnDismissListener(dialogInterface -> {
                                Intent intent = new Intent(SignUpStudent.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            });
                        })
                        .addOnFailureListener(e -> {
                            new Thread(() -> {
                                progressDialog.dismiss();
                                runOnUiThread(() -> Toast.makeText(this, "Error: Unable to create your account", Toast.LENGTH_SHORT).show());
                                return;
                            });
                        });

            });

        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            fileUri = data.getData();
            Resume.setText(getFileNameFromUri(fileUri));
        }
    }

    public void UploadFileToDrbx(Uri fileUri, String studNum) {
        DropboxInit dropboxInit = new DropboxInit();
        new Thread(() -> {
            try (InputStream inputStream = getContentResolver().openInputStream(fileUri)) {

                String fileName = studNum + ".pdf";

                FileMetadata metadata = dropboxInit.client.files().uploadBuilder("/" + studNum + "/" + fileName)
                        .withMode(WriteMode.ADD)
                        .uploadAndFinish(inputStream);

            } catch (Exception e) {

            }
        }).start();
    }

    @SuppressLint("Range")
    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        String scheme = uri.getScheme();
        if (scheme.equals("file")) {
            fileName = uri.getLastPathSegment();
        } else if (scheme.equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                cursor.close();
            }
        }
        return fileName;
    }

    @Override
    public void onBackPressed() {
        return;
    }
}