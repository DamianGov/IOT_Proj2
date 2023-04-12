package com.example.iot_proj2;

import android.content.Context;
import android.icu.lang.UCharacter;
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

import com.dropbox.core.util.StringUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;

public class ApplicationAdapterStudent extends RecyclerView.Adapter<ApplicationAdapterStudent.ApplicationViewHolder>{

    private List<Application> applicationList;

    private Context context;


    public ApplicationAdapterStudent(List<Application> applicationList, Context context) {
        this.applicationList = applicationList;
        this.context = context;
    }

    public static class ApplicationViewHolder extends RecyclerView.ViewHolder {

        TextView applicationTitle, applicationPosition, applicationDescription, applicationStatus, applicationLecturer, tvEmpty;
        ImageView withdrawButton;
        View viewSeparator;

        public ApplicationViewHolder(View itemView) {
            super(itemView);
            applicationTitle = itemView.findViewById(R.id.applicationTitleTextViewStudent);
            applicationPosition = itemView.findViewById(R.id.applicationPositionTextViewStudent);
            applicationDescription = itemView.findViewById(R.id.applicationDescriptionTextViewStudent);
            applicationStatus = itemView.findViewById(R.id.applicationStatusTextViewStudent);
            applicationLecturer = itemView.findViewById(R.id.applicationLecturerNameTextViewStudent);
            withdrawButton = itemView.findViewById(R.id.withdrawAppButton);
            viewSeparator = itemView.findViewById(R.id.viewAppStud);
            tvEmpty = itemView.findViewById(R.id.tvEmptyAppStud);
        }

    }

    @NonNull
    @Override
    public ApplicationAdapterStudent.ApplicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.application_item_layout_student, parent, false);
        return new ApplicationAdapterStudent.ApplicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApplicationAdapterStudent.ApplicationViewHolder holder, int position) {
        if(applicationList.isEmpty())
       {
           holder.applicationTitle.setVisibility(View.GONE);
            holder.applicationPosition.setVisibility(View.GONE);
           holder.applicationDescription.setVisibility(View.GONE);
           holder.withdrawButton.setVisibility(View.GONE);
            holder.viewSeparator.setVisibility(View.GONE);
            holder.applicationStatus.setVisibility(View.GONE);
            holder.applicationLecturer.setVisibility(View.GONE);
           holder.tvEmpty.setVisibility(View.VISIBLE);
            holder.tvEmpty.setText("No Applications");
        } else {
            holder.applicationTitle.setVisibility(View.VISIBLE);
            holder.applicationPosition.setVisibility(View.VISIBLE);
            holder.applicationDescription.setVisibility(View.VISIBLE);
            holder.withdrawButton.setVisibility(View.VISIBLE);
            holder.viewSeparator.setVisibility(View.VISIBLE);
            holder.applicationStatus.setVisibility(View.VISIBLE);
            holder.applicationLecturer.setVisibility(View.VISIBLE);
            holder.tvEmpty.setVisibility(View.GONE);

            Application application = applicationList.get(position);



            holder.applicationTitle.setText(application.getModule());
            holder.applicationPosition.setText(application.getType());
            holder.applicationDescription.setText(application.getDescription());
            holder.applicationLecturer.setText(application.getPersonName());

            String status = application.getStatus();
            holder.applicationStatus.setText(UCharacter.toTitleCase(Locale.UK,status,null,0));
            switch (status)
            {
                case "pending": holder.applicationStatus.setTextColor(ContextCompat.getColor(context,R.color.Pending));
                break;
                case "vacancy withdrawn" :
                case "withdrawn" :
                     holder.applicationStatus.setTextColor(ContextCompat.getColor(context,R.color.Withdrawn));
                break;
                case "accepted": holder.applicationStatus.setTextColor(ContextCompat.getColor(context,R.color.Accepted));
                break;
                case "declined" : holder.applicationStatus.setTextColor(ContextCompat.getColor(context,R.color.Declined));
                break;
            }

            if("pending".equals(application.getStatus()))
            {
                   holder.withdrawButton.setVisibility(View.VISIBLE);
            }
            else{
                   holder.withdrawButton.setVisibility(View.GONE);
            }

                    holder.withdrawButton.setOnClickListener(view -> {
                        new AlertDialog.Builder(context)
                                .setMessage("Are you sure you want to withdraw this application?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", (dialogInterface, i) -> {
                                    FirebaseFirestore FStore;
                                    FStore = FirebaseFirestore.getInstance();
                                    FStore.collection("Application").document(Long.toString(application.getDocId())).update("status", "withdrawn")
                                            .addOnSuccessListener(unused -> {
                                                application.setStatus("withdrawn");
                                                Toast.makeText(context, "Application Withdrawn", Toast.LENGTH_SHORT).show();
                                                notifyDataSetChanged();
                                            }).addOnFailureListener(e -> {
                                            });


                                })
                                .setNegativeButton("No",null)
                                .show();
                    });
                }


    }

    @Override
    public int getItemCount() {
        if(applicationList.isEmpty())
        {
            return 1;
        }else {
            return applicationList.size();
        }
    }

}
