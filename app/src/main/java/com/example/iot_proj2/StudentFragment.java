package com.example.iot_proj2;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class StudentFragment extends Fragment {

    private List<String> StudentName, StudentNum, StudentConcat;
    private List<Boolean> StudentRestrict;

    private ArrayAdapter<String> adapter;

    private Spinner StudentSpn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student, container, false);

        FirebaseFirestore FStore = FirebaseFirestore.getInstance();

        CollectionReference getAllStudent = FStore.collection("Student");

        StudentSpn = view.findViewById(R.id.spnAdminStudent);

        TextView StudentStatus = view.findViewById(R.id.tvRestrictionStatusStudent);

        Button RestrictBtn = view.findViewById(R.id.btnAdminStudentRestrict);

        getAllStudent.get().addOnCompleteListener(task -> {
            StudentName = new ArrayList<>();
            StudentNum = new ArrayList<>();
            StudentRestrict = new ArrayList<>();
            StudentConcat = new ArrayList<>();

            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                    StudentName.add(queryDocumentSnapshot.getString("name"));
                    StudentNum.add(queryDocumentSnapshot.getId());
                    StudentRestrict.add(queryDocumentSnapshot.getBoolean("restrict"));
                    StudentConcat.add(queryDocumentSnapshot.getString("name") + " (" + queryDocumentSnapshot.getId() + ")");
                }

                adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, StudentConcat);
                StudentSpn.setAdapter(adapter);


            }


        });

        StudentSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setCornerRadius(12);
                if (StudentRestrict.get(i)) {
                    gradientDrawable.setColor(ContextCompat.getColor(requireContext(), R.color.Approved));
                    StudentStatus.setText(StudentName.get(i) + " is Restricted");
                    StudentStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.Cancelled));
                    RestrictBtn.setText("Unrestrict");
                } else {
                    gradientDrawable.setColor(ContextCompat.getColor(requireContext(), R.color.Cancelled));
                    StudentStatus.setText(StudentName.get(i) + " is not restricted");
                    StudentStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.Approved));
                    RestrictBtn.setText("Restrict");
                }
                RestrictBtn.setBackgroundDrawable(gradientDrawable);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        RestrictBtn.setOnClickListener(view1 -> {
            int Position = StudentSpn.getSelectedItemPosition();

            if (StudentRestrict.get(Position)) {
                new AlertDialog.Builder(requireContext())
                        .setMessage("Are you sure you want to unrestrict " + StudentName.get(Position) + "?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            FStore.collection("Student").document(StudentNum.get(Position)).update("restrict", false).
                                    addOnSuccessListener(unused -> {
                                        Toast.makeText(requireContext(), StudentName.get(Position) + " has been unrestricted", Toast.LENGTH_SHORT).show();
                                        StudentRestrict.set(Position, false);
                                        adapter.notifyDataSetChanged();
                                        StudentSpn.setAdapter(adapter);
                                        StudentSpn.setSelection(Position);
                                    });
                        }).setNegativeButton("No", null)
                        .show();
            } else {
                new AlertDialog.Builder(requireContext())
                        .setMessage("Are you sure you want to restrict " + StudentName.get(Position) + "?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            FStore.collection("Student").document(StudentNum.get(Position)).update("restrict", true).
                                    addOnSuccessListener(unused -> {
                                        Toast.makeText(requireContext(), StudentName.get(Position) + " has been restricted", Toast.LENGTH_SHORT).show();
                                        StudentRestrict.set(Position, true);
                                        adapter.notifyDataSetChanged();
                                        StudentSpn.setAdapter(adapter);
                                        StudentSpn.setSelection(Position);
                                    });
                        }).setNegativeButton("No", null)
                        .show();
            }

        });

        return view;
    }
}