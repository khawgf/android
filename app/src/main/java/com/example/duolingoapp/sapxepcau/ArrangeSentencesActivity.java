package com.example.duolingoapp.sapxepcau;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import java.util.Random;

public class ArrangeSentencesActivity extends AppCompatActivity {

    final  String DATABASE_NAME = "HocNgonNgu.db";
    SQLiteDatabase database;
    DatabaseAccess DB;
    TextView txtScore,txtQuestCount, txtTime, txtBack;
    EditText edAnswer;
    Button btnContinue;
    int questionCurrent = 0;
    int questionTrue = 0;
    String answer, nameScreen;
    int score=0;
    int idBo;
    private ProgressBar progressBar;
    private final int DISPLAY_DURATION = 1400; //Thời gian đóng bottom sheet
    private BottomSheetDialog bottomSheetDialog;
    private CoordinatorLayout coordinatorLayout;

    // Khai báo biến đếm thời gian
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis=50000; // Thời gian 30s của mỗi câu hỏi

    private long startTimeMillis; // Thời gian bắt đầu làm bài
    private boolean isEndOfQuestions = false; // Biến cờ để đánh dấu khi kết thúc câu hỏi
    StatusDB statusDB;
    ArrayList<ArrangeSentences> arrangeSentences;
    private String[] keys = {"part1", "part2", "part3", "part4"};
    LinearLayout part1, part2, part3, part4;

    private int presCounter = 0;
    private int maxPresCounter = 4;
    private ArrayList<TextView> textViews = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_arrange_sentences);
        DB = DatabaseAccess.getInstance(getApplicationContext());
        statusDB = StatusDB.getInstance(getApplicationContext());

        AnhXa();
        progressBar.setProgress(0); // Khởi tạo giá trị tiến độ ban đầu là 0%
        changeBtnColor();
        Intent intent=getIntent();
        idBo=intent.getIntExtra("idBo",0);
        nameScreen = getIntent().getStringExtra("screenCurrent");
        arrangeSentences = getList();

        //Thời gian để xem đáp án, sau đó hiện câu hỏi tiếp theo
        CountDownTimer nextScreenDisplayTime =new CountDownTimer(5000,2000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                questionCurrent++;
                edAnswer.setTextColor(Color.BLACK);
                btnContinue.setEnabled(true);
                edAnswer.setText("");
                answer="";
                presCounter = 0;
                maxPresCounter = 4;
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
                Intent intent = new Intent(ArrangeSentencesActivity.this, QuestionListActivity.class);
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
        timeLeftInMillis = 50000; // Đặt lại thời gian còn lại
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
                            statusDB.updateStatus(DB.iduser, idBo, MainActivity.getIDSkill(nameScreen), score, formattedTime, ArrangeSentencesActivity.this);
                        } else if (status.getScore() == score && (minutes < timeMinutes || (minutes == timeMinutes && seconds < timeSeconds))) {
                            // Thời gian ngắn hơn
                            statusDB.updateStatus(DB.iduser, idBo, MainActivity.getIDSkill(nameScreen), score, formattedTime, ArrangeSentencesActivity.this);
                        }

                    } else {
                        statusDB.addStatus(DB.iduser, idBo, MainActivity.getIDSkill(nameScreen), score, formattedTime, ArrangeSentencesActivity.this);
                    }

                    Intent intent = new Intent(ArrangeSentencesActivity.this, LessonCompletionActivity.class);
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

    @SuppressLint("SetTextI18n")
    public void showNextQuestion(int pos) {
        textViews.clear();
        part1.removeAllViews();
        part2.removeAllViews();
        part3.removeAllViews();
        part4.removeAllViews();

        // Cập nhật giao diện hiển thị thời gian
        updateCountdownText();
        Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.NORMAL);
        edAnswer.setTypeface(boldTypeface);
        edAnswer.setTextSize(20);

        int totalQuestions = arrangeSentences.size();

        // Cập nhật giá trị tiến độ của ProgressBar
        int progressValue = ((pos + 1) * 100) / (totalQuestions+1); // Tính toán giá trị tiến độ
        progressBar.setProgress(progressValue);

        if (pos == totalQuestions) {
            btnContinue.setText("Complete");
            isEndOfQuestions = true;
            startTimerForQuestionCompletion();

        } else if(pos < totalQuestions) {
            txtQuestCount.setText("Question: " + (questionCurrent + 1) + "/" + totalQuestions + "");
            ArrangeSentences as = arrangeSentences.get(pos);
            answer = as.getDapan();
            keys[0] = as.getPart1();
            keys[1] = as.getPart2();
            keys[2] = as.getPart3();
            keys[3] = as.getPart4();

            keys = randomArray(keys);

            addView(part1, keys[0], edAnswer);
            addView(part2, keys[1], edAnswer);
            addView(part3, keys[2], edAnswer);
            addView(part4, keys[3], edAnswer);
        }
    }

    private void addView(LinearLayout viewParent, final String text, final EditText editText) {
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        linearLayoutParams.rightMargin = 5;

        final TextView textView = new TextView(this);

        textView.setLayoutParams(linearLayoutParams);
        textView.setBackground(this.getResources().getDrawable(R.drawable.bgbtn));
        textView.setTextColor(this.getResources().getColor(R.color.black));
        textView.setGravity(Gravity.CENTER);
        textView.setText(text);
        textView.setClickable(true);
        textView.setFocusable(true);
        textView.setTextSize(20);
        textView.setBackgroundTintList(null);

        textView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint({"SetTextI18n", "ResourceAsColor"})
            @Override
            public void onClick(View v) {
                String editTextContent = editText.getText().toString();
                if (editTextContent.contains(text)) {
                    // Nếu text đã tồn tại trên EditText, xóa nó ra khỏi EditText
                    editTextContent = editTextContent.replace(text, "").trim();
                    editText.setText(editTextContent);
                    textView.setBackgroundTintList(null);
                    presCounter--;
                } else {

                    if (presCounter < maxPresCounter) {
                        if (presCounter == 0)
                            editText.setText("");

                        int chuoi = 0;

                        if (editText.getText().toString().equals("") || chuoi == 3) {
                            editText.setText(editText.getText().toString() + text);
                            chuoi++;
                        } else {
                            editText.setText(editText.getText().toString() + " " + text);
                            chuoi++;
                        }

                        presCounter++;
                        textView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E6E4EA")));

                    }
                }
            }
        });
        viewParent.addView(textView);
        textViews.add(textView); // Thêm textView vào mảng hoặc danh sách
    }


    private String[] randomArray(String[] ar) {
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            String a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
        return ar;
    }

    public void showanswer(){
        edAnswer.setText(answer);
        edAnswer.setTextColor(Color.GREEN);
        edAnswer.setTextSize(24);
        // Trước khi set kiểu chữ in đậm, bạn cần khai báo một kiểu chữ in đậm
        Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD);
        edAnswer.setTypeface(boldTypeface);
        edAnswer.clearFocus();
    }

    public void checkAnswer()
    {
        // Vô hiệu hóa clickable của tất cả các textView
        for (TextView textView : textViews) {
            textView.setClickable(false);
        }

        btnContinue.setEnabled(false);
        if(answer.equals(edAnswer.getText().toString()))
        {
            showCustomDialog(R.layout.dialog_bottomlayout_correct, "Great Job!", "Congratulations! Your answer is correct.");
            edAnswer.setTextColor(Color.GREEN);
            questionTrue++;
            score+=5;
            txtScore.setText("Score: "+score);
            edAnswer.clearFocus();
        }
        else{
            showCustomDialog(R.layout.dialog_bottomlayout_incorrect, "Incorrect", "Sorry, your answer is incorrect.");
            edAnswer.setTextColor(Color.RED);
            edAnswer.startAnimation(shakeError());
            edAnswer.clearFocus();
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

    private ArrayList<ArrangeSentences> getList(){
        ArrayList<ArrangeSentences> list = new ArrayList<>();
        database = Database.initDatabase(ArrangeSentencesActivity.this, DATABASE_NAME);
        Cursor cursor = database.rawQuery("SELECT * FROM SapXepCau WHERE IDBo = ?",new String[]{String.valueOf(idBo)});

        for (int i = 0; i < cursor.getCount(); i++){
            cursor.moveToPosition(i);
            int idcau = cursor.getInt(0);
            int idbo = cursor.getInt(1);
            String dapan = cursor.getString(2);
            String part1 = cursor.getString(3);
            String part2 = cursor.getString(4);
            String part3 = cursor.getString(5);
            String part4 = cursor.getString(6);

            list.add(new ArrangeSentences(idcau, idbo, dapan, part1, part2, part3, part4));
        }
        return list;
    }

    public void AnhXa(){
        txtScore = findViewById(R.id.txtScore_ArrangeSentences);
        txtQuestCount = findViewById(R.id.txtQuestCount_ArrangeSentences);
        txtTime = findViewById(R.id.txtTime_ArrangeSentences);
        btnContinue = findViewById(R.id.btnContinue_ArrangeSentences);
        edAnswer = findViewById(R.id.edAnswer_ArrangeSentences);
        txtBack = findViewById(R.id.txtBack_ArrangeSentences);
        progressBar = findViewById(R.id.progressBar_ArrangeSentences);
        part1 = findViewById(R.id.layoutPart1);
        part2 = findViewById(R.id.layoutPart2);
        part3 = findViewById(R.id.layoutPart3);
        part4 = findViewById(R.id.layoutPart4);
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
                String answer = edAnswer.getText().toString().trim();

                // Nếu EditText đều đã được nhập, thay đổi màu nền và màu chữ của button
                if (!answer.isEmpty()) {
                    // Lấy màu từ tài nguyên màu trong tệp colors.xml
                    int rectangle6Color = ContextCompat.getColor(ArrangeSentencesActivity.this, R.color.rectangle_6_color);
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
        edAnswer.addTextChangedListener(textWatcher);
    }
}