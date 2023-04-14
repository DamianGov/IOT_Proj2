package com.example.iot_proj2;

import android.content.Context;
import android.graphics.PostProcessor;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class LecturerFragment extends Fragment {


    private List<String> LecturerName, LecturerNum, LecturerConcat;
    private List<Boolean> LecturerRestrict;

    private ArrayAdapter<String> adapter;

    private Spinner LecturerSpn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_lecturer,container,false);

        FirebaseFirestore FStore = FirebaseFirestore.getInstance();

        CollectionReference getAllLecturer = FStore.collection("Lecturer");

         LecturerSpn = view.findViewById(R.id.spnAdminLecturer);

        TextView LecturerStatus = view.findViewById(R.id.tvRestrictionStatusLecturer);

        Button RestrictBtn = view.findViewById(R.id.btnAdminLecturerRestrict);

        getAllLecturer.get().addOnCompleteListener(task -> {
            LecturerName = new ArrayList<>();
            LecturerNum = new ArrayList<>();
            LecturerRestrict = new ArrayList<>();
            LecturerConcat = new ArrayList<>();

            if(task.isSuccessful())
            {
                for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult())
                {
                    LecturerName.add(queryDocumentSnapshot.getString("name"));
                    LecturerNum.add(queryDocumentSnapshot.getId());
                    LecturerRestrict.add(queryDocumentSnapshot.getBoolean("restrict"));
                    LecturerConcat.add(queryDocumentSnapshot.getString("name")+" ("+queryDocumentSnapshot.getId()+")");
                }

                adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item,LecturerConcat);
                LecturerSpn.setAdapter(adapter);


            }


        });

        LecturerSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setCornerRadius(12);
                if(LecturerRestrict.get(i)){
                    gradientDrawable.setColor(ContextCompat.getColor(requireContext(),R.color.Approved));
                    LecturerStatus.setText(LecturerName.get(i) + " is Restricted");
                    LecturerStatus.setTextColor(ContextCompat.getColor(requireContext(),R.color.Cancelled));
                    RestrictBtn.setText("Unrestrict");
                }
                else {
                    gradientDrawable.setColor(ContextCompat.getColor(requireContext(),R.color.Cancelled));
                    LecturerStatus.setText(LecturerName.get(i) + " is not restricted");
                    LecturerStatus.setTextColor(ContextCompat.getColor(requireContext(),R.color.Approved));
                    RestrictBtn.setText("Restrict");
                }
                RestrictBtn.setBackgroundDrawable(gradientDrawable);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        RestrictBtn.setOnClickListener(view1 -> {
            int Position = LecturerSpn.getSelectedItemPosition();

            if(LecturerRestrict.get(Position))
            {
                new AlertDialog.Builder(requireContext())
                        .setMessage("Are you sure you want to unrestrict "+LecturerName.get(Position)+"?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                                FStore.collection("Lecturer").document(LecturerNum.get(Position)).update("restrict",false).
                                        addOnSuccessListener(unused -> {
                                            Toast.makeText(requireContext(), LecturerName.get(Position)+" has been unrestricted", Toast.LENGTH_SHORT).show();
                                            LecturerRestrict.set(Position, false);
                                            adapter.notifyDataSetChanged();
                                            LecturerSpn.setAdapter(adapter);
                                            LecturerSpn.setSelection(Position);
                                        });
                        }).setNegativeButton("No",null)
                        .show();
            }
            else {
                new AlertDialog.Builder(requireContext())
                        .setMessage("Are you sure you want to restrict "+LecturerName.get(Position)+"?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            FStore.collection("Lecturer").document(LecturerNum.get(Position)).update("restrict",true).
                                    addOnSuccessListener(unused -> {
                                        Toast.makeText(requireContext(), LecturerName.get(Position)+" has been restricted", Toast.LENGTH_SHORT).show();
                                        LecturerRestrict.set(Position, true);
                                        adapter.notifyDataSetChanged();
                                        LecturerSpn.setAdapter(adapter);
                                        LecturerSpn.setSelection(Position);
                                    });
                        }).setNegativeButton("No",null)
                        .show();
            }

        });

        return view;
    }
}