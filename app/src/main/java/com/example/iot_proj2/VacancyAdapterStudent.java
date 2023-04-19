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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VacancyAdapterStudent extends RecyclerView.Adapter<VacancyAdapterStudent.VacancyViewHolderStudent>{
    private List<Vacancy> vacancyList;
    private Context context;

    // Constructor
    public VacancyAdapterStudent(List<Vacancy> vacancyList, Context con) {
        this.vacancyList = vacancyList;
        this.context = con;
    }

    // ViewHolder class
    public static class VacancyViewHolderStudent extends RecyclerView.ViewHolder {
        TextView vacancyLecturerTextView;
        TextView vacancyTitleTextView;
        TextView vacancyDescriptionTextView;
        TextView vacancyPositionTextView;

        TextView vacancySalaryTextView;

        TextView vacancySemesterTextView;

        ImageView vacancyImage;
        View vacancySeperate;
        TextView vacancyEmpty;
        ImageView applyButton;

        public VacancyViewHolderStudent(View itemView) {
            super(itemView);
            vacancyTitleTextView = itemView.findViewById(R.id.vacancyTitleTextViewStud);
            vacancyPositionTextView = itemView.findViewById(R.id.vacancyPositionTextViewStud);
            vacancyDescriptionTextView = itemView.findViewById(R.id.vacancyDescriptionTextViewStud);
            vacancyLecturerTextView = itemView.findViewById(R.id.vacancyLecturerTextViewStud);
            vacancyEmpty = itemView.findViewById(R.id.tvEmptyVacStud);
            vacancySeperate = itemView.findViewById(R.id.viewStud);
            vacancyImage = itemView.findViewById(R.id.imageViewLecturerImg);
            applyButton = itemView.findViewById(R.id.applyButton);
            vacancySalaryTextView = itemView.findViewById(R.id.vacancySalaryTextViewStud);
            vacancySemesterTextView = itemView.findViewById(R.id.vacancySemesterTextViewStud);
        }
    }

    @NonNull
    @Override
    public VacancyViewHolderStudent onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vacancy_item_layout_student, parent, false);
        return new VacancyViewHolderStudent(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VacancyViewHolderStudent holder, int position) {
        if (vacancyList.isEmpty()) {
            holder.vacancyLecturerTextView.setVisibility(View.GONE);
            holder.vacancyTitleTextView.setVisibility(View.GONE);
            holder.vacancyDescriptionTextView.setVisibility(View.GONE);
            holder.vacancyPositionTextView.setVisibility(View.GONE);
            holder.vacancyImage.setVisibility(View.GONE);
            holder.vacancySeperate.setVisibility(View.GONE);
            holder.applyButton.setVisibility(View.GONE);
            holder.vacancySalaryTextView.setVisibility(View.GONE);
            holder.vacancySemesterTextView.setVisibility(View.GONE);
            holder.vacancyEmpty.setVisibility(View.VISIBLE);
            holder.vacancyEmpty.setText("No Vacancies");

        } else {
            holder.vacancyEmpty.setVisibility(View.GONE);
            holder.vacancyLecturerTextView.setVisibility(View.VISIBLE);
            holder.vacancyTitleTextView.setVisibility(View.VISIBLE);
            holder.vacancyDescriptionTextView.setVisibility(View.VISIBLE);
            holder.vacancyPositionTextView.setVisibility(View.VISIBLE);
            holder.vacancyImage.setVisibility(View.VISIBLE);
            holder.vacancySeperate.setVisibility(View.VISIBLE);
            holder.applyButton.setVisibility(View.VISIBLE);
            holder.vacancySalaryTextView.setVisibility(View.VISIBLE);
            holder.vacancySemesterTextView.setVisibility(View.VISIBLE);

            Vacancy vacancy = vacancyList.get(position);

            FirebaseFirestore FStore;
            FStore = FirebaseFirestore.getInstance();
            // Set the vacancy title
            holder.vacancyTitleTextView.setText(vacancy.getModule());

            holder.vacancyLecturerTextView.setText(vacancy.getLecturer());

            // Set the vacancy position
            holder.vacancyPositionTextView.setText(vacancy.getType());

            holder.vacancyDescriptionTextView.setText(vacancy.getDescription());

            holder.vacancySalaryTextView.setText(vacancy.getSalary()+" per/hour");
            holder.vacancySemesterTextView.setText("Semester " +vacancy.getSemester());


            // Set click listener for the withdraw button
            holder.applyButton.setOnClickListener(v -> {
                // Update the vacancy status to "Vacancy Withdrawn"
                new AlertDialog.Builder(context)
                        .setMessage("Are you sure you want to apply for this Vacancy?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialogInterface, i) -> {

                            Query getLatestId = FStore.collection("Application").orderBy("docId", Query.Direction.DESCENDING).limit(1);

                            Task<QuerySnapshot> task = getLatestId.get();

                            Tasks.whenAllSuccess(task).addOnSuccessListener(objects -> {
                                QuerySnapshot taskResult = task.getResult();
                                int maxid = 1;
                                if (!taskResult.isEmpty() && taskResult != null) {
                                    DocumentSnapshot documentSnapshot = taskResult.getDocuments().get(0);
                                    long tempId = documentSnapshot.getLong("docId");
                                    maxid = (int) tempId + 1;
                                }

                                String DocId = Integer.toString(maxid);

                                DocumentReference newApp = FStore.collection("Application").document(DocId);
                                Map<String, Object> data = new HashMap<>();
                                data.put("status", "pending");
                                data.put("student_num", UserIDStatic.getInstance().getUserId());
                                data.put("vacancy_id", Long.toString(vacancy.getDocId()));
                                data.put("docId", maxid);

                                newApp.set(data).addOnSuccessListener(unused -> {
                                            vacancyList.remove(position);
                                            Toast.makeText(context, "Applied for Vacancy", Toast.LENGTH_SHORT).show();
                                            notifyItemRemoved(position);
                                        })
                                        .addOnFailureListener(e -> {
                                        });

                            });
                        })
                        .setNegativeButton("No", null)
                        .show();

            });
        }
    }


    @Override
    public int getItemCount() {
        if(vacancyList.isEmpty())
        {
            return 1;
        }else {
            return vacancyList.size();
        }
    }
}
