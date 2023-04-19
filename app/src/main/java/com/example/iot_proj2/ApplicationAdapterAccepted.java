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

public class ApplicationAdapterAccepted extends RecyclerView.Adapter<ApplicationAdapterAccepted.ApplicationViewHolder> {
    private List<Application> applicationList;

    private Context context;
    private Handler handler = new Handler(Looper.getMainLooper());


    public ApplicationAdapterAccepted(List<Application> applicationList, Context context) {
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
    public ApplicationAdapterAccepted.ApplicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.application_item_layout, parent, false);
        return new ApplicationAdapterAccepted.ApplicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApplicationAdapterAccepted.ApplicationViewHolder holder, int position) {
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
            holder.acceptApp.setVisibility(View.GONE);
            holder.declineApp.setVisibility(View.GONE);
            holder.seperator.setVisibility(View.VISIBLE);
            holder.applicationSalary.setVisibility(View.VISIBLE);
            holder.applicationSemester.setVisibility(View.VISIBLE);

            Application application = applicationList.get(position);

            holder.applicationTitle.setText(application.getModule());
            holder.applicationStudentName.setText(application.getPersonName());
            holder.applicationStudentNum.setText(application.getStudent_num());
            holder.applicationDescrip.setText(application.getDescription());
            holder.applicationType.setText(application.getType());

            holder.applicationSemester.setText("Semester "+application.getSemester());
            holder.applicationSalary.setText(application.getSalary()+" per/hour");


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
}
