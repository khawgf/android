package com.example.duolingoapp.taikhoan;

import static android.graphics.Color.pack;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.duolingoapp.MainActivity;
import com.example.duolingoapp.R;
//import com.facebook.AccessToken;
//import com.facebook.CallbackManager;
//import com.facebook.FacebookCallback;
//import com.facebook.FacebookException;
//import com.facebook.login.LoginManager;
//import com.facebook.login.LoginResult;
import com.example.duolingoapp.premium.PremiumDB;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private static final int PROGRESS_INTERVAL = 200; // milliseconds
    private static final int MAX_PROGRESS = 100;

    private static final int RC_SIGN_IN = 9001;

    private ProgressBar progressBar;
    private View signInLayout;

    private TextView signUp, forgetPassword;

    private EditText edEmail, edPassword;

    private Button btnSignIn, btnGoogleAuth, btnFacebookAuth;

    private ImageButton btnShowHidePassword;

    DatabaseAccess DB;
    private FirebaseAuth mAuth;

    private FirebaseDatabase database;

    private GoogleSignInClient mGoogleSignInClient;

    FirebaseDatabase rootNode;
    DatabaseReference reference;
    PremiumDB premiumDB;

    CallbackManager mCallbackManager; //đăng nhập với facebook


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launch_screen);

        FirebaseApp.initializeApp(this);

        progressBar = findViewById(R.id.progressBar_LaunchScreen);
        signInLayout = findViewById(R.id.signInLayout);
        signUp = findViewById(R.id.txtSignUp);
        forgetPassword = findViewById(R.id.txtForgetPassword);
        edEmail = findViewById(R.id.edEmail_SignIn);
        edPassword = findViewById(R.id.edPassword_SignIn);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnShowHidePassword = findViewById(R.id.btnShowHidePassword_SignIn);

        btnGoogleAuth = findViewById(R.id.btnSignInWithGoogle);
        btnFacebookAuth = findViewById(R.id.btnSignInWithFacebook);

        changeBtnColor();

        DB = DatabaseAccess.getInstance(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        premiumDB = PremiumDB.getInstance(getApplicationContext());
        FacebookSdk.sdkInitialize(getApplicationContext());

        //đăng nhập với google
        database = FirebaseDatabase.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

        // Yêu cầu người dùng chọn tài khoản Google mỗi khi đăng nhập
        mGoogleSignInClient.signOut(); // Đảm bảo rằng người dùng phải chọn lại tài khoản

        //đăng nhập với google
        btnGoogleAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("isInternetConnected login", String.valueOf(isNetworkConnected(LoginActivity.this)));
                if (isNetworkConnected(LoginActivity.this)){
                    googleSigIn();
                } else {
                    Toast.makeText(LoginActivity.this, "Vui lòng kết nối mạng", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnFacebookAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("isInternetConnected login", String.valueOf(isNetworkConnected(LoginActivity.this)));
                if (isNetworkConnected(LoginActivity.this)){
                    // Sử dụng LoginManager để bắt đầu quá trình đăng nhập bằng Facebook
                    LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile","email"));
                } else {
                    Toast.makeText(LoginActivity.this, "Vui lòng kết nối mạng", Toast.LENGTH_SHORT).show();
                }

            }
        });


        //duy trì đăng nhập
        if(mAuth.getCurrentUser()!=null){
            DB.iduser = mAuth.getCurrentUser().getUid();
            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }

        //đăng nhập bằng facebook
        mCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        AccessToken accessToken = loginResult.getAccessToken();
                        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject json, GraphResponse response) {
                                // Application code
                                if (response.getError() != null) {
                                    System.out.println("ERROR");
                                } else {
                                    String fbUserEmail = json.optString("email");

                                    AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
                                    loginWithGoogleOrFacebook(credential, fbUserEmail);

                                }
                                Log.d("SignUpActivity", response.toString());
                            }
                        });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,gender, birthday");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });

        // Nhận extra từ Intent
        boolean hideLaunchScreen = getIntent().getBooleanExtra("hideLaunchScreen", false);

        // Ẩn layout launch_screen.xml nếu được yêu cầu từ SignUp Activity
        if (hideLaunchScreen) {
            progressBar.setVisibility(View.GONE);
            signInLayout.setVisibility(View.VISIBLE);
        } else {
            // Ẩn layout sign_in.xml ban đầu
            signInLayout.setVisibility(View.GONE);
            // Nếu không được yêu cầu, tiến hành mô phỏng tiến trình
            simulateProgress();
        }

        signUp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        forgetPassword.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("isInternetConnected login", String.valueOf(isNetworkConnected(LoginActivity.this)));
                if (isNetworkConnected(LoginActivity.this)){
                    String email = edEmail.getText().toString().trim();
                    String matkhau = edPassword.getText().toString().trim();
                    if (TextUtils.isEmpty(email)) {
                        Toast.makeText(getApplicationContext(),
                                        "Hãy nhập Email của bạn!!",
                                        Toast.LENGTH_LONG)
                                .show();
                        return;
                    }

                    if (TextUtils.isEmpty(matkhau)) {
                        Toast.makeText(getApplicationContext(),
                                        "Hãy nhập mật khẩu của bạn!!",
                                        Toast.LENGTH_LONG)
                                .show();
                        return;
                    }

                    // signin existing user

                    mAuth.signInWithEmailAndPassword(email, matkhau)
                            .addOnCompleteListener(
                                    new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(
                                                @NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getApplicationContext(),
                                                                "Đăng nhập thành công!!",
                                                                Toast.LENGTH_LONG)
                                                        .show();

                                                DB.iduser = mAuth.getCurrentUser().getUid();

                                                //DB.CapNhatUser(mAuth.getCurrentUser().getUid());
                                                // hide the progress bar
                                                // if sign-in is successful
                                                // intent to home activity
                                                Intent intent = new Intent(LoginActivity.this,
                                                        MainActivity.class);
                                                startActivity(intent);
                                            } else {
                                                // sign-in failed
                                                Toast.makeText(getApplicationContext(),
                                                                "Sai Email hoặc mật khẩu!!",
                                                                Toast.LENGTH_LONG)
                                                        .show();
                                            }
                                        }
                                    });
                } else {
                    Toast.makeText(LoginActivity.this, "Vui lòng kết nối mạng", Toast.LENGTH_SHORT).show();
                }


                // Kiểm tra kết nối internet
                //isInternetConnected = isNetworkConnected(LoginActivity.this);

//                if (!isInternetConnected) {
//                    // Nếu không có kết nối internet, kiểm tra đăng nhập bằng SQLite
//                    if (DB.checkLoginSQLite(email, matkhau)) {
//                        Toast.makeText(getApplicationContext(),
//                                        "Đăng nhập thành công!!",
//                                        Toast.LENGTH_LONG)
//                                .show();
//                        // Nếu đăng nhập thành công từ SQLite, chuyển đến MainActivity
//                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                        startActivity(intent);
//                        return;
//                    } else {
//                        // Nếu không đăng nhập thành công từ SQLite, thông báo cho người dùng
//                        Toast.makeText(getApplicationContext(),
//                                        "Sai Email hoặc mật khẩu!!",
//                                        Toast.LENGTH_LONG)
//                                .show();
//                        return;
//                    }
//                }

                // Nếu có kết nối internet, tiến hành đăng nhập bằng Firebase


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

    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    private void simulateProgress() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            int progress = 0;

            @Override
            public void run() {
                progress += 5; // Tăng giá trị tiến trình
                progressBar.setProgress(progress);

                if (progress < MAX_PROGRESS) {
                    // Nếu tiến trình chưa đạt 100%, tiếp tục cập nhật
                    handler.postDelayed(this, PROGRESS_INTERVAL);
                } else {
                    // Nếu tiến trình đạt 100%, ẩn progressBar và hiển thị layout sign_in.xml
                    progressBar.setVisibility(View.GONE);
                    signInLayout.setVisibility(View.VISIBLE);
                }
            }
        }, PROGRESS_INTERVAL);
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
                String matkhau = edPassword.getText().toString().trim();

                // Nếu tất cả các EditText đều đã được nhập, thay đổi màu nền và màu chữ của button
                if (!email.isEmpty() && !matkhau.isEmpty()) {
                    // Lấy màu từ tài nguyên màu trong tệp colors.xml
                    int rectangle6Color = ContextCompat.getColor(LoginActivity.this, R.color.rectangle_6_color);
                    btnSignIn.setBackgroundTintList(ColorStateList.valueOf(rectangle6Color));
                    btnSignIn.setTextColor(Color.WHITE);
                } else{
                    btnSignIn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E6E4EA")));
                    btnSignIn.setTextColor(Color.parseColor("#BAB4B4"));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No need to implement anything here
            }
        };

        // Áp dụng TextWatcher cho từng EditText
        edEmail.addTextChangedListener(textWatcher);
        edPassword.addTextChangedListener(textWatcher);
    }

    //đăng nhập với google
    private void googleSigIn(){

        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent,RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Chuyển kết quả hoạt động trở lại Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode==RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                loginWithGoogleOrFacebook(credential, account.getEmail());
            }catch (ApiException e){
                e.printStackTrace();
            }
        }
    }

    private void loginWithGoogleOrFacebook(AuthCredential credential, String email) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            DatabaseAccess db = new DatabaseAccess(new DatabaseAccess.OnEmailCheckListener() {
                                @Override
                                public void onEmailCheck(boolean emailExists, String idUser) {
                                    if (emailExists) {
                                        //Email tồn tại, không cần lưu thêm
                                        DB.iduser = idUser;
                                        Toast.makeText(getApplicationContext(),
                                                        "Đăng nhập thành công!!",
                                                        Toast.LENGTH_LONG)
                                                .show();
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        // Kết thúc Activity hiện tại
                                        finish();
                                    } else {
                                        //Email không tồn tại, lưu thông tin người dùng
                                        Toast.makeText(getApplicationContext(),
                                                        "Đăng nhập thành công!!",
                                                        Toast.LENGTH_LONG)
                                                .show();
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        DB.iduser = user.getUid();
                                        String phone = "";
                                        if (user.getPhoneNumber() != null){
                                            phone = user.getPhoneNumber();
                                        }
                                        DB.open();
                                        Boolean insert = DB.insertData(user.getUid(),user.getDisplayName(),email,phone,""); //SQLite
                                        DB.close();

                                        // if the user created intent to login activity
                                        rootNode= FirebaseDatabase.getInstance();
                                        reference= rootNode.getReference("User");

                                        User newuser = new User(user.getUid(), user.getDisplayName(),email, "", phone);
                                        reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(newuser);

                                        premiumDB.addPremium(user.getUid(), 1);
                                        premiumDB.addPremium(user.getUid(), 2);
                                        premiumDB.addPremium(user.getUid(), 3);

                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        // Kết thúc Activity hiện tại
                                        finish();
                                    }
                                }
                            });
                            // Gọi phương thức checkEmailByFirebase
                            db.checkEmailByFirebase(email);
                        }
                        else {
                            Toast.makeText(LoginActivity.this,"Đăng nhập thất bại",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

//    private void loginWithGoogleOrFacebook(AuthCredential credential, String email) {
//
//        DatabaseAccess db = new DatabaseAccess(new DatabaseAccess.OnEmailCheckListener() {
//            @Override
//            public void onEmailCheck(boolean emailExists, String idUser) {
//                if (emailExists) {
//                    //Email tồn tại, không cần lưu thêm
//                    DB.iduser = idUser;
//                    Toast.makeText(getApplicationContext(),
//                                    "Đăng nhập thành công!!",
//                                    Toast.LENGTH_LONG)
//                            .show();
//                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                    startActivity(intent);
//                    // Kết thúc Activity hiện tại
//                    finish();
//                } else {
//                    //Email không tồn tại, lưu thông tin người dùng
//                    mAuth.signInWithCredential(credential)
//                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                                @Override
//                                public void onComplete(@NonNull Task<AuthResult> task) {
//                                    if (task.isSuccessful()){
//                                        Toast.makeText(getApplicationContext(),
//                                                        "Đăng nhập thành công!!",
//                                                        Toast.LENGTH_LONG)
//                                                .show();
//                                        FirebaseUser user = mAuth.getCurrentUser();
//                                        DB.iduser = user.getUid();
//                                        String phone = "";
//                                        if (user.getPhoneNumber() != null){
//                                            phone = user.getPhoneNumber();
//                                        }
//                                        DB.open();
//                                        Boolean insert = DB.insertData(user.getUid(),user.getDisplayName(),email,phone,"");
//                                        DB.close();
//
//                                        // if the user created intent to login activity
//                                        rootNode= FirebaseDatabase.getInstance();
//                                        reference= rootNode.getReference("User");
//
//                                        User newuser = new User(user.getUid(), user.getDisplayName(),email, "", phone);
//                                        reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(newuser);
//
//                                        premiumDB.addPremium(user.getUid(), 1);
//                                        premiumDB.addPremium(user.getUid(), 2);
//                                        premiumDB.addPremium(user.getUid(), 3);
//
//                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                                        startActivity(intent);
//                                        // Kết thúc Activity hiện tại
//                                        finish();
//
//                                    }
//                                    else {
//                                        Toast.makeText(LoginActivity.this,"Đăng nhập thất bại",Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//                            });
//                }
//            }
//        });
//        // Gọi phương thức checkEmailByFirebase
//        db.checkEmailByFirebase(email);
//    }

}