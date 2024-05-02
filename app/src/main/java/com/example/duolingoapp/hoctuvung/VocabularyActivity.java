package com.example.duolingoapp.hoctuvung;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.duolingoapp.R;
import com.example.duolingoapp.bocauhoi.QuestionListActivity;
import com.example.duolingoapp.dienkhuyet.FillBlanksActivity;
import com.example.duolingoapp.taikhoan.DatabaseAccess;
import com.example.duolingoapp.ui.home.Database;

import java.util.ArrayList;

public class VocabularyActivity extends AppCompatActivity {

    final  String DATABASE_NAME = "HocNgonNgu.db";
    SQLiteDatabase database;
    DatabaseAccess DB;

    TextView txtWordENG_Voc, txtWordVIE_Voc, txtWordSpelling_Voc, txtWordPronoun_Voc, txtBack;
    Button btnContinue;
    ImageView img_Voc;
    private ProgressBar progressBar;
    int idBo;
    String nameScreen;
    int questionCurrent = 0;
    ArrayList<Vocabulary> vocList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_vocabulary);

        DB = DatabaseAccess.getInstance(getApplicationContext());

        AnhXa();
        progressBar.setProgress(0); // Khởi tạo giá trị tiến độ ban đầu là 0%

        Intent intent=getIntent();
        idBo=intent.getIntExtra("idBo",0);
        nameScreen = getIntent().getStringExtra("screenCurrent");

        vocList = getListVoc();

        showNextQuestion(questionCurrent);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                questionCurrent++;
                showNextQuestion(questionCurrent);
            }
        });

        txtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(VocabularyActivity.this, QuestionListActivity.class);
                intent.putExtra("screenCurrent", nameScreen);
                startActivity(intent);
            }
        });

    }

    public void AnhXa(){
        txtWordENG_Voc = findViewById(R.id.txtWordENG_Voc);
        txtWordVIE_Voc = findViewById(R.id.txtWordVIE_Voc);
        txtWordSpelling_Voc = findViewById(R.id.txtWordSpelling_Voc);
        txtWordPronoun_Voc = findViewById(R.id.txtWordPronoun_Voc);
        txtBack = findViewById(R.id.txtBackVoc);
        btnContinue = findViewById(R.id.btnContinue_Voc);
        img_Voc = findViewById(R.id.img_Voc);
        progressBar = findViewById(R.id.progressBar_Voc);
    }

    private ArrayList<Vocabulary> getListVoc(){
        ArrayList<Vocabulary> list = new ArrayList<>();

        database = Database.initDatabase(VocabularyActivity.this, DATABASE_NAME);
        Cursor cursor = database.rawQuery("SELECT * FROM TuVung WHERE ID_Bo = ?",new String[]{String.valueOf(idBo)});

        for (int i = 0; i < cursor.getCount(); i++){
            cursor.moveToPosition(i);
            int idtu = cursor.getInt(0);
            int idbo = cursor.getInt(1);
            String dapan = cursor.getString(2);
            String dichnghia = cursor.getString(3);
            String loaitu = cursor.getString(4);
            String audio = cursor.getString(5);
            byte[] anh = cursor.getBlob(6);

            list.add(new Vocabulary(idtu,idbo,dapan,dichnghia,loaitu,audio,anh));
        }
        return list;
    }

    @SuppressLint("SetTextI18n")
    public void showNextQuestion(int pos) {

        // Cập nhật giá trị tiến độ của ProgressBar
        int totalQuestions = vocList.size();
        int progressValue = ((pos + 1) * 100) / (totalQuestions+1); // Tính toán giá trị tiến độ
        progressBar.setProgress(progressValue);

        if (pos == totalQuestions) {
            btnContinue.setText("Practice testing");
            Intent intent = new Intent(VocabularyActivity.this, VocabularyTestActivity.class);
            intent.putExtra("idBo", idBo);
            intent.putExtra("screenCurrent", nameScreen);
            startActivity(intent);

        } else if(pos < totalQuestions) {
            Vocabulary voc = vocList.get(pos);
            txtWordENG_Voc.setText(voc.getDapan());
            txtWordVIE_Voc.setText(voc.getDichnghia());
            txtWordPronoun_Voc.setText("(" + voc.getLoaitu() + ")");
            Bitmap img= BitmapFactory.decodeByteArray(voc.getAnh(),0,voc.getAnh().length);
            img_Voc.setImageBitmap(img);
        }
    }

}