package com.example.duolingoapp.dienkhuyet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.example.duolingoapp.LessonCompletionActivity;
import com.example.duolingoapp.MainActivity;
import com.example.duolingoapp.R;
import com.example.duolingoapp.bocauhoi.QuestionListActivity;
import com.example.duolingoapp.taikhoan.DatabaseAccess;
import com.example.duolingoapp.taikhoan.User;
import com.example.duolingoapp.trangthai.Status;
import com.example.duolingoapp.trangthai.StatusDB;
import com.example.duolingoapp.ui.home.Database;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import android.graphics.Typeface;

import java.util.ArrayList;

public class FillBlanksActivity extends AppCompatActivity {

    final  String DATABASE_NAME = "HocNgonNgu.db";
    SQLiteDatabase database;
    DatabaseAccess DB;
    TextView txtScore,txtQuestCount,txtQuestion, txtTime,txtTip, txtBack;
    EditText edtAnswer;
    Button btnContinue;
    int questionCurrent = 0;
    int questionTrue = 0;
    String answer, nameScreen;
    int score=0;
    int idBo;
    User user;
    private ProgressBar progressBar;
    private final int DISPLAY_DURATION = 1400; //Thời gian đóng bottom sheet
    private BottomSheetDialog bottomSheetDialog;
    private CoordinatorLayout coordinatorLayout;

    // Khai báo biến đếm thời gian
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis=30000; // Thời gian 30s của mỗi câu hỏi

    private long startTimeMillis; // Thời gian bắt đầu làm bài
    private boolean isEndOfQuestions = false; // Biến cờ để đánh dấu khi kết thúc câu hỏi
    StatusDB statusDB;
    ArrayList<FillBlank> fillBlanks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_fill_blanks);
        DB = DatabaseAccess.getInstance(getApplicationContext());
        statusDB = StatusDB.getInstance(getApplicationContext());

        AnhXa();
        progressBar.setProgress(0); // Khởi tạo giá trị tiến độ ban đầu là 0%
        changeBtnColor();
//        user = DB.getUser(DB.iduser);
        Intent intent=getIntent();
        idBo=intent.getIntExtra("idBo",0);
        nameScreen = getIntent().getStringExtra("screenCurrent");
        fillBlanks = getList();

        //Thời gian để xem đáp án, sau đó hiện câu hỏi tiếp theo
        CountDownTimer nextScreenDisplayTime =new CountDownTimer(3000,2000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                questionCurrent++;
                edtAnswer.setTextColor(Color.BLACK);
                btnContinue.setEnabled(true);
                edtAnswer.setText("");
                answer="";
                showNextQuestion(questionCurrent);
            }
        };

        showNextQuestion(questionCurrent);

        // Khởi tạo thời gian bắt đầu làm bài
        startTimeMillis = System.currentTimeMillis();
        startTimerForQuestionCompletion();


        // Khởi tạo CountDownTimer với thời gian 30s của mỗi câu hỏi
        updateCountdownText(); //hiện time trên giao diện
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Cập nhật thời gian còn lại sau mỗi tick (1 giây)
                timeLeftInMillis = millisUntilFinished;
                updateCountdownText();
            }

            @Override
            public void onFinish() {
                // Xử lý khi đếm ngược kết thúc (hết thời gian)
                timeLeftInMillis = 0;
                updateCountdownText();
                checkAnswer();
                showanswer();

                nextScreenDisplayTime.start();
                // Bắt đầu lại đếm thời gian
                restartCountdownTimer();
            }
        };

        countDownTimer.start(); //bắt đầu đếm ngược 30s

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer();
                showanswer();

                nextScreenDisplayTime.start();
                // Bắt đầu lại đếm thời gian
                restartCountdownTimer();
            }
        });
        txtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                countDownTimer.cancel();
                Intent intent = new Intent(FillBlanksActivity.this, QuestionListActivity.class);
                intent.putExtra("screenCurrent", nameScreen);
                startActivity(intent);
            }
        });

    }

    // Phương thức cập nhật hiển thị thời gian đếm ngược
    @SuppressLint("SetTextI18n")
    private void updateCountdownText() {
        txtTime.setText(timeLeftInMillis / 1000 + " s");
    }

    private void restartCountdownTimer() {
        timeLeftInMillis = 30000; // Đặt lại thời gian còn lại
        countDownTimer.start(); // Bắt đầu đếm thời gian lại
    }

    private void startTimerForQuestionCompletion() {
        // Khởi tạo Handler để đếm thời gian
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Kiểm tra nếu đã kết thúc câu hỏi thì dừng đếm thời gian
                if (isEndOfQuestions) {

                    handler.removeCallbacksAndMessages(null); // Dừng đếm thời gian

                    // Tính và hiển thị thời gian làm bài
                    long elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
                    long elapsedTimeSeconds = elapsedTimeMillis / 1000;
                    long minutes = elapsedTimeSeconds / 60;
                    long seconds = elapsedTimeSeconds % 60;
                    String formattedTime = String.format("%02d:%02d", minutes, seconds);

                    Status status = statusDB.checkStatus(DB.iduser, idBo, MainActivity.getIDSkill(nameScreen));

                    if (status != null){
                        String[] parts = status.getTimedone().split(":");
                        int timeMinutes = Integer.parseInt(parts[0]);
                        int timeSeconds = Integer.parseInt(parts[1]);

                        if (status.getScore() < score){
                            statusDB.updateStatus(DB.iduser, idBo, MainActivity.getIDSkill(nameScreen), score, formattedTime, FillBlanksActivity.this);
                        } else if (status.getScore() == score && (minutes < timeMinutes || (minutes == timeMinutes && seconds < timeSeconds))) {
                            // Thời gian ngắn hơn
                            statusDB.updateStatus(DB.iduser, idBo, MainActivity.getIDSkill(nameScreen), score, formattedTime, FillBlanksActivity.this);
                        }

                    } else {
                        statusDB.addStatus(DB.iduser, idBo, MainActivity.getIDSkill(nameScreen), score, formattedTime, FillBlanksActivity.this);
                    }

                    Intent intent = new Intent(FillBlanksActivity.this, LessonCompletionActivity.class);
                    intent.putExtra("score", score);
                    intent.putExtra("questiontrue", questionTrue);
                    intent.putExtra("qcount", questionCurrent);
                    intent.putExtra("timedone", formattedTime);
                    intent.putExtra("screenCurrent", nameScreen);
                    startActivity(intent);
                    isEndOfQuestions = false;

                } else {
                    // Nếu chưa kết thúc câu hỏi, tiếp tục đếm thời gian sau 1 giây
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000); // Bắt đầu đếm thời gian sau 1 giây

    }

    public void AnhXa(){
        txtScore = findViewById(R.id.txtScore_FillBlank);
        txtQuestCount = findViewById(R.id.txtQuestCount_FillBlank);
        txtQuestion = findViewById(R.id.txtQuestion_FillBlank);
        txtTime = findViewById(R.id.txtTime_FillBlank);
        edtAnswer = findViewById(R.id.edAnswer_FillBlank);
        btnContinue = findViewById(R.id.btnContinue_FillBlank);
        txtTip = findViewById(R.id.txtTip_FillBlank);
        txtBack = findViewById(R.id.txtBack_FillBlank);
        progressBar = findViewById(R.id.progressBar_FillBlank);
    }

    private ArrayList<FillBlank> getList(){
        ArrayList<FillBlank> list = new ArrayList<>();
        database = Database.initDatabase(FillBlanksActivity.this, DATABASE_NAME);
        Cursor cursor = database.rawQuery("SELECT * FROM DienKhuyet WHERE ID_Bo = ?",new String[]{String.valueOf(idBo)});

        for (int i = 0; i < cursor.getCount(); i++){
            cursor.moveToPosition(i);
            int idcau = cursor.getInt(0);
            int idbo = cursor.getInt(1);
            String noidung = cursor.getString(2);
            String dapan = cursor.getString(3);
            String goiy = cursor.getString(4);

            list.add(new FillBlank(idcau, idbo, noidung, dapan, goiy));
        }
        return list;
    }

    @SuppressLint("SetTextI18n")
    public void showNextQuestion(int pos) {

        // Cập nhật giao diện hiển thị thời gian
        updateCountdownText();
        Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.NORMAL);
        edtAnswer.setTypeface(boldTypeface);
        edtAnswer.setTextSize(20);

        int totalQuestions = fillBlanks.size();

        // Cập nhật giá trị tiến độ của ProgressBar
        int progressValue = ((pos + 1) * 100) / (totalQuestions+1); // Tính toán giá trị tiến độ
        progressBar.setProgress(progressValue);

        if (pos == totalQuestions) {
            btnContinue.setText("Complete");
            isEndOfQuestions = true;
            startTimerForQuestionCompletion();
        } else if(pos < totalQuestions) {
            txtQuestCount.setText("Question: " + (questionCurrent + 1) + "/" + totalQuestions + "");
            FillBlank fillBlank = fillBlanks.get(pos);
            String questcontent = fillBlank.getNoidung();
            answer = fillBlank.getDapan();
            txtTip.setText(fillBlank.getGoiy());
            txtQuestion.setText(questcontent);
        }
    }

    public void showanswer(){
        edtAnswer.setText(answer);
        edtAnswer.setTextColor(Color.GREEN);
        edtAnswer.setTextSize(24);
        // Trước khi set kiểu chữ in đậm, bạn cần khai báo một kiểu chữ in đậm
        Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD);
        edtAnswer.setTypeface(boldTypeface);
        edtAnswer.clearFocus();
    }

    public void checkAnswer()
    {
        btnContinue.setEnabled(false);
        if(answer.equals(edtAnswer.getText().toString()))

        {
            showCustomDialog(R.layout.dialog_bottomlayout_correct, "Great Job!", "Congratulations! Your answer is correct.");
            edtAnswer.setTextColor(Color.GREEN);
            questionTrue++;
            score+=5;
            txtScore.setText("Score: "+score);
            edtAnswer.clearFocus();
        }
        else{
            showCustomDialog(R.layout.dialog_bottomlayout_incorrect, "Incorrect", "Sorry, your answer is incorrect.");
            edtAnswer.setTextColor(Color.RED);
            edtAnswer.startAnimation(shakeError());
            edtAnswer.clearFocus();
        }

    }

    public TranslateAnimation shakeError() {
        TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
        shake.setDuration(500);
        shake.setInterpolator(new CycleInterpolator(7));
        return shake;
    }

    private void showCustomDialog(int layoutResID, String title, String message) {
        // Inflate the layout for the Bottom Sheet
        View bottomSheetView = getLayoutInflater().inflate(layoutResID, null);
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Find and set the views in the Bottom Sheet
        TextView txtTitle = bottomSheetView.findViewById(R.id.txtTitle);
        TextView txtMessage = bottomSheetView.findViewById(R.id.txtMessage1);
        txtTitle.setText(title);
        txtMessage.setText(message);

        // Initialize the CoordinatorLayout
        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        // Show the Bottom Sheet
        bottomSheetDialog.setCancelable(false);
        bottomSheetDialog.show();

        // Set btnContinue above the Bottom Sheet
        btnContinue.bringToFront();

        // Sử dụng Handler để đóng Bottom Sheet sau 1,3 giây
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                bottomSheetDialog.dismiss();
            }
        }, DISPLAY_DURATION); // time close

    }

    private void changeBtnColor() {
        // Khai báo một TextWatcher để theo dõi sự thay đổi trên các EditText
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No need to implement anything here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String answer = edtAnswer.getText().toString().trim();

                // Nếu EditText đều đã được nhập, thay đổi màu nền và màu chữ của button
                if (!answer.isEmpty()) {
                    // Lấy màu từ tài nguyên màu trong tệp colors.xml
                    int rectangle6Color = ContextCompat.getColor(FillBlanksActivity.this, R.color.rectangle_6_color);
                    btnContinue.setBackgroundTintList(ColorStateList.valueOf(rectangle6Color));
                    btnContinue.setTextColor(Color.WHITE);
                } else{
                    btnContinue.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E6E4EA")));
                    btnContinue.setTextColor(Color.parseColor("#BAB4B4"));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No need to implement anything here
            }
        };

        // Áp dụng TextWatcher cho từng EditText
        edtAnswer.addTextChangedListener(textWatcher);
    }
}