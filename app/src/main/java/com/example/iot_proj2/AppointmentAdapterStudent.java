package com.example.iot_proj2;

import android.content.Context;
import android.icu.lang.UCharacter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

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

public class AppointmentAdapterStudent extends RecyclerView.Adapter<AppointmentAdapterStudent.AppointmentViewHolder>{

    private List<Appointment> appointmentList;

    private Context context;

    public AppointmentAdapterStudent(List<Appointment> appointmentList, Context context) {
        this.appointmentList = appointmentList;
        this.context = context;
    }

    public static class AppointmentViewHolder extends RecyclerView.ViewHolder
    {
        TextView appointmentTitle, appointmentTime, appointmentStatus, appointmentDate, tvEmpty;
        ImageView withdrawButton;
        View separator;

        public AppointmentViewHolder(View itemView)
        {
            super(itemView);
            appointmentTitle = itemView.findViewById(R.id.appointmentTitleTextViewStudent);
            appointmentTime = itemView.findViewById(R.id.appointmentTimeTextViewStudent);
            appointmentStatus = itemView.findViewById(R.id.appointmentStatusTextViewStudent);
            appointmentDate = itemView.findViewById(R.id.appointmentDateTextViewStudent);
            tvEmpty = itemView.findViewById(R.id.tvEmptyAppointmentStud);
            withdrawButton = itemView.findViewById(R.id.withdrawAppointmentButton);
            separator = itemView.findViewById(R.id.viewAppointmentStud);
        }

    }

    @NonNull
    @Override
    public AppointmentAdapterStudent.AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.appointment_item_layout_student, parent, false);
        return new AppointmentAdapterStudent.AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentAdapterStudent.AppointmentViewHolder holder, int position) {
        if(appointmentList.isEmpty())
        {
            holder.appointmentTitle.setVisibility(View.GONE);
            holder.appointmentTime.setVisibility(View.GONE);
            holder.appointmentStatus.setVisibility(View.GONE);
            holder.appointmentDate.setVisibility(View.GONE);
            holder.withdrawButton.setVisibility(View.GONE);
            holder.separator.setVisibility(View.GONE);
            holder.tvEmpty.setVisibility(View.VISIBLE);
            holder.tvEmpty.setText("No Appointments");
        } else {
            holder.appointmentTitle.setVisibility(View.VISIBLE);
            holder.appointmentTime.setVisibility(View.VISIBLE);
            holder.appointmentStatus.setVisibility(View.VISIBLE);
            holder.appointmentDate.setVisibility(View.VISIBLE);
            holder.withdrawButton.setVisibility(View.VISIBLE);
            holder.separator.setVisibility(View.VISIBLE);
            holder.tvEmpty.setVisibility(View.GONE);

            Appointment appointment = appointmentList.get(position);

            holder.appointmentTitle.setText("Appointment with "+appointment.getLecturerName());

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

            String status = appointment.getStatus();
            holder.appointmentStatus.setText(UCharacter.toTitleCase(Locale.UK,status,null,0));
            switch (status)
            {
                case "cancelled":holder.appointmentStatus.setTextColor(ContextCompat.getColor(context,R.color.Cancelled));
                break;
                case "pending" : holder.appointmentStatus.setTextColor(ContextCompat.getColor(context,R.color.Pending));
                break;
                case "approved" : holder.appointmentStatus.setTextColor(ContextCompat.getColor(context,R.color.Approved));
                break;
            }

            if("approved".equals(appointment.getStatus()) || "pending".equals(appointment.getStatus()))
            {
                holder.withdrawButton.setVisibility(View.VISIBLE);
            } else
            {
                holder.withdrawButton.setVisibility(View.GONE);
            }

            holder.withdrawButton.setOnClickListener(view -> {
                new AlertDialog.Builder(context)
                        .setMessage("Are you sure you want to cancel this appointment?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            FirebaseFirestore FStore;
                            FStore = FirebaseFirestore.getInstance();
                            FStore.collection("Appointment").document(Long.toString(appointment.getDocId())).update("status", "cancelled")
                                    .addOnSuccessListener(unused -> {
                                        if("approved".equals(appointment.getStatus()))
                                        {
                                            new Thread(() -> SendEmail(appointment.getLecturerEmail(),"Vacancy Portal - Appointment Cancelled with "+appointment.getStudentName()+"("+appointment.getStud_num()+")"
                                                    ,"Hello, "+appointment.getLecturerName()+".\n\n"+appointment.getStudentName()+"("+appointment.getStudentEmail()+")"+" has cancelled their appointment with you that was scheduled for "+appointment.getStart_time()+".\n\nThank you.\nKind regards,\nVacancy Team.")).start();
                                        }

                                        appointment.setStatus("cancelled");
                                        Toast.makeText(context, "Appointment has been Cancelled", Toast.LENGTH_SHORT).show();
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
    public int getItemCount () {
        if (appointmentList.isEmpty()) {
            return 1;
        } else {
            return appointmentList.size();
        }
    }
    private void SendEmail(String email, String subject, String body)
    {
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
