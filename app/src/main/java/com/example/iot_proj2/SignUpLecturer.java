package com.example.iot_proj2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignUpLecturer extends AppCompatActivity {

    @BindView(R.id.btnSignUpLectBack)
    Button Back;

    @BindView(R.id.edtSignUpLectName)
    EditText Name;

    @BindView(R.id.edtSignUpLecPass)
    EditText Pass;

    @BindView(R.id.edtSignUpLecEmail)
    EditText LecEmail;

    @BindView(R.id.spnSignUpLecFac)
    Spinner Faculty;

    @BindView(R.id.spnSignUpLecDept)
    Spinner Department;

    @BindView(R.id.spnSignUpLectMod)
    Spinner Modules;

    @BindView(R.id.edtSignUpLecCPass)
    EditText ConfirmPass;

    @BindView(R.id.edtSignUpLecNumber)
    EditText Number;

    @BindView(R.id.btnSignUpLecSubmit)
    Button Submit;

    private FirebaseFirestore FStore;
    private CustomSpinnerAdapter tempModAdap = null;

    private final HashSet<String> selectedItems = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_lecturer);

        ButterKnife.bind(this);

        FStore = FirebaseFirestore.getInstance();

        Back.setOnClickListener(view -> {
            Intent intent = new Intent(SignUpLecturer.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        ArrayAdapter<String> FacAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, StaticStrings.FacultyString);
        Faculty.setAdapter(FacAdapter);
        Faculty.setPrompt("Faculty");

        ArrayAdapter<String> DeptAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{});
        Department.setAdapter(DeptAdapter);
        Department.setPrompt("Department");

        CustomSpinnerAdapter ModAdapter = new CustomSpinnerAdapter(this, Arrays.asList(new String[]{}));
        Modules.setAdapter(ModAdapter);
        Modules.setPrompt("Modules");


        Faculty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Update department spinner data based on selected faculty
                String selectedFaculty = StaticStrings.FacultyString[position];
                String[] departmentsForSelectedFaculty = StaticStrings.DepartmentString[position];
                ArrayAdapter<String> updatedDepartmentAdapter = new ArrayAdapter<>(SignUpLecturer.this, android.R.layout.simple_spinner_item, departmentsForSelectedFaculty);
                Department.setAdapter(updatedDepartmentAdapter);
                // Clear module spinner data
                ModAdapter.clear();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        Department.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Update module spinner data based on selected department
                String selectedFaculty = StaticStrings.FacultyString[Faculty.getSelectedItemPosition()];
                String selectedDepartment = StaticStrings.DepartmentString[Faculty.getSelectedItemPosition()][position];
                String[] modulesForSelectedDepartment = StaticStrings.ModuleString[Faculty.getSelectedItemPosition()][position];
                CustomSpinnerAdapter updatedModuleAdapter = new CustomSpinnerAdapter(SignUpLecturer.this, Arrays.asList(modulesForSelectedDepartment));
                tempModAdap = updatedModuleAdapter;
                Modules.setAdapter(updatedModuleAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        Submit.setOnClickListener(view -> {
            String name = Name.getText().toString().trim();
            String number = Number.getText().toString().trim();
            String password = Pass.getText().toString().trim();
            String confPass = ConfirmPass.getText().toString().trim();
            String email = LecEmail.getText().toString().trim();
            String faculty = Faculty.getSelectedItem().toString();
            String department = Department.getSelectedItem().toString();
            List<String> selectedModules = tempModAdap.getSelectedItems();

            if (TextUtils.isEmpty(name)) {
                Name.setError("Please enter your Name");
                return;
            }

            if (TextUtils.isEmpty(number)) {
                Number.setError("Please enter your Staff Number");
                return;
            }

            if (number.length() < 8 || number.length() > 10) {
                Number.setError("Your Staff Number must be 8 to 10 digits");
                return;
            }


            if (TextUtils.isEmpty(email)) {
                LecEmail.setError("Please enter your Email");
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                LecEmail.setError("Invalid Email");
                return;
            }

            if (password.length() < 6 || password.length() > 20) {
                Pass.setError("Your Password must be 6 to 20 characters long");
                return;
            }

            if (!password.equals(confPass)) {
                ConfirmPass.setError("The Password and Confirm Password do not match");
                return;
            }

            if (selectedModules == null || selectedModules.isEmpty()) {
                Toast.makeText(this, "Please select a Module", Toast.LENGTH_SHORT).show();
                return;
            }

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("");
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // Checks
            DocumentReference docLec = FStore.collection("Lecturer").document(number);
            Query query = FStore.collection("Lecturer").whereEqualTo("email", email);

            Task<DocumentSnapshot> docLecTask = docLec.get();
            Task<QuerySnapshot> queryTask = query.get();

            Tasks.whenAllSuccess(docLecTask, queryTask).addOnSuccessListener(results -> {
                DocumentSnapshot docLecSnapshot = docLecTask.getResult();
                QuerySnapshot querySnapshot = queryTask.getResult();

                if (docLecSnapshot.exists()) {
                    Number.setError("Staff Number already exists");
                    progressDialog.dismiss();
                    return;
                }

                if (!querySnapshot.isEmpty()) {
                    LecEmail.setError("Email already exists");
                    progressDialog.dismiss();
                    return;
                }

                String sepModules = TextUtils.join(",", selectedModules);

                DocumentReference lecturerDoc = FStore.collection("Lecturer").document(number);

                String hashPassword = StaticStrings.hashPassword(password);
                Map<String, Object> data = new HashMap<>();
                data.put("email", email);
                data.put("faculty", faculty);
                data.put("department", department);
                data.put("name", name);
                data.put("module", sepModules);
                data.put("password", hashPassword);
                data.put("pass_token", "");
                data.put("restrict", false);

                lecturerDoc.set(data).addOnSuccessListener(unused -> {
                            new Email(this, email, "Welcome to DUT Vacancy Portal", "Welcome to DUT Vacancy Portal, " + name + ".\n\nYour account has been successfully created with us.\n\nThank you.\nKind regards,\nVacancy Team.", "Your account has been created", "Account Created, but error in sending email", progressDialog).execute();
                            progressDialog.setOnDismissListener(dialogInterface -> {
                                Intent intent = new Intent(SignUpLecturer.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            });
                        })
                        .addOnFailureListener(e -> new Thread(() -> {
                            progressDialog.dismiss();
                            runOnUiThread(() -> Toast.makeText(this, "Error: Unable to create your account", Toast.LENGTH_SHORT).show());
                            return;
                        }));
            });


        });

    }

    @Override
    public void onBackPressed() {
        return;
    }
}