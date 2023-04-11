package com.example.iot_proj2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VacancyBoardLecturer extends AppCompatActivity {

    @BindView(R.id.rvLecVacBoard)
    RecyclerView VacBoard;

    private FirebaseFirestore FStore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacancy_board_lecturer);

        ButterKnife.bind(this);

        FStore = FirebaseFirestore.getInstance();

        Query query = FStore.collection("Vacancy").whereEqualTo("created_by", UserIDStatic.getInstance().getUserId());
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        QuerySnapshot querySnapshot = task.getResult();
                        if(querySnapshot != null && !querySnapshot.isEmpty())
                        {
                            List<Vacancy> vacancyList = new ArrayList<>();
                            for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments())
                            {
                                Vacancy vacancy = documentSnapshot.toObject(Vacancy.class);
                                vacancyList.add(vacancy);
                            }

                            VacancyAdapter vacancyAdapter = new VacancyAdapter(vacancyList,this);
                            VacBoard.setAdapter(vacancyAdapter);

                            LinearLayoutManager layoutManager = new LinearLayoutManager(VacancyBoardLecturer.this);
                            VacBoard.setLayoutManager(layoutManager);
                        }
                    }
                });

    }
}