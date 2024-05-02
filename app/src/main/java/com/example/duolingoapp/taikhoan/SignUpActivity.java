package com.example.duolingoapp.taikhoan;

import static android.graphics.Color.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.duolingoapp.R;
import com.example.duolingoapp.premium.PremiumDB;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private TextView backSinUp;
    private View signInLayout;

    private Button btnSignUp;

    private EditText edName, edEmail, edPassword, edRePassword, edSDT;

    private ImageButton btnShowHidePassword, btnShowHideRePassword;

    FirebaseAuth mAuth;

    FirebaseDatabase rootNode;
    DatabaseReference reference;
    DatabaseAccess DB;
    PremiumDB premiumDB;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        backSinUp = findViewById(R.id.txtBackSignUp);
        signInLayout = findViewById(R.id.signInLayout);
        btnSignUp = findViewById(R.id.btnSignUp);
        edName = findViewById(R.id.edName);
        edSDT = findViewById(R.id.edPhoneNumber);
        edEmail = findViewById(R.id.edEmailAddress);
        edPassword = findViewById(R.id.edPassword);
        edRePassword = findViewById(R.id.edRePassword);
        btnShowHidePassword = findViewById(R.id.btnShowHidePassword);
        btnShowHideRePassword = findViewById(R.id.btnShowHideRePassword);

        changeBtnColor();

        mAuth = FirebaseAuth.getInstance();
        DB =  DatabaseAccess.getInstance(getApplicationContext());
        premiumDB = PremiumDB.getInstance(getApplicationContext());


        backSinUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gửi Intent với extra để yêu cầu LoginActivity ẩn layout launch_screen.xml
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                intent.putExtra("hideLaunchScreen", true);
                startActivity(intent);
                // Kết thúc Activity hiện tại
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("isInternetConnected signup", String.valueOf(isNetworkConnected(SignUpActivity.this)));
                if (isNetworkConnected(SignUpActivity.this)){
                    String hoten = edName.getText().toString().trim();
                    String email = edEmail.getText().toString().trim();
                    String sdt = edSDT.getText().toString().trim();
                    String matkhau = edPassword.getText().toString().trim();
                    String xacnhanmatkhau = edRePassword.getText().toString().trim();

                    if(hoten.equals("")||email.equals("")||sdt.equals("")||matkhau.equals(""))
                    {
                        Toast.makeText(SignUpActivity.this, "Điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        if(matkhau.equals(xacnhanmatkhau)){

                            Boolean kiemtrataikhoan = DB.checktaikhoan(email);
                            if(kiemtrataikhoan == false)
                            {


                                mAuth.createUserWithEmailAndPassword(email, matkhau)
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task)
                                            {
                                                if (task.isSuccessful()) {
                                                    DB.open();
                                                    Boolean insert = DB.insertData(mAuth.getCurrentUser().getUid(),hoten,email,sdt,matkhau);
                                                    DB.close();
                                                    btnSignUp.setText(insert.toString());
                                                    Toast.makeText(SignUpActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();

                                                    // if the user created intent to login activity
                                                    rootNode= FirebaseDatabase.getInstance();
                                                    reference= rootNode.getReference("User");
                                                    User newuser = new User(mAuth.getCurrentUser().getUid(), hoten, email, matkhau, sdt);
                                                    reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(newuser);

                                                    premiumDB.addPremium(mAuth.getCurrentUser().getUid(), 1);
                                                    premiumDB.addPremium(mAuth.getCurrentUser().getUid(), 2);
                                                    premiumDB.addPremium(mAuth.getCurrentUser().getUid(), 3);

                                                    Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                                                    intent.putExtra("hideLaunchScreen", true);
                                                    startActivity(intent);
                                                    // Kết thúc Activity hiện tại
                                                    finish();
                                                }
                                                else {

                                                    Toast.makeText(SignUpActivity.this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                            else{
                                Toast.makeText(SignUpActivity.this, "tên tài khoản đã tồn tại", Toast.LENGTH_SHORT).show();
                            }


                        }
                        else{
                            Toast.makeText(SignUpActivity.this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                            edPassword.setText("");
                            edRePassword.setText("");
                        }
                    }
                } else {
                    Toast.makeText(SignUpActivity.this, "Vui lòng kết nối mạng", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Bắt sự kiện nhấn nút để chuyển đổi giữa hiển thị và ẩn mật khẩu
        btnShowHidePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edPassword.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    // Nếu mật khẩu đang hiển thị, chuyển đổi về ẩn mật khẩu
                    edPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    btnShowHidePassword.setImageResource(R.drawable.icon_visible_off); // Đặt icon là mắt đóng
                } else {
                    // Nếu mật khẩu đang ẩn, chuyển đổi về hiển thị mật khẩu
                    edPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    btnShowHidePassword.setImageResource(R.drawable.icon_visible); // Đặt icon là mắt mở
                }

                // Đặt con trỏ về cuối chuỗi để không làm mất vị trí hiện tại của người dùng
                edPassword.setSelection(edPassword.getText().length());
            }
        });

        // Bắt sự kiện nhấn nút để chuyển đổi giữa hiển thị và ẩn mật khẩu
        btnShowHideRePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edRePassword.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    // Nếu mật khẩu đang hiển thị, chuyển đổi về ẩn mật khẩu
                    edRePassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    btnShowHideRePassword.setImageResource(R.drawable.icon_visible_off); // Đặt icon là mắt đóng
                } else {
                    // Nếu mật khẩu đang ẩn, chuyển đổi về hiển thị mật khẩu
                    edRePassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    btnShowHideRePassword.setImageResource(R.drawable.icon_visible); // Đặt icon là mắt mở
                }

                // Đặt con trỏ về cuối chuỗi để không làm mất vị trí hiện tại của người dùng
                edRePassword.setSelection(edRePassword.getText().length());
            }
        });
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
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
                String hoten = edName.getText().toString().trim();
                String email = edEmail.getText().toString().trim();
                String sdt = edSDT.getText().toString().trim();
                String matkhau = edPassword.getText().toString().trim();
                String xacnhanmatkhau = edRePassword.getText().toString().trim();

                // Nếu tất cả các EditText đều đã được nhập, thay đổi màu nền và màu chữ của button
                if (!hoten.isEmpty() && !email.isEmpty() && !sdt.isEmpty() && !matkhau.isEmpty() && !xacnhanmatkhau.isEmpty()) {
                    // Lấy màu từ tài nguyên màu trong tệp colors.xml
                    int rectangle6Color = ContextCompat.getColor(SignUpActivity.this, R.color.rectangle_6_color);
                    btnSignUp.setBackgroundTintList(ColorStateList.valueOf(rectangle6Color));
                    btnSignUp.setTextColor(Color.WHITE);
                }else{
                    btnSignUp.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E6E4EA")));
                    btnSignUp.setTextColor(Color.parseColor("#BAB4B4"));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No need to implement anything here
            }
        };

        // Áp dụng TextWatcher cho từng EditText
        edName.addTextChangedListener(textWatcher);
        edEmail.addTextChangedListener(textWatcher);
        edSDT.addTextChangedListener(textWatcher);
        edPassword.addTextChangedListener(textWatcher);
        edRePassword.addTextChangedListener(textWatcher);
    }
}