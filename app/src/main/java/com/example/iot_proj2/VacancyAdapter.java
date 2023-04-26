package com.example.iot_proj2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.List;

public class VacancyAdapter extends RecyclerView.Adapter<VacancyAdapter.VacancyViewHolder> {
    private final List<Vacancy> vacancyList;
    private final Context context;

    // Constructor
    public VacancyAdapter(List<Vacancy> vacancyList, Context con) {
        this.vacancyList = vacancyList;
        this.context = con;
    }

    // ViewHolder class
    public static class VacancyViewHolder extends RecyclerView.ViewHolder {
        TextView vacancyTitleTextView;
        TextView vacancyPositionTextView;
        TextView vacancyDescriptionTextView;
        TextView vacancyStatusTextView;

        TextView vacancySemesterTextView;
        TextView vacancySalaryTextView;

        View vacancyView;
        TextView vacancyEmpty;
        ImageView withdrawButton;

        public VacancyViewHolder(View itemView) {
            super(itemView);
            vacancyTitleTextView = itemView.findViewById(R.id.vacancyTitleTextView);
            vacancyPositionTextView = itemView.findViewById(R.id.vacancyPositionTextView);
            vacancyDescriptionTextView = itemView.findViewById(R.id.vacancyDescriptionTextView);
            vacancyStatusTextView = itemView.findViewById(R.id.vacancyStatusTextView);
            vacancyView = itemView.findViewById(R.id.viewLec);
            vacancyEmpty = itemView.findViewById(R.id.tvEmptyVacLec);
            withdrawButton = itemView.findViewById(R.id.withdrawButton);
            vacancySemesterTextView = itemView.findViewById(R.id.vacancySemesterTextView);
            vacancySalaryTextView = itemView.findViewById(R.id.vacancySalaryTextView);
        }
    }

    @NonNull
    @Override
    public VacancyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vacancy_item_layout, parent, false);
        return new VacancyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VacancyViewHolder holder, int position) {
        if (vacancyList.isEmpty()) {
            holder.vacancyTitleTextView.setVisibility(View.GONE);
            holder.vacancyPositionTextView.setVisibility(View.GONE);
            holder.vacancyDescriptionTextView.setVisibility(View.GONE);
            holder.withdrawButton.setVisibility(View.GONE);
            holder.vacancyView.setVisibility(View.GONE);
            holder.vacancyStatusTextView.setVisibility(View.GONE);
            holder.vacancySemesterTextView.setVisibility(View.GONE);
            holder.vacancySalaryTextView.setVisibility(View.GONE);
            holder.vacancyEmpty.setVisibility(View.VISIBLE);
            holder.vacancyEmpty.setText("No Vacancies");

        } else {
            holder.vacancyTitleTextView.setVisibility(View.VISIBLE);
            holder.vacancyPositionTextView.setVisibility(View.VISIBLE);
            holder.vacancyDescriptionTextView.setVisibility(View.VISIBLE);
            holder.withdrawButton.setVisibility(View.VISIBLE);
            holder.vacancyView.setVisibility(View.VISIBLE);
            holder.vacancyStatusTextView.setVisibility(View.VISIBLE);
            holder.vacancySemesterTextView.setVisibility(View.VISIBLE);
            holder.vacancySalaryTextView.setVisibility(View.VISIBLE);
            holder.vacancyEmpty.setVisibility(View.GONE);
            Vacancy vacancy = vacancyList.get(position);

            // Set the vacancy title
            holder.vacancyTitleTextView.setText(vacancy.getModule());

            String statusText;
            switch (vacancy.getStatus()) {
                case "0": {
                    statusText = "Complete";
                    holder.vacancyStatusTextView.setTextColor(ContextCompat.getColor(context, R.color.Complete));
                }
                break;
                case "1": {
                    statusText = "Available";
                    holder.vacancyStatusTextView.setTextColor(ContextCompat.getColor(context, R.color.Available));
                }
                break;
                case "2": {
                    statusText = "Vacancy Withdrawn";
                    holder.vacancyStatusTextView.setTextColor(ContextCompat.getColor(context, R.color.Withdrawn));
                }
                break;
                default:
                    statusText = "Unknown";
                    break;
            }
            holder.vacancyStatusTextView.setText(statusText);

            // Set the vacancy position
            holder.vacancyPositionTextView.setText(vacancy.getType());

            holder.vacancyDescriptionTextView.setText(vacancy.getDescription());

            holder.vacancySemesterTextView.setText("Semester " + vacancy.getSemester());

            holder.vacancySalaryTextView.setText(vacancy.getSalary() + " per/hour");

            if ("1".equals(vacancy.getStatus())) { // Show the withdraw button only if the status is "Available"
                holder.withdrawButton.setVisibility(View.VISIBLE);
            } else {
                holder.withdrawButton.setVisibility(View.GONE);
            }

            FirebaseFirestore FStore;
            FStore = FirebaseFirestore.getInstance();
            // Set click listener for the withdraw button
            holder.withdrawButton.setOnClickListener(v -> {
                // Update the vacancy status to "Vacancy Withdrawn"

                new AlertDialog.Builder(context)
                        .setMessage("Are you sure you want to withdraw this vacancy?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            FStore.collection("Vacancy").document(Long.toString(vacancy.getDocId())).update("status", "2")
                                    .addOnSuccessListener(aVoid -> {
                                        // Update the vacancy status in the local list
                                        vacancy.setStatus("2");
                                        Toast.makeText(context, "Vacancy Withdrawn", Toast.LENGTH_SHORT).show();
                                        notifyDataSetChanged();

                                    })
                                    .addOnFailureListener(e -> {
                                    });
                            Query getApp = FStore.collection("Application").whereEqualTo("vacancy_id", vacancy.getDocId()).whereNotIn("status", Arrays.asList("declined", "withdrawn"));
                            getApp.get().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if (querySnapshot != null) {
                                        List<DocumentSnapshot> documentSnapshots = querySnapshot.getDocuments();
                                        for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                                            documentSnapshot.getReference().update("status", "vacancy withdrawn");
                                        }
                                    }
                                }
                            });
                        })
                        .setNegativeButton("No", null)
                        .show();
            });
        }
    }

    @Override
    public int getItemCount() {
        if (vacancyList.isEmpty()) {
            return 1;
        } else {
            return vacancyList.size();
        }
    }
}
