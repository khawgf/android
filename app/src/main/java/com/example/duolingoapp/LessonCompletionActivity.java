package com.example.duolingoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.duolingoapp.bocauhoi.QuestionListActivity;

public class LessonCompletionActivity extends AppCompatActivity {

    TextView txtFinalQTrue;
    TextView txtFinalText;
    TextView txtFinalScore;
    TextView txtTimeDone;
    Button btnReturn;
    int score;
    int questionTrue;
    int qcount;
    String nameScreen, timedone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lesson_completion);

        Intent intent=getIntent();
        score=intent.getIntExtra("score",0);
        questionTrue = intent.getIntExtra("questiontrue",0);
        qcount = intent.getIntExtra("qcount",0);
        timedone = intent.getStringExtra("timedone");
        nameScreen = getIntent().getStringExtra("screenCurrent");
        AnhXa();

        if(questionTrue==qcount){
            txtFinalText.setText("Lesson Completion!");
        }
        if(questionTrue<qcount){
            txtFinalText.setText("Good luck next time!");
        }

//        double percentage = ((double) questionTrue / qcount) * 100;
//        String result = String.format("%.0f%%", percentage);
//        txtFinalQTrue.setText(result);
        txtFinalQTrue.setText(questionTrue + " / " + qcount);
        txtFinalScore.setText(" "+score);
        txtTimeDone.setText(timedone);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LessonCompletionActivity.this, QuestionListActivity.class);
                intent.putExtra("screenCurrent", nameScreen);
                startActivity(intent);
            }
        });

    }

    public void AnhXa(){
        txtFinalScore=findViewById(R.id.txtFinalScore);
        txtFinalQTrue=findViewById(R.id.txtPerfect);
        txtFinalText=findViewById(R.id.txtFinalText);
        txtTimeDone=findViewById(R.id.txtTime_FinishFB);
        btnReturn =findViewById(R.id.btnReturn_FinishFB);
    }
}