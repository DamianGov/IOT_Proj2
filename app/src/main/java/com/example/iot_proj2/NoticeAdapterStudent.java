package com.example.iot_proj2;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class NoticeAdapterStudent extends RecyclerView.Adapter<NoticeAdapterStudent.NoticeViewHolderStudent> {


    private final List<Notice> noticeList;

    private final Context context;

    public NoticeAdapterStudent(List<Notice> noticeList, Context context) {
        this.noticeList = noticeList;
        this.context = context;
    }

    public static class NoticeViewHolderStudent extends RecyclerView.ViewHolder
    {

        TextView Title, Description, Time, Empty, Lecturer;

        public NoticeViewHolderStudent(@NonNull View itemView) {
            super(itemView);
            Title = itemView.findViewById(R.id.noticeTitleTextViewStud);
            Description = itemView.findViewById(R.id.NoticeDescriptionTextViewStud);
            Time = itemView.findViewById(R.id.NoticeTimeStampStud);
            Empty = itemView.findViewById(R.id.tvEmptyNoticeStud);
            Lecturer = itemView.findViewById(R.id.NoticeLecturerStud);
        }
    }

    @NonNull
    @Override
    public NoticeAdapterStudent.NoticeViewHolderStudent onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notice_board_item_student, parent, false);
        return new NoticeAdapterStudent.NoticeViewHolderStudent(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeAdapterStudent.NoticeViewHolderStudent holder, int position) {
        if(noticeList.isEmpty())
        {
            holder.Title.setVisibility(View.GONE);
            holder.Description.setVisibility(View.GONE);
            holder.Time.setVisibility(View.GONE);
            holder.Lecturer.setVisibility(View.GONE);
            holder.Empty.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(197, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER;
            holder.Empty.setLayoutParams(layoutParams);
            holder.Empty.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            holder.Empty.setText("No Notices");
        } else {
            holder.Title.setVisibility(View.VISIBLE);
            holder.Description.setVisibility(View.VISIBLE);
            holder.Lecturer.setVisibility(View.VISIBLE);
            holder.Time.setVisibility(View.VISIBLE);
            holder.Empty.setVisibility(View.GONE);

            Notice notice = noticeList.get(position);

            holder.Title.setText(notice.getTitle());
            holder.Description.setText(notice.getDescription());
            holder.Time.setText("Created On " + notice.getTime());
            holder.Lecturer.setText("Created By: "+notice.getLecturer());

        }
    }
    @Override
    public int getItemCount() {
        if (noticeList.isEmpty()) {
            return 1;
        } else {
            return noticeList.size();
        }
    }
}
