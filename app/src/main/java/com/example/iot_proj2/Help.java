package com.example.iot_proj2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.SpannableStringBuilderKt;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Help extends AppCompatActivity {

    @BindView(R.id.btnHelpBack)
    Button Back;

    @BindView(R.id.edtHelpName)
    EditText Name;

    @BindView(R.id.edtHelpEmail)
    EditText Email;

    @BindView(R.id.edtHelpComment)
    EditText Comment;

    @BindView(R.id.btnHelpSubmit)
    Button Submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        ButterKnife.bind(this);

        Back.setOnClickListener(view -> {
            Intent intent = new Intent(Help.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        Submit.setOnClickListener(view -> {
            String name = Name.getText().toString().trim();
            String email = Email.getText().toString().trim();
            String comment = Comment.getText().toString().trim();

            if(TextUtils.isEmpty(name))
            {
                Name.setError("Please enter your Name");
                return;
            }

            if(TextUtils.isEmpty(email))
            {
                Email.setError("Please enter your Email");
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            {
                Email.setError("Invalid Email Address");
                return;
            }

            if (TextUtils.isEmpty(comment))
            {
                Comment.setError("Please enter a Comment/Issue");
                return;
            }

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("");
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();




            new Email(this, "iotgrp2023@gmail.com","Vacancy Portal - Comment/Issue Report",
                    "Hello.\n\nThere is a comment/issue that has been submitted by "+name+" ("+email +").\n\nTheir comment:\n**\n"+ comment +"\n**\n\nThank you.\nVacancy Portal System.","Comment/Issue Successfully Submitted","Unable to Submit Comment/Issue",progressDialog).execute();

            progressDialog.setOnDismissListener(dialogInterface -> {
                Intent intent = new Intent(Help.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        });
    }
}