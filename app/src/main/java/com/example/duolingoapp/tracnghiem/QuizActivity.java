package com.example.duolingoapp.tracnghiem;

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
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.duolingoapp.LessonCompletionActivity;
import com.example.duolingoapp.MainActivity;
import com.example.duolingoapp.R;
import com.example.duolingoapp.bocauhoi.QuestionListActivity;
import com.example.duolingoapp.taikhoan.DatabaseAccess;
import com.example.duolingoapp.trangthai.Status;
import com.example.duolingoapp.trangthai.StatusDB;
import com.example.duolingoapp.ui.home.Database;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

public class QuizActivity extends AppCompatActivity {

    final  String DATABASE_NAME = "HocNgonNgu.db";
    SQLiteDatabase database;
    DatabaseAccess DB;
    TextView txtScore, txtQuestion, txtQuestCount, txtTime, txtBack;
    RadioButton rdOp1, rdOp2, rdOp3, rdOp4;
    RadioGroup rdgChoices;
    Button btnContinue;
    private ProgressBar progressBar;
    int idBo;
    int questionTrue = 0;
    int questionCurrent = 0;
    String nameScreen;
    int score=0,  answer=0;
    ArrayList<Quiz> quizList;
    private final int DISPLAY_DURATION = 1400; //Thời gian đóng bottom sheet
    private BottomSheetDialog bottomSheetDialog;
    private CoordinatorLayout coordinatorLayout;

    // Khai báo biến đếm thời gian
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis=30000; // Thời gian 30s của mỗi câu hỏi

    private long startTimeMillis; // Thời gian bắt đầu làm bài
    private boolean isEndOfQuestions = false; // Biến cờ để đánh dấu khi kết thúc câu hỏi
    StatusDB statusDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_quiz);
        DB = DatabaseAccess.getInstance(getApplicationContext());
        statusDB = StatusDB.getInstance(getApplicationContext());

        AnhXa();
        progressBar.setProgress(0); // Khởi tạo giá trị tiến độ ban đầu là 0%
        Intent intent=getIntent();
        idBo=intent.getIntExtra("idBo",0);
        nameScreen = getIntent().getStringExtra("screenCurrent");

        quizList = getList();

        restoreRadioGroupListener();

        //Thời gian để xem đáp án, sau đó hiện câu hỏi tiếp theo
        CountDownTimer nextScreenDisplayTime =new CountDownTimer(3000,2000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                questionCurrent++;
                btnContinue.setEnabled(true);
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

                Intent intent = new Intent(QuizActivity.this, QuestionListActivity.class);
                intent.putExtra("screenCurrent", nameScreen);
                startActivity(intent);
            }
        });
    }

    // Phương thức để khôi phục sự kiện lắng nghe của RadioGroup
    private void restoreRadioGroupListener() {
        //int rectangle6Color = ContextCompat.getColor(QuizActivity.this, R.color.rectangle_6_color);

        rdgChoices.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Kiểm tra xem RadioButton nào được chọn
                RadioButton checkedRadioButton = findViewById(checkedId);
                if (checkedRadioButton != null) {
                    int rectangle6Color = ContextCompat.getColor(QuizActivity.this, R.color.rectangle_6_color);

                    // Đổi màu cho RadioButton được chọn
//                    checkedRadioButton.setBackgroundTintList(ColorStateList.valueOf(rectangle6Color));
//                    checkedRadioButton.setTextColor(Color.WHITE);

                    // Đổi màu cho btnContinue
                    btnContinue.setBackgroundTintList(ColorStateList.valueOf(rectangle6Color));
                    btnContinue.setTextColor(Color.WHITE);

                } else {
                    btnContinue.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E6E4EA")));
                    btnContinue.setTextColor(Color.parseColor("#BAB4B4"));
                }

//                // Đổi màu cho các RadioButton không được chọn
//                for (int i = 0; i < group.getChildCount(); i++) {
//                    View v = group.getChildAt(i);
//                    if (v instanceof RadioButton && v.getId() != checkedId) {
//                        ((RadioButton) v).setBackgroundTintList(null);
//                        ((RadioButton) v).setTextColor(Color.BLACK);
//                    }
//                }
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
                            statusDB.updateStatus(DB.iduser, idBo, MainActivity.getIDSkill(nameScreen), score, formattedTime, QuizActivity.this);
                        } else if (status.getScore() == score && (minutes < timeMinutes || (minutes == timeMinutes && seconds < timeSeconds))) {
                            // Thời gian ngắn hơn
                            statusDB.updateStatus(DB.iduser, idBo, MainActivity.getIDSkill(nameScreen), score, formattedTime, QuizActivity.this);
                        }

                    } else {
                        statusDB.addStatus(DB.iduser, idBo, MainActivity.getIDSkill(nameScreen), score, formattedTime, QuizActivity.this);
                    }

                    Intent intent = new Intent(QuizActivity.this, LessonCompletionActivity.class);
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


    private ArrayList<Quiz> getList(){
        ArrayList<Quiz> list = new ArrayList<>();

        database = Database.initDatabase(QuizActivity.this, DATABASE_NAME);
        Cursor cursor = database.rawQuery("SELECT * FROM TracNghiem WHERE ID_Bo = ?",new String[]{String.valueOf(idBo)});

        for (int i = 0; i < cursor.getCount(); i++){
            cursor.moveToPosition(i);
            int idcau = cursor.getInt(0);
            int idbo = cursor.getInt(1);
            String noidung = cursor.getString(2);
            String dapanA = cursor.getString(3);
            String dapanB = cursor.getString(4);
            String dapanC = cursor.getString(5);
            String dapanD = cursor.getString(6);
            String dapanTrue = cursor.getString(7);

            list.add(new Quiz(idcau, idbo, noidung, dapanA, dapanB, dapanC, dapanD, dapanTrue));
        }
        return list;
    }

    @SuppressLint("SetTextI18n")
    public void showNextQuestion(int pos) {

        // Cập nhật giao diện hiển thị thời gian
        updateCountdownText();

        // Xóa chọn radio button
        rdgChoices.clearCheck();

        restoreRadioGroupListener();


        // Đặt lại màu của các RadioButton về màu ban đầu
        rdOp1.setBackground(this.getResources().getDrawable(R.drawable.bgbtn));
        rdOp2.setBackground(this.getResources().getDrawable(R.drawable.bgbtn));
        rdOp3.setBackground(this.getResources().getDrawable(R.drawable.bgbtn));
        rdOp4.setBackground(this.getResources().getDrawable(R.drawable.bgbtn));

        rdOp1.setTextColor(Color.BLACK);
        rdOp2.setTextColor(Color.BLACK);
        rdOp3.setTextColor(Color.BLACK);
        rdOp4.setTextColor(Color.BLACK);


        btnContinue.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E6E4EA")));
        btnContinue.setTextColor(Color.parseColor("#BAB4B4"));


        // Cập nhật giá trị tiến độ của ProgressBar
        int totalQuestions = quizList.size();
        int progressValue = ((pos + 1) * 100) / (totalQuestions+1); // Tính toán giá trị tiến độ
        progressBar.setProgress(progressValue);


        if (pos == totalQuestions) {
            btnContinue.setText("Complete");
            isEndOfQuestions = true;
            startTimerForQuestionCompletion();

        } else if(pos < totalQuestions) {
            txtQuestCount.setText("Question: " + (questionCurrent + 1) + "/" + totalQuestions + "");
            Quiz quiz = quizList.get(pos);
            answer = Integer.parseInt(quiz.getDapanTrue());
            txtQuestion.setText(quiz.getNoidung());
            rdOp1.setText(quiz.getDapanA());
            rdOp2.setText(quiz.getDapanB());
            rdOp3.setText(quiz.getDapanC());
            rdOp4.setText(quiz.getDapanD());
        }
    }

    public void showanswer(){
        // Loại bỏ sự kiện lắng nghe của RadioButton
        rdgChoices.setOnCheckedChangeListener(null);


        if(1==answer) {
            rdOp1.setBackground(this.getResources().getDrawable(R.drawable.background_green));
            rdOp1.setTextColor(Color.WHITE);
        } else if(2==answer) {
            rdOp2.setBackground(this.getResources().getDrawable(R.drawable.background_green));
            rdOp2.setTextColor(Color.WHITE);
        } else if(3==answer) {
            rdOp3.setBackground(this.getResources().getDrawable(R.drawable.background_green));
            rdOp3.setTextColor(Color.WHITE);
        }else if(4==answer) {
            rdOp4.setBackground(this.getResources().getDrawable(R.drawable.background_green));
            rdOp4.setTextColor(Color.WHITE);
        }

        if(rdOp1.isChecked() && answer!=1){
            rdOp1.setBackground(this.getResources().getDrawable(R.drawable.background_red));
            rdOp1.setTextColor(Color.WHITE);
        } else if(rdOp2.isChecked() && answer!=2){
            rdOp2.setBackground(this.getResources().getDrawable(R.drawable.background_red));
            rdOp2.setTextColor(Color.WHITE);
        } else if(rdOp3.isChecked() && answer!=3){
            rdOp3.setBackground(this.getResources().getDrawable(R.drawable.background_red));
            rdOp3.setTextColor(Color.WHITE);
        }else if(rdOp4.isChecked() && answer!=4){
            rdOp4.setBackground(this.getResources().getDrawable(R.drawable.background_red));
            rdOp4.setTextColor(Color.WHITE);
        }
    }

    public void checkAnswer()
    {
        btnContinue.setEnabled(false);
        if(rdOp1.isChecked()){
            if(1==answer) {
                showCustomDialog(R.layout.dialog_bottomlayout_correct, "Great Job!", "Congratulations! Your answer is correct.");
                score+=5;
                questionTrue++;

            } else {
                showCustomDialog(R.layout.dialog_bottomlayout_incorrect, "Incorrect", "Sorry, your answer is incorrect.");
                rdOp1.startAnimation(shakeError());
            }
        }
        if(rdOp2.isChecked()){
            if(2==answer) {
                showCustomDialog(R.layout.dialog_bottomlayout_correct, "Great Job!", "Congratulations! Your answer is correct.");
                score+=5;
                questionTrue++;
            } else {
                showCustomDialog(R.layout.dialog_bottomlayout_incorrect, "Incorrect", "Sorry, your answer is incorrect.");
                rdOp2.startAnimation(shakeError());
            }
        }
        if(rdOp3.isChecked()){
            if(3==answer) {
                showCustomDialog(R.layout.dialog_bottomlayout_correct, "Great Job!", "Congratulations! Your answer is correct.");
                score+=5;
                questionTrue++;

            } else {
                showCustomDialog(R.layout.dialog_bottomlayout_incorrect, "Incorrect", "Sorry, your answer is incorrect.");
                rdOp3.startAnimation(shakeError());
            }
        }
        if(rdOp4.isChecked()){
            if(4==answer) {
                showCustomDialog(R.layout.dialog_bottomlayout_correct, "Great Job!", "Congratulations! Your answer is correct.");
                score+=5;
                questionTrue++;
            }  else {
                showCustomDialog(R.layout.dialog_bottomlayout_incorrect, "Incorrect", "Sorry, your answer is incorrect.");
                rdOp4.startAnimation(shakeError());
            }
        }

        txtScore.setText("Score: "+score);

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


    public void AnhXa(){
        txtQuestion = findViewById(R.id.txtQuestion_Quiz);
        txtQuestCount = findViewById(R.id.txtQuestCount_Quiz);
        txtTime = findViewById(R.id.txtTime_Quiz);
        txtBack = findViewById(R.id.txtBack_Quiz);
        btnContinue = findViewById(R.id.btnContinue_Quiz);
        progressBar = findViewById(R.id.progressBar_Quiz);
        txtScore = findViewById(R.id.txtScore_Quiz);
        rdgChoices = findViewById(R.id.radiochoices_Quiz);
        rdOp1 = findViewById(R.id.rdOption1_Quiz);
        rdOp2 = findViewById(R.id.rdOption2_Quiz);
        rdOp3 = findViewById(R.id.rdOption3_Quiz);
        rdOp4 = findViewById(R.id.rdOption4_Quiz);
    }




}