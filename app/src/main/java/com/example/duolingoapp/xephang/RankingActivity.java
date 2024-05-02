package com.example.duolingoapp.xephang;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.duolingoapp.MainActivity;
import com.example.duolingoapp.R;
import com.example.duolingoapp.taikhoan.DatabaseAccess;
import com.example.duolingoapp.taikhoan.User;
import com.example.duolingoapp.trangthai.Status;
import com.example.duolingoapp.trangthai.StatusDB;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class RankingActivity extends AppCompatActivity {

    private TextView backRanking;
    private DatabaseReference mDatabase;
    ArrayList<Status> statusList;
    RankingAdapter adapter;
    RecyclerView rvStatus;
    DatabaseAccess DB;
    StatusDB statusDB;

    @SuppressLint({"MissingInflatedId", "NotifyDataSetChanged"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ranking);

        DB = DatabaseAccess.getInstance(getApplicationContext());
        statusDB = StatusDB.getInstance(getApplicationContext());

        backRanking = findViewById(R.id.txtBack_Ranking);
        rvStatus = findViewById(R.id.recyclerViewListRanking);

        backRanking.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RankingActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        ArrayList<Status> statusListFromSQLite = statusDB.getStatusListFromSQLite();
        if (statusListFromSQLite != null && !statusListFromSQLite.isEmpty()) {
            // Nếu có dữ liệu từ SQLite, tính toán xếp hạng và hiển thị
            calculateRanking(statusListFromSQLite);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(RankingActivity.this);
            rvStatus.setLayoutManager(layoutManager);
            adapter = new RankingAdapter(RankingActivity.this, R.layout.list_item_ranking, statusList, DB.getUserListFromSQLite());
            rvStatus.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

//        mDatabase = FirebaseDatabase.getInstance().getReference().child("TrangThai");
//
//        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                ArrayList<Status> trangThaiList = new ArrayList<>();
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    Status trangThai = snapshot.getValue(Status.class);
//                    trangThaiList.add(trangThai);
//                }
//                calculateRanking(trangThaiList);
//                getUserList();
//            }
//        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void calculateRanking(ArrayList<Status> trangThaiList) {
        HashMap<String, Integer> userScoreMap = new HashMap<>();

        // nhóm các dòng có cùng idUser, tính tổng điểm
        for (Status trangThai : trangThaiList) {
            String idUser = trangThai.getIdUser();
            int score = trangThai.getScore();

            if (userScoreMap.containsKey(idUser)) {
                int currentScore = userScoreMap.get(idUser);
                userScoreMap.put(idUser, currentScore + score);
            } else {
                userScoreMap.put(idUser, score);
            }
        }

        statusList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : userScoreMap.entrySet()) {
            String idUser = entry.getKey();
            int totalScore = entry.getValue();

            // Tạo một đối tượng TrangThai mới với idUser và tổng điểm
            Status trangThai = new Status();
            trangThai.setIdUser(idUser);
            trangThai.setScore(totalScore);

            statusList.add(trangThai);
        }

        Collections.sort(statusList, new Comparator<Status>() {
            @Override
            public int compare(Status trangThai1, Status trangThai2) {
                // Sắp xếp theo giảm dần của điểm
                return Integer.compare(trangThai2.getScore(), trangThai1.getScore());
            }
        });
        //getUserList();
    }

    public void getUserList(){
        // Gọi hàm getListUser để lấy danh sách người dùng
        DB.getListUser(new DatabaseAccess.OnUserListListener() {
            @Override
            public void onUserListRetrieved(ArrayList<User> userList) {
                // Gán danh sách người dùng vào biến userList
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(RankingActivity.this);
                rvStatus.setLayoutManager(layoutManager);
                adapter = new RankingAdapter(RankingActivity.this, R.layout.list_item_ranking, statusList, userList);
                rvStatus.setAdapter(adapter);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onUserListError(String errorMessage) {
                // Xử lý khi có lỗi xảy ra khi lấy danh sách người dùng
                Log.e("RankingActivity", "Error retrieving user list: " + errorMessage);
            }
        });
    }
}