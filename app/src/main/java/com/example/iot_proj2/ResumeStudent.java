package com.example.iot_proj2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DownloadBuilder;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ResumeStudent extends AppCompatActivity {

    @BindView(R.id.imgDownloadResume)
    ImageView Download;

    @BindView(R.id.edtStudResumeUpdate)
    EditText Resume;

    @BindView(R.id.btnStudResumeUpdatebtn)
    Button UpdateResume;

    private Uri fileUriUpdate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume_student);

        ButterKnife.bind(this);

        Download.setOnClickListener(view -> {
            DropboxInit dropboxInit = new DropboxInit();

            String fileOnDrpbx = "/"+UserIDStatic.getInstance().getUserId()+"/"+UserIDStatic.getInstance().getUserId()+".pdf";


            File saveLocally = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), UserIDStatic.getInstance().getUserId()+".pdf");


            ProgressDialog progressDialog = new ProgressDialog(this);
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
                    Uri fileUri = FileProvider.getUriForFile(this, auth,saveLocally);
                    if (fileUri != null) {
                        intent.setDataAndType(fileUri, "application/pdf");
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        this.startActivity(intent);
                    }

                    runOnUiThread(() -> Toast.makeText(this, "Download Complete", Toast.LENGTH_SHORT).show());
                } catch (DownloadErrorException e) {
                    progressDialog.dismiss();
                    runOnUiThread(() -> Toast.makeText(this, "Error: Unable to Download Resume", Toast.LENGTH_SHORT).show());
                } catch (IOException e) {
                    progressDialog.dismiss();
                    runOnUiThread(() -> Toast.makeText(this, "Error: I/O Exception", Toast.LENGTH_SHORT).show());
                } catch (DbxException e) {
                    progressDialog.dismiss();
                    runOnUiThread(() -> Toast.makeText(this, "Error: Dropbox Exception", Toast.LENGTH_SHORT).show());
                }
            }).start();


        });

        Resume.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            startActivityForResult(Intent.createChooser(intent,"Select Resume (PDF)"), 1);
        });

        UpdateResume.setOnClickListener(view -> {
            if (fileUriUpdate == null)
            {
                Resume.setError("Please choose your Resume file");
                return;
            }
            else
                Resume.setError(null);

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("");
            progressDialog.setMessage("Updating your Resume...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            UploadFileToDrbx(fileUriUpdate, UserIDStatic.getInstance().getUserId(),progressDialog);


        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK)
        {
            fileUriUpdate = data.getData();
            Resume.setText(getFileNameFromUri(fileUriUpdate));
        }
    }
    @SuppressLint("Range")
    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        String scheme = uri.getScheme();
        if (scheme.equals("file")) {
            fileName = uri.getLastPathSegment();
        } else if (scheme.equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                cursor.close();
            }
        }
        return fileName;
    }

    public void UploadFileToDrbx(Uri fileUri,String studNum, ProgressDialog p)
    {
        DropboxInit dropboxInit = new DropboxInit();
        new Thread(() -> {
            try (InputStream inputStream = getContentResolver().openInputStream(fileUri)) {

                String fileName = studNum + ".pdf";

                FileMetadata metadata = dropboxInit.client.files().uploadBuilder("/"+ studNum +"/"+ fileName)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(inputStream);

                p.dismiss();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Your resume has been updated.", Toast.LENGTH_SHORT).show();
                    fileUriUpdate = null;
                    Resume.setText("");
                });

            } catch (Exception e) {

            }
        }).start();
    }

}