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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private final List<Appointment> appointmentList;

    private final Context context;

    public AppointmentAdapter(List<Appointment> appointmentList, Context context) {
        this.appointmentList = appointmentList;
        this.context = context;
    }

    public static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView appointmentTitle, appointmentTime, appointmentStatus, appointmentDate, tvEmpty, appointmentReason;
        ImageView approveButton, cancelButton;
        View separator;

        public AppointmentViewHolder(View itemView) {
            super(itemView);
            appointmentTitle = itemView.findViewById(R.id.appointmentTitleTextView);
            appointmentTime = itemView.findViewById(R.id.appointmentTimeTextView);
            appointmentStatus = itemView.findViewById(R.id.appointmentStatusTextView);
            appointmentDate = itemView.findViewById(R.id.appointmentDateTextView);
            tvEmpty = itemView.findViewById(R.id.tvEmptyAppointmentLec);
            approveButton = itemView.findViewById(R.id.approveAppointmentButtonLec);
            cancelButton = itemView.findViewById(R.id.cancelAppointmentButtonLec);
            separator = itemView.findViewById(R.id.viewAppointmentLec);
            appointmentReason = itemView.findViewById(R.id.appointmentReasonTextView);
        }

    }

    @NonNull
    @Override
    public AppointmentAdapter.AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.appointment_item_layout, parent, false);
        return new AppointmentAdapter.AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentAdapter.AppointmentViewHolder holder, int position) {
        if (appointmentList.isEmpty()) {
            holder.appointmentTitle.setVisibility(View.GONE);
            holder.appointmentTime.setVisibility(View.GONE);
            holder.appointmentStatus.setVisibility(View.GONE);
            holder.appointmentDate.setVisibility(View.GONE);
            holder.approveButton.setVisibility(View.GONE);
            holder.cancelButton.setVisibility(View.GONE);
            holder.separator.setVisibility(View.GONE);
            holder.appointmentReason.setVisibility(View.GONE);
            holder.tvEmpty.setVisibility(View.VISIBLE);
            holder.tvEmpty.setText("No Appointments");
        } else {
            holder.appointmentTitle.setVisibility(View.VISIBLE);
            holder.appointmentTime.setVisibility(View.VISIBLE);
            holder.appointmentStatus.setVisibility(View.VISIBLE);
            holder.appointmentDate.setVisibility(View.VISIBLE);
            holder.approveButton.setVisibility(View.VISIBLE);
            holder.cancelButton.setVisibility(View.VISIBLE);
            holder.separator.setVisibility(View.VISIBLE);
            holder.appointmentReason.setVisibility(View.VISIBLE);
            holder.tvEmpty.setVisibility(View.GONE);

            Appointment appointment = appointmentList.get(position);

            holder.appointmentTitle.setText("Appointment with " + appointment.getStudentName() + "(" + appointment.getStud_num() + ")");

            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

            String dateOutput = "", timeOutput = "";
            try {
                Date date = inputFormat.parse(appointment.getStart_time());
                dateOutput = dateFormat.format(date);
                timeOutput = timeFormat.format(date);
            } catch (ParseException e) {

            }

            holder.appointmentTime.setText(timeOutput);
            holder.appointmentDate.setText(dateOutput);
            holder.appointmentReason.setText(appointment.getReason());

            String status = appointment.getStatus();

            holder.appointmentStatus.setText(UCharacter.toTitleCase(Locale.UK, status, null, 0));

            switch (status) {
                case "cancelled":
                    holder.appointmentStatus.setTextColor(ContextCompat.getColor(context, R.color.Cancelled));
                    break;
                case "pending":
                    holder.appointmentStatus.setTextColor(ContextCompat.getColor(context, R.color.Pending));
                    break;
                case "approved":
                    holder.appointmentStatus.setTextColor(ContextCompat.getColor(context, R.color.Approved));
                    break;
            }

            if ("pending".equals(appointment.getStatus())) {
                holder.approveButton.setVisibility(View.VISIBLE);
            } else {
                holder.approveButton.setVisibility(View.GONE);
            }

            if ("approved".equals(appointment.getStatus()) || "pending".equals(appointment.getStatus())) {
                holder.cancelButton.setVisibility(View.VISIBLE);
            } else {
                holder.cancelButton.setVisibility(View.GONE);
            }

            holder.approveButton.setOnClickListener(view -> {
                new AlertDialog.Builder(context)
                        .setMessage("Are you sure you want to approve this appointment?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            FirebaseFirestore FStore;
                            FStore = FirebaseFirestore.getInstance();

                            // Update Appointment
                            FStore.collection("Appointment").document(Long.toString(appointment.getDocId())).update("status", "approved").addOnSuccessListener(unused -> {

                                new Thread(() -> SendEmail(appointment.getStudentEmail(), "Vacancy Portal - Appointment Approved", "Hello, " + appointment.getStudentName() + ".\n\nYour appointment with " + appointment.getLecturerName() + " scheduled for " + appointment.getStart_time() + " has been approved.\nPlease take note of the Date and Time of your appointment.\n\nThe venue for your appointment is in the lecturer's office.\nPlease locate the relevant faculty the lecturer belongs to.\n\nThank you.\nKind regards,\nVacancy Team.")).start();

                                appointment.setStatus("approved");
                                Toast.makeText(context, "Appointment has been approved", Toast.LENGTH_SHORT).show();
                                notifyDataSetChanged();
                            });

                            Query queryAllOtherAppointment = FStore.collection("Appointment").whereEqualTo("staff_num", UserIDStatic.getInstance().getUserId()).whereEqualTo("start_time", appointment.getStart_time()).whereNotEqualTo("stud_num", appointment.getStud_num());
                            queryAllOtherAppointment.get().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    QuerySnapshot otherAppoint = task.getResult();
                                    if (otherAppoint != null) {
                                        List<DocumentSnapshot> documentSnapshots = otherAppoint.getDocuments();

                                        for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                                            for (int x = 0; x < appointmentList.size(); x++) {
                                                Appointment appLoop = appointmentList.get(x);
                                                if (appLoop.getStud_num().equals(documentSnapshot.getString("stud_num")) && "pending".equals(documentSnapshot.getString("status"))) {
                                                    appointmentList.get(x).setStatus("cancelled");

                                                }
                                            }
                                            documentSnapshot.getReference().update("status", "cancelled");
                                        }
                                        notifyDataSetChanged();
                                    }
                                }
                            });


                        }).setNegativeButton("No", null)
                        .show();
            });


            holder.cancelButton.setOnClickListener(view -> {
                new AlertDialog.Builder(context)
                        .setMessage("Are you sure you want to cancel this appointment?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            FirebaseFirestore FStore;
                            FStore = FirebaseFirestore.getInstance();
                            FStore.collection("Appointment").document(Long.toString(appointment.getDocId())).update("status", "cancelled")
                                    .addOnSuccessListener(unused -> {

                                        new Thread(() -> SendEmail(appointment.getStudentEmail(), "Vacancy Portal - Appointment Cancelled"
                                                , "Hello, " + appointment.getStudentName() + ".\n\nUnfortunately your appointment with " + appointment.getLecturerName() + " scheduled for " + appointment.getStart_time() + " has been cancelled.\n\nThank you.\nKind regards,\nVacancy Team.")).start();

                                        appointment.setStatus("cancelled");
                                        Toast.makeText(context, "Appointment has been Cancelled", Toast.LENGTH_SHORT).show();
                                        notifyDataSetChanged();
                                    }).addOnFailureListener(e -> {
                                    });


                        })
                        .setNegativeButton("No", null)
                        .show();
            });
        }
    }

    @Override
    public int getItemCount() {
        if (appointmentList.isEmpty()) {
            return 1;
        } else {
            return appointmentList.size();
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
