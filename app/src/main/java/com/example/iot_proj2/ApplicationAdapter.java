package com.example.iot_proj2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DownloadBuilder;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ApplicationViewHolder> {
    private final List<Application> applicationList;

    private final Context context;

    private final Handler handler = new Handler(Looper.getMainLooper());

    public ApplicationAdapter(List<Application> applicationList, Context context) {
        this.applicationList = applicationList;
        this.context = context;
    }

    public static class ApplicationViewHolder extends RecyclerView.ViewHolder {

        TextView applicationTitle, applicationStudentName, applicationStudentNum, applicationDescrip, applicationType, tvEmpty, applicationSemester, applicationSalary;

        View seperator;

        ImageView downloadResume, acceptApp, declineApp;


        public ApplicationViewHolder(View itemView) {
            super(itemView);
            applicationTitle = itemView.findViewById(R.id.applicationTitleTextViewLecturer);
            applicationStudentName = itemView.findViewById(R.id.applicationStudentNameTextViewLecturer);
            applicationStudentNum = itemView.findViewById(R.id.applicationNumberTextViewLecturer);
            applicationDescrip = itemView.findViewById(R.id.applicationDescriptionTextViewLecturer);
            applicationType = itemView.findViewById(R.id.applicationTypeTextViewLecturer);
            tvEmpty = itemView.findViewById(R.id.tvEmptyAppLec);
            downloadResume = itemView.findViewById(R.id.downloadResumeAppButtonLec);
            acceptApp = itemView.findViewById(R.id.acceptAppButtonLec);
            declineApp = itemView.findViewById(R.id.declineAppButtonLec);
            seperator = itemView.findViewById(R.id.viewAppLec);
            applicationSemester = itemView.findViewById(R.id.applicationSemesterTextViewLecturer);
            applicationSalary = itemView.findViewById(R.id.applicationSalaryTextViewLecturer);
        }
    }

    @NonNull
    @Override
    public ApplicationAdapter.ApplicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.application_item_layout, parent, false);
        return new ApplicationAdapter.ApplicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApplicationAdapter.ApplicationViewHolder holder, int position) {
        if (applicationList.isEmpty()) {
            holder.seperator.setVisibility(View.GONE);
            holder.applicationTitle.setVisibility(View.GONE);
            holder.applicationStudentName.setVisibility(View.GONE);
            holder.applicationStudentNum.setVisibility(View.GONE);
            holder.applicationDescrip.setVisibility(View.GONE);
            holder.applicationType.setVisibility(View.GONE);
            holder.tvEmpty.setVisibility(View.VISIBLE);
            holder.downloadResume.setVisibility(View.GONE);
            holder.acceptApp.setVisibility(View.GONE);
            holder.declineApp.setVisibility(View.GONE);
            holder.applicationSalary.setVisibility(View.GONE);
            holder.applicationSemester.setVisibility(View.GONE);
            holder.tvEmpty.setText("No Applications");
        } else {
            holder.applicationTitle.setVisibility(View.VISIBLE);
            holder.applicationStudentName.setVisibility(View.VISIBLE);
            holder.applicationStudentNum.setVisibility(View.VISIBLE);
            holder.applicationDescrip.setVisibility(View.VISIBLE);
            holder.applicationType.setVisibility(View.VISIBLE);
            holder.tvEmpty.setVisibility(View.GONE);
            holder.downloadResume.setVisibility(View.VISIBLE);
            holder.acceptApp.setVisibility(View.VISIBLE);
            holder.declineApp.setVisibility(View.VISIBLE);
            holder.seperator.setVisibility(View.VISIBLE);
            holder.applicationSalary.setVisibility(View.VISIBLE);
            holder.applicationSemester.setVisibility(View.VISIBLE);

            Application application = applicationList.get(position);

            holder.applicationTitle.setText(application.getModule());
            holder.applicationStudentName.setText(application.getPersonName());
            holder.applicationStudentNum.setText(application.getStudent_num());
            holder.applicationDescrip.setText(application.getDescription());
            holder.applicationType.setText(application.getType());

            holder.applicationSemester.setText("Semester " + application.getSemester());
            holder.applicationSalary.setText(application.getSalary() + " per/hour");

            holder.downloadResume.setOnClickListener(view -> {
                DropboxInit dropboxInit = new DropboxInit();

                String fileOnDrpbx = "/" + application.getStudent_num() + "/" + application.getStudent_num() + ".pdf";


                File saveLocally = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), application.getStudent_num() + ".pdf");


                ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setTitle("");
                progressDialog.setMessage("Downloading Resume...");
                progressDialog.show();

                new Thread(() -> {
                    DownloadBuilder downloadBuilder = dropboxInit.client.files().downloadBuilder(fileOnDrpbx);

                    // Download the file and save it to the local file path
                    try (OutputStream outputStream = new FileOutputStream(saveLocally)) {
                        downloadBuilder.download(outputStream);
                        progressDialog.dismiss();
                        String auth = "com.example.iot_proj2.SPECIALAUTH";
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri fileUri = FileProvider.getUriForFile(context, auth, saveLocally);
                        if (fileUri != null) {
                            intent.setDataAndType(fileUri, "application/pdf");
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            context.startActivity(intent);
                        }

                        handler.post(() -> Toast.makeText(context, "Download Complete", Toast.LENGTH_SHORT).show());

                    } catch (DownloadErrorException e) {
                        progressDialog.dismiss();
                        handler.post(() -> Toast.makeText(context, "Error: Unable to Download Resume", Toast.LENGTH_SHORT).show());
                    } catch (IOException e) {
                        progressDialog.dismiss();
                        handler.post(() -> Toast.makeText(context, "Error: I/O Exception", Toast.LENGTH_SHORT).show());
                    } catch (DbxException e) {
                        progressDialog.dismiss();
                        handler.post(() -> Toast.makeText(context, "Error: Dropbox Exception", Toast.LENGTH_SHORT).show());
                    }
                }).start();
            });

            holder.acceptApp.setOnClickListener(view -> {
                new AlertDialog.Builder(context)
                        .setMessage("Are you sure you want to accept this Application?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            FirebaseFirestore FStore;
                            FStore = FirebaseFirestore.getInstance();

                            // Update Status of Application
                            FStore.collection("Application").document(Long.toString(application.getDocId())).update("status", "accepted")
                                    .addOnSuccessListener(unused -> {
                                        applicationList.remove(position);
                                        Toast.makeText(context, "Application Accepted", Toast.LENGTH_SHORT).show();
                                        notifyItemRemoved(position);

                                        // Update Vacancy
                                        FStore.collection("Vacancy").document(application.getVacancy_id()).update("status", "0")
                                                .addOnSuccessListener(unused1 -> {


                                                    // Get Student Details
                                                    DocumentReference StudentDetails = FStore.collection("Student").document(application.getStudent_num());
                                                    StudentDetails.get().addOnCompleteListener(task -> {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot details = task.getResult();

                                                            if (details.exists()) {
                                                                String name = details.getString("name");
                                                                String email = details.getString("email");
                                                                // Send email to Student that they have been accepted
                                                                new Thread(() -> {
                                                                    SendEmail(email, "Vacancy Portal - Application Accepted", "Hello, " + name + ".\n\nYou have been accepted as a " + application.getType() + " for " + application.getModule() + ".\n\nPlease contact the lecturer responsible for more information\n\nThank you.\nKind regards,\nVacancy Team.");
                                                                }).start();

                                                            }
                                                        }
                                                    });

                                                    // Create Student as Tutor/TA
                                                    DocumentReference Tutor = FStore.collection("Tutor").document(application.getStudent_num());
                                                    Tutor.get().addOnCompleteListener(task -> {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot tutorDetails = task.getResult();

                                                            if (tutorDetails.exists()) {
                                                                if (!tutorDetails.getString("type").equals(application.getType()) && "Teaching Assistant".equals(application.getType())) {
                                                                    // Update Tutor table
                                                                    FStore.collection("Tutor").document(application.getStudent_num()).update("type", application.getType());
                                                                }
                                                            } else {
                                                                // Create a new tutor document in collection
                                                                Map<String, Object> newTutor = new HashMap<>();
                                                                newTutor.put("type", application.getType());
                                                                FStore.collection("Tutor").document(application.getStudent_num()).set(newTutor);
                                                            }
                                                        }
                                                    });

                                                    // Get latest id for Module_Tutor collection
                                                    Query getLatestId = FStore.collection("Module_Tutor").orderBy("docId", Query.Direction.DESCENDING).limit(1);
                                                    getLatestId.get().addOnCompleteListener(task -> {
                                                        if (task.isSuccessful()) {
                                                            QuerySnapshot latestId = task.getResult();
                                                            int maxid = 1;
                                                            if (!latestId.isEmpty()) {
                                                                DocumentSnapshot documentSnapshot = latestId.getDocuments().get(0);
                                                                long id = documentSnapshot.getLong("docId");
                                                                maxid = (int) id + 1;
                                                            }

                                                            Map<String, Object> newModTutor = new HashMap<>();
                                                            newModTutor.put("tutor_id", application.getStudent_num());
                                                            newModTutor.put("staff_num", UserIDStatic.getInstance().getUserId());
                                                            newModTutor.put("module", application.getModule());
                                                            newModTutor.put("docId", maxid);

                                                            FStore.collection("Module_Tutor").document(Integer.toString(maxid)).set(newModTutor);

                                                        }
                                                    });

                                                    // Cancel all other applications for this vacancy
                                                    Query queryAllOther = FStore.collection("Application").whereEqualTo("vacancy_id", application.getVacancy_id()).whereNotEqualTo("student_num", application.getStudent_num());
                                                    queryAllOther.get().addOnCompleteListener(task -> {
                                                        if (task.isSuccessful()) {
                                                            QuerySnapshot otherApps = task.getResult();
                                                            if (otherApps != null) {
                                                                List<DocumentSnapshot> documentSnapshots = otherApps.getDocuments();

                                                                for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                                                                    for (int x = 0; x < applicationList.size(); x++) {
                                                                        Application app = applicationList.get(x);
                                                                        if (app.getStudent_num().equals(documentSnapshot.getString("student_num")) && app.getVacancy_id().equals(application.getVacancy_id())) {
                                                                            applicationList.remove(x);
                                                                            notifyItemRemoved(x);
                                                                        }
                                                                    }
                                                                    documentSnapshot.getReference().update("status", "declined");
                                                                }
                                                            }

                                                        }
                                                    });


                                                }).addOnFailureListener(
                                                        e -> {
                                                            // If Update Vacancy Fail
                                                        }
                                                );

                                    }).addOnFailureListener(e -> {
                                        // If update Application fail
                                    });


                        })
                        .setNegativeButton("No", null)
                        .show();
            });

            holder.declineApp.setOnClickListener(view -> {
                new AlertDialog.Builder(context)
                        .setMessage("Are you sure you want to decline this Application?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            FirebaseFirestore FStore;
                            FStore = FirebaseFirestore.getInstance();


                            // Update Status of Application
                            FStore.collection("Application").document(Long.toString(application.getDocId())).update("status", "declined").addOnSuccessListener(unused -> {

                                applicationList.remove(position);
                                Toast.makeText(context, "Application Declined", Toast.LENGTH_SHORT).show();
                                notifyItemRemoved(position);

                                // Get Student Details
                                DocumentReference StudentDetails = FStore.collection("Student").document(application.getStudent_num());
                                StudentDetails.get().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot details = task.getResult();

                                        if (details.exists()) {
                                            String name = details.getString("name");
                                            String email = details.getString("email");
                                            // Send email to Student that they have been declined
                                            new Thread(() -> {
                                                SendEmail(email, "Vacancy Portal - Application Declined", "Hello, " + name + ".\n\nUnfortunately your application has been declined for " + application.getModule() + ".\n\nPlease contact the lecturer responsible for more information\n\nThank you.\nKind regards,\nVacancy Team.");
                                            }).start();

                                        }
                                    }
                                });


                            });
                        }).setNegativeButton("No", null)
                        .show();
            });

        }
    }

    @Override
    public int getItemCount() {
        if (applicationList.isEmpty()) {
            return 1;
        } else {
            return applicationList.size();
        }
    }

    private void SendEmail(String email, String subject, String body) {
        String username = "iotgrp2023@gmail.com";
        String password = "qdqxulmrnbfkrqvg";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message messageObj = new MimeMessage(session);
            messageObj.setFrom(new InternetAddress(username));
            messageObj.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            messageObj.setSubject(subject);
            messageObj.setText(body);
            Transport.send(messageObj);
        } catch (MessagingException e) {

        }
    }
}
