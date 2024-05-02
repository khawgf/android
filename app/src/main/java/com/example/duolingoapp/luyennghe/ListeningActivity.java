package com.example.duolingoapp.luyennghe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
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

import java.io.IOException;
import java.util.ArrayList;

public class ListeningActivity extends AppCompatActivity {
    final  String DATABASE_NAME = "HocNgonNgu.db";
    SQLiteDatabase database;
    DatabaseAccess DB;
    TextView txtScore, txtQuestCount, txtTime, txtBack;
    RadioButton rdOp1, rdOp2, rdOp3, rdOp4;
    RadioGroup rdgChoices;
    Button btnContinue, btnLoudspeaker;
    ImageView img_Listening;
    private ProgressBar progressBar;
    int idBo;
    int questionTrue = 0;
    int questionCurrent = 0;
    String nameScreen;
    int score=0, answer=0;
    ArrayList<Listening> listenings;
    private final int DISPLAY_DURATION = 1400; //Thời gian đóng bottom sheet
    private BottomSheetDialog bottomSheetDialog;
    private CoordinatorLayout coordinatorLayout;

    // Khai báo biến đếm thời gian
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis=30000; // Thời gian 30s của mỗi câu hỏi

    private long startTimeMillis; // Thời gian bắt đầu làm bài
    private boolean isEndOfQuestions = false; // Biến cờ để đánh dấu khi kết thúc câu hỏi
    StatusDB statusDB;
    String audioFileName;
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private Handler handler = new Handler();
    int audioDuration; // Lấy thời lượng của audio

    // Khai báo biến boolean để theo dõi trạng thái của trình phát âm thanh
    private boolean isAudioPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_listening);

        DB = DatabaseAccess.getInstance(getApplicationContext());
        statusDB = StatusDB.getInstance(getApplicationContext());

        AnhXa();
        progressBar.setProgress(0); // Khởi tạo giá trị tiến độ ban đầu là 0%
        Intent intent=getIntent();
        idBo=intent.getIntExtra("idBo",0);
        nameScreen = getIntent().getStringExtra("screenCurrent");
        listenings = getList();

        //Thời gian để xem đáp án, sau đó hiện câu hỏi tiếp theo
        CountDownTimer nextScreenDisplayTime =new CountDownTimer(3000,2000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                questionCurrent++;
                btnContinue.setEnabled(true);
                answer=0;
                isAudioPlaying = false;// Đặt lại trạng thái của trình phát âm thanh khi audio kết thúc
                showNextQuestion(questionCurrent);
                countDownTimer.cancel();


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
                doStop();
                timeLeftInMillis = 0;
                updateCountdownText();
                checkAnswer();
                showanswer();

                nextScreenDisplayTime.start();
            }
        };


        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doStop();

                checkAnswer();
                showanswer();

                nextScreenDisplayTime.start();
            }
        });
        txtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doStop();
                finish();
                Intent intent = new Intent(ListeningActivity.this, QuestionListActivity.class);
                intent.putExtra("screenCurrent", nameScreen);
                startActivity(intent);
            }
        });

        // Gắn sự kiện cho RadioGroup để lắng nghe sự thay đổi trong RadioButton được chọn
        restoreRadioGroupListener();

        // Bắt sự kiện khi nhấn nút phát
        btnLoudspeaker.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                // Kiểm tra trạng thái của trình phát âm thanh
                if (!isAudioPlaying) {
                    // Phát âm thanh từ thư mục "audio" trong "assets"
                    playAudioFromAssets(audioFileName);
                    btnLoudspeaker.setBackgroundTintList(ColorStateList.valueOf(R.color.textColorHint));
                    btnLoudspeaker.setEnabled(false);
                    // Cập nhật trạng thái là đang phát âm thanh
                    isAudioPlaying = true;
                }
            }
        });


        // Cài đặt sự kiện cho seekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Nếu người dùng kéo thanh chạy, di chuyển đến thời gian tương ứng trong phát lại
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Không cần xử lý trong trường hợp này
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Không cần xử lý trong trường hợp này
            }
        });
    }

    // Phương thức để khôi phục sự kiện lắng nghe của RadioGroup
    private void restoreRadioGroupListener() {
        int rectangle6Color = ContextCompat.getColor(ListeningActivity.this, R.color.rectangle_6_color);

        rdOp1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Xử lý khi rdOption1 được chọn
                    rdOp1.setBackgroundTintList(ColorStateList.valueOf(rectangle6Color));
                    rdOp1.setTextColor(Color.WHITE);
                    // Đổi màu cho btnContinue
                    btnContinue.setBackgroundTintList(ColorStateList.valueOf(rectangle6Color));
                    btnContinue.setTextColor(Color.WHITE);

                    // Xóa chọn cho các radio button còn lại
                    rdOp2.setChecked(false);
                    rdOp3.setChecked(false);
                    rdOp4.setChecked(false);
                } else {
                    rdOp1.setBackgroundTintList(null);
                    rdOp1.setTextColor(Color.BLACK);
                }
            }
        });

        rdOp2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Xử lý khi rdOption1 được chọn
                    rdOp2.setBackgroundTintList(ColorStateList.valueOf(rectangle6Color));
                    rdOp2.setTextColor(Color.WHITE);
                    // Đổi màu cho btnContinue
                    btnContinue.setBackgroundTintList(ColorStateList.valueOf(rectangle6Color));
                    btnContinue.setTextColor(Color.WHITE);

                    // Xóa chọn cho các radio button còn lại
                    rdOp1.setChecked(false);
                    rdOp3.setChecked(false);
                    rdOp4.setChecked(false);
                } else {
                    rdOp2.setBackgroundTintList(null);
                    rdOp2.setTextColor(Color.BLACK);
                }
            }
        });

        rdOp3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Xử lý khi rdOption1 được chọn
                    rdOp3.setBackgroundTintList(ColorStateList.valueOf(rectangle6Color));
                    rdOp3.setTextColor(Color.WHITE);
                    // Đổi màu cho btnContinue
                    btnContinue.setBackgroundTintList(ColorStateList.valueOf(rectangle6Color));
                    btnContinue.setTextColor(Color.WHITE);

                    // Xóa chọn cho các radio button còn lại
                    rdOp1.setChecked(false);
                    rdOp2.setChecked(false);
                    rdOp4.setChecked(false);
                } else {
                    rdOp3.setBackgroundTintList(null);
                    rdOp3.setTextColor(Color.BLACK);
                }
            }
        });

        rdOp4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    // Xử lý khi rdOption1 được chọn
                    rdOp4.setBackgroundTintList(ColorStateList.valueOf(rectangle6Color));
                    rdOp4.setTextColor(Color.WHITE);
                    // Đổi màu cho btnContinue
                    btnContinue.setBackgroundTintList(ColorStateList.valueOf(rectangle6Color));
                    btnContinue.setTextColor(Color.WHITE);

                    // Xóa chọn cho các radio button còn lại
                    rdOp1.setChecked(false);
                    rdOp2.setChecked(false);
                    rdOp3.setChecked(false);
                } else {
                    rdOp4.setBackgroundTintList(null);
                    rdOp4.setTextColor(Color.BLACK);
                }
            }
        });

    }

    // Phương thức để phát âm thanh từ thư mục "audio" trong "assets"
    private void playAudioFromAssets(String filename) {
        try {
            AssetFileDescriptor afd = getAssets().openFd("audio/" + filename);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mediaPlayer.prepare();
            mediaPlayer.start();

            // Lấy thời lượng của audio
            audioDuration = mediaPlayer.getDuration();

            // Thiết lập giá trị tối đa cho SeekBar
            seekBar.setMax(audioDuration);

            // Cập nhật thanh chạy mỗi 100ms
            updateSeekBar();

            // Xử lý sự kiện khi audio kết thúc phát
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // Đặt lại trạng thái của thanh chạy khi audio kết thúc
                    //seekBar.setProgress(0);

                    restartCountdownTimer(); //bắt đầu đếm ngược 30s
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Phương thức để cập nhật thanh chạy
    private void updateSeekBar() {
        if (mediaPlayer != null) {
            // Tính toán tỷ lệ vị trí hiện tại của audio so với thời lượng của nó
            int currentPosition = mediaPlayer.getCurrentPosition();
            int progress = currentPosition * 100 / audioDuration;

            // Cập nhật vị trí của SeekBar dựa trên tỷ lệ tính toán
            seekBar.setProgress(currentPosition);

            // Lập lịch cho việc cập nhật tiếp theo sau 100ms
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateSeekBar();
                }
            }, 100);
        }
    }

    private void doStart( )  {
        if(this.mediaPlayer.isPlaying()) {
            //this.mediaPlayer.stop();
            this.mediaPlayer.pause();
            this.mediaPlayer.reset();
        }
        else {this.mediaPlayer.start();}
    }

    private void doStop()  {
        if (mediaPlayer!=null){
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

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
                            statusDB.updateStatus(DB.iduser, idBo, MainActivity.getIDSkill(nameScreen), score, formattedTime, ListeningActivity.this);
                        } else if (status.getScore() == score && (minutes < timeMinutes || (minutes == timeMinutes && seconds < timeSeconds))) {
                            // Thời gian ngắn hơn
                            statusDB.updateStatus(DB.iduser, idBo, MainActivity.getIDSkill(nameScreen), score, formattedTime, ListeningActivity.this);
                        }

                    } else {
                        statusDB.addStatus(DB.iduser, idBo, MainActivity.getIDSkill(nameScreen), score, formattedTime, ListeningActivity.this);
                    }

                    Intent intent = new Intent(ListeningActivity.this, LessonCompletionActivity.class);
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

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    public void showNextQuestion(int pos) {

        // Cập nhật giao diện hiển thị thời gian
        timeLeftInMillis = 30000;
        updateCountdownText();

        // Xóa chọn riêng từng radio button
        rdOp1.setChecked(false);
        rdOp2.setChecked(false);
        rdOp3.setChecked(false);
        rdOp4.setChecked(false);

        restoreRadioGroupListener(); // Khôi phục sự kiện lắng nghe của RadioGroup

        // Đặt lại màu của các RadioButton về màu ban đầu
        rdOp1.setBackground(this.getResources().getDrawable(R.drawable.custom_radius_white));
        rdOp2.setBackground(this.getResources().getDrawable(R.drawable.custom_radius_white));
        rdOp3.setBackground(this.getResources().getDrawable(R.drawable.custom_radius_white));
        rdOp4.setBackground(this.getResources().getDrawable(R.drawable.custom_radius_white));

        rdOp1.setBackgroundTintList(null);
        rdOp1.setTextColor(Color.BLACK);
        rdOp2.setBackgroundTintList(null);
        rdOp2.setTextColor(Color.BLACK);
        rdOp3.setBackgroundTintList(null);
        rdOp3.setTextColor(Color.BLACK);
        rdOp4.setBackgroundTintList(null);
        rdOp4.setTextColor(Color.BLACK);

        btnContinue.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E6E4EA")));
        btnContinue.setTextColor(Color.parseColor("#BAB4B4"));

        btnLoudspeaker.setBackgroundTintList(null);
        btnLoudspeaker.setEnabled(true);


        // Cập nhật giá trị tiến độ của ProgressBar
        int totalQuestions = listenings.size();
        int progressValue = ((pos + 1) * 100) / (totalQuestions+1); // Tính toán giá trị tiến độ
        progressBar.setProgress(progressValue);


        if (pos == totalQuestions) {
            btnContinue.setText("Complete");
            isEndOfQuestions = true;
            startTimerForQuestionCompletion();
        } else if(pos < totalQuestions) {
            txtQuestCount.setText("Question: " + (questionCurrent + 1) + "/" + totalQuestions + "");
            Listening voc = listenings.get(pos);
            answer = Integer.parseInt(voc.getDapanTrue());
            rdOp1.setText(voc.getDapanA());
            rdOp2.setText(voc.getDapanB());
            rdOp3.setText(voc.getDapanC());
            rdOp4.setText(voc.getDapanD());
            Bitmap img= BitmapFactory.decodeByteArray(voc.getHinhanh(),0,voc.getHinhanh().length);
            img_Listening.setImageBitmap(img);
            audioFileName = voc.getAudio();
        }
    }

    @SuppressLint({"UseCompatLoadingForDrawables"})
    //@SuppressLint("ResourceType")
    public void showanswer(){
        // Loại bỏ sự kiện lắng nghe của RadioButton
        rdOp1.setOnCheckedChangeListener(null);
        rdOp2.setOnCheckedChangeListener(null);
        rdOp3.setOnCheckedChangeListener(null);
        rdOp4.setOnCheckedChangeListener(null);


        rdOp1.setBackgroundTintList(null);
        rdOp2.setBackgroundTintList(null);
        rdOp3.setBackgroundTintList(null);
        rdOp4.setBackgroundTintList(null);

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

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
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

    private ArrayList<Listening> getList(){
        ArrayList<Listening> list = new ArrayList<>();

        database = Database.initDatabase(ListeningActivity.this, DATABASE_NAME);
        Cursor cursor = database.rawQuery("SELECT * FROM LuyenNghe WHERE ID_Bo = ?",new String[]{String.valueOf(idBo)});

        for (int i = 0; i < cursor.getCount(); i++){
            cursor.moveToPosition(i);
            int idbai = cursor.getInt(0);
            int idbo = cursor.getInt(1);
            String dapanA = cursor.getString(2);
            String dapanB = cursor.getString(3);
            String dapanC = cursor.getString(4);
            String dapanD = cursor.getString(5);
            String dapanTrue = cursor.getString(6);
            byte[] anh = cursor.getBlob(7);
            String audio = cursor.getString(8);

            list.add(new Listening(idbai, idbo, dapanA, dapanB, dapanC, dapanD, dapanTrue, anh, audio));
        }
        return list;
    }

    public void AnhXa(){
        txtQuestCount = findViewById(R.id.txtQuestCount_Lis);
        txtTime = findViewById(R.id.txtTime_Lis);
        txtBack = findViewById(R.id.txtBack_Lis);
        btnContinue = findViewById(R.id.btnContinue_Lis);
        img_Listening = findViewById(R.id.img_Lis);
        txtBack = findViewById(R.id.txtBack_Lis);
        progressBar = findViewById(R.id.progressBar_Lis);
        txtScore = findViewById(R.id.txtScore_Lis);
        rdOp1 = findViewById(R.id.rdOption1_Lis);
        rdOp2 = findViewById(R.id.rdOption2_Lis);
        rdOp3 = findViewById(R.id.rdOption3_Lis);
        rdOp4 = findViewById(R.id.rdOption4_Lis);
        rdgChoices = findViewById(R.id.radiochoices_Lis);
        btnLoudspeaker = findViewById(R.id.btnLoudspeaker_Lis);
        seekBar = findViewById(R.id.seekbar_Lis);
    }

}