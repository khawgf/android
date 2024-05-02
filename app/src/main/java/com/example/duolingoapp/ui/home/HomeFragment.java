package com.example.duolingoapp.ui.home;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.duolingoapp.R;
import com.example.duolingoapp.bocauhoi.QuestionListActivity;
import com.example.duolingoapp.xephang.RankingActivity;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    CardView cardViewHocTuVung, cardViewTracNghiem, cardViewSapXepCau, cardViewLuyenNghe,cardViewDienKhuyet,cardViewXepHang;
    SQLiteDatabase database;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        cardViewHocTuVung = root.findViewById(R.id.cardViewHocTuVung);
        cardViewDienKhuyet= root.findViewById(R.id.cardViewDienKhuyet);
        cardViewTracNghiem= root.findViewById(R.id.cardViewTracNghiem);
        cardViewSapXepCau = root.findViewById(R.id.cardViewSapXepCau);
        cardViewLuyenNghe = root.findViewById(R.id.cardViewLuyenNghe);
        cardViewXepHang = root.findViewById(R.id.cardViewXepHang);

        cardViewHocTuVung.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), QuestionListActivity.class);
                intent.putExtra("screenCurrent", "Học từ vựng");
                startActivity(intent);
            }
        });
        cardViewDienKhuyet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), QuestionListActivity.class);
                intent.putExtra("screenCurrent", "Điền khuyết");
                startActivity(intent);
            }
        });
        cardViewTracNghiem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), QuestionListActivity.class);
                intent.putExtra("screenCurrent", "Trắc nghiệm");
                startActivity(intent);
            }
        });
        cardViewSapXepCau.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), QuestionListActivity.class);
                intent.putExtra("screenCurrent", "Sắp xếp câu");
                startActivity(intent);
            }
        });
        cardViewLuyenNghe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), QuestionListActivity.class);
                intent.putExtra("screenCurrent", "Luyện nghe");
                startActivity(intent);
            }
        });
        cardViewXepHang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RankingActivity.class);
                startActivity(intent);
            }
        });

        return root;
    }



}