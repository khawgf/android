package com.example.duolingoapp.taikhoan;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.duolingoapp.MainActivity;
import com.example.duolingoapp.R;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import com.example.duolingoapp.trangthai.StatusDB;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ProfileActivity extends AppCompatActivity {

    TextView txtSettings,txtBack, txtSDT, txtEmail, txtName;
    ImageView imgUser;
    DatabaseAccess DB;
    User user;
    Button btnShare;
    private BarChart barChart;
    private StatusDB statusDB;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        DB = DatabaseAccess.getInstance(getApplicationContext());
        statusDB = StatusDB.getInstance(getApplicationContext());

        txtSettings = findViewById(R.id.txtSettings);
        txtBack = findViewById(R.id.txtBackProfile);
        txtName = findViewById(R.id.txtName_Profile);
        txtEmail = findViewById(R.id.txtEmail_Profile);
        txtSDT = findViewById(R.id.txtSDT_Profile);
        imgUser = findViewById(R.id.imgUser_Profile);
        btnShare = findViewById(R.id.btnShare_Profile);
        barChart = findViewById(R.id.barChart);

        if (DB.iduser!=null){
            user = DB.getUser(DB.iduser);

            txtName.setText(user.getHoTen());
            txtEmail.setText(user.getEmail());
            txtSDT.setText(user.getSDT());
        }

        if (user.getImg() == null || user.getImg().length == 0) {
            // Nếu không có dữ liệu, gán hình ảnh mặc định
            imgUser.setImageResource(R.drawable.icon_user);
        } else {
            Bitmap img = BitmapFactory.decodeByteArray(user.getImg(), 0, user.getImg().length);
            imgUser.setImageBitmap(img);
        }

        // Lấy tổng điểm theo kỹ năng từ bảng TrangThai
        HashMap<Integer, Integer> totalScoreBySkill = statusDB.getScoreBySkill(DB.iduser);

        if (totalScoreBySkill.size()>0){
            // Thống kê dữ liệu
            HashMap<String, Integer> chartData = getStatusData(totalScoreBySkill);

            // Hiển thị dữ liệu trên biểu đồ
            displayBarChart(chartData);
        }


        txtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        txtSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, SettingActivity.class);
                intent.putExtra("back", "profile");
                startActivity(intent);
                finish();
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "https://www.upload-apk.com/en/0LF67O3Wq1JFnRu");
                sendIntent.setType("text/plain");

                // Tạo một Intent Chooser với các ứng dụng có thể xử lý Intent chia sẻ
                Intent chooserIntent = Intent.createChooser(sendIntent, "Chia sẻ thông qua...");

                // Kiểm tra xem có ít nhất một ứng dụng có thể xử lý Intent không
                if (sendIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(chooserIntent);
                } else {
                    Toast.makeText(ProfileActivity.this, "Không có ứng dụng có thể xử lý chia sẻ.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private HashMap<String, Integer> getStatusData(HashMap<Integer, Integer> totalScoreBySkill) {
        StatusDB statusDB = StatusDB.getInstance(getApplicationContext());
        statusDB.open();

        // Lấy danh sách kỹ năng từ bảng Skill
        HashMap<Integer, String> skills = statusDB.getSkills();

        // Tạo một HashMap mới để lưu trữ dữ liệu biểu đồ
        HashMap<String, Integer> chartData = new HashMap<>();
        // Tạo dữ liệu biểu đồ dựa trên kỹ năng và tổng điểm
        for (Map.Entry<Integer, String> skillEntry : skills.entrySet()) {
            int skillId = skillEntry.getKey();
            String skillName = skillEntry.getValue();

            // Lấy tổng điểm theo kỹ năng
            if (totalScoreBySkill.containsKey(skillId)) {
                int totalScore = totalScoreBySkill.get(skillId);
                chartData.put(skillName, totalScore);
            } else {
                chartData.put(skillName, 0);
            }

        }

        statusDB.close();
        return chartData;
    }

    private void displayBarChart(HashMap<String, Integer> chartData) {
        List<String> skillNames = new ArrayList<>(chartData.keySet()); //skill name
        List<Integer> skillData = new ArrayList<>(chartData.values()); //score

        barChart.getAxisRight().setDrawLabels(false);

        ArrayList<BarEntry> entries = new ArrayList<>();

        // Tìm giá trị lớn nhất trong dữ liệu
        float maxValue = Float.MIN_VALUE; // Giá trị nhỏ nhất của float

        // Duyệt qua mỗi kỹ năng và thêm dữ liệu vào entries
        for (int i = 0; i < skillNames.size(); i++) {
            int value = skillData.get(i);

            // Thêm BarEntry vào entries với giá trị x là chỉ số của kỹ năng và giá trị y là tổng điểm
            entries.add(new BarEntry(i, value));

            if (value > maxValue) {
                maxValue = value;
            }

        }

        YAxis yAxis = barChart.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(maxValue); // Sử dụng giá trị lớn nhất trong dữ liệu
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisLineColor(Color.BLACK);
        yAxis.setLabelCount(10, true); // Đặt cách nhau 10 đơn vị và phân bố đều các nhãn
        yAxis.setGranularity(1f);

        BarDataSet dataSet = new BarDataSet(entries, "Skill");

        // Gán mảng màu cho DataSet
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        barChart.getDescription().setEnabled(false);
        barChart.invalidate();

        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(skillNames));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setGranularityEnabled(true);
    }
    
}