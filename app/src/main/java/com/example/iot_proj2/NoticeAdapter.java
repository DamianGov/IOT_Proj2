package com.example.iot_proj2;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder> {


    private final List<Notice> noticeList;

    private final Context context;

    public NoticeAdapter(List<Notice> noticeList, Context context) {
        this.noticeList = noticeList;
        this.context = context;
    }

    public static class NoticeViewHolder extends RecyclerView.ViewHolder
    {

        TextView Title, Description, Time, Empty;

        public NoticeViewHolder(@NonNull View itemView) {
            super(itemView);
            Title = itemView.findViewById(R.id.noticeTitleTextView);
            Description = itemView.findViewById(R.id.NoticeDescriptionTextView);
            Time = itemView.findViewById(R.id.NoticeTimeStamp);
            Empty = itemView.findViewById(R.id.tvEmptyNoticeLec);
        }
    }

    @NonNull
    @Override
    public NoticeAdapter.NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notice_board_item_lecturer, parent, false);
        return new NoticeAdapter.NoticeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeAdapter.NoticeViewHolder holder, int position) {
        if(noticeList.isEmpty())
        {
            holder.Title.setVisibility(View.GONE);
            holder.Description.setVisibility(View.GONE);
            holder.Time.setVisibility(View.GONE);
            holder.Empty.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(197, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER;
            holder.Empty.setLayoutParams(layoutParams);
            holder.Empty.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            holder.Empty.setText("No Notices");
        } else {
            holder.Title.setVisibility(View.VISIBLE);
            holder.Description.setVisibility(View.VISIBLE);
            holder.Time.setVisibility(View.VISIBLE);
            holder.Empty.setVisibility(View.GONE);

            Notice notice = noticeList.get(position);

            holder.Title.setText(notice.getTitle());
            holder.Description.setText(notice.getDescription());
            holder.Time.setText("Created On " + notice.getTime());

            if (notice.isExpired()) {
                holder.Title.setTextColor(ContextCompat.getColor(context, R.color.Withdrawn));
                holder.Description.setTextColor(ContextCompat.getColor(context, R.color.Withdrawn));
                holder.Time.setTextColor(ContextCompat.getColor(context, R.color.Withdrawn));
            } else {

                FirebaseFirestore FStore;
                FStore = FirebaseFirestore.getInstance();

                holder.Title.setOnLongClickListener(view -> {
                    new AlertDialog.Builder(context)
                            .setMessage("Are you sure you want to disable this Notice, it cannot be enabled?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", (dialogInterface, i) -> {
                                FStore.collection("Note").document(Long.toString(notice.getDocId())).update("expired", true)
                                        .addOnSuccessListener(aVoid -> {
                                            notice.setExpired(true);
                                            Toast.makeText(context, "Notice Disabled", Toast.LENGTH_SHORT).show();
                                            notifyDataSetChanged();

                                        })
                                        .addOnFailureListener(e -> {
                                        });
                            })
                            .setNegativeButton("No", null)
                            .show();
                    return true;
                });


            }
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
