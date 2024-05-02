package com.example.duolingoapp.taikhoan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.duolingoapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextView backForgetPass;

    private Button btnSendMail_ResetPass;

    private EditText edEmail;

    FirebaseAuth mAuth;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        backForgetPass = findViewById(R.id.txtBack_ForgetPass);
        edEmail = findViewById(R.id.edEmail_ForgetPass);
        btnSendMail_ResetPass = findViewById(R.id.btnSendMail_ResetPass);

        changeBtnColor();

        mAuth = FirebaseAuth.getInstance();

        backForgetPass.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Gửi Intent với extra để yêu cầu LoginActivity ẩn layout launch_screen.xml
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                intent.putExtra("hideLaunchScreen", true);
                startActivity(intent);
                // Kết thúc Activity hiện tại
                finish();
            }
        });

        btnSendMail_ResetPass.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

    }

    private void resetPassword(){
        String email = edEmail.getText().toString().trim();

        if(email.isEmpty()){
            edEmail.setError("Hãy nhập Email của bạn!");
            edEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            edEmail.setError("Hãy nhập đúng Email!");
            edEmail.requestFocus();
        }

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ForgotPasswordActivity.this,"Hãy kiểm tra (Hộp thư đến) của bạn để tiến hành thiết lập lại mật khẩu!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                    intent.putExtra("hideLaunchScreen", true);
                    startActivity(intent);
                    // Kết thúc Activity hiện tại
                    finish();
                }
                else {
                    Toast.makeText(ForgotPasswordActivity.this,"KHÔNG THÀNH CÔNG!Hãy kiểm tra lại Email của bạn và thử lại!", Toast.LENGTH_LONG).show();
                }

            }
        });
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
                // Kiểm tra xem tất cả các EditText đã nhập đầy đủ thông tin chưa
                String email = edEmail.getText().toString().trim();

                // Nếu tất cả các EditText đều đã được nhập, thay đổi màu nền và màu chữ của button
                if (!email.isEmpty()) {
                    // Lấy màu từ tài nguyên màu trong tệp colors.xml
                    int rectangle6Color = ContextCompat.getColor(ForgotPasswordActivity.this, R.color.rectangle_6_color);
                    btnSendMail_ResetPass.setBackgroundTintList(ColorStateList.valueOf(rectangle6Color));
                    btnSendMail_ResetPass.setTextColor(Color.WHITE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No need to implement anything here
            }
        };

        // Áp dụng TextWatcher cho từng EditText
        edEmail.addTextChangedListener(textWatcher);
    }
}