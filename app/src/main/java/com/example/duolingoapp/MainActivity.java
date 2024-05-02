package com.example.duolingoapp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.navigation.ui.AppBarConfiguration;


import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.Manifest;

import com.example.duolingoapp.ads.MyApplication;
import com.example.duolingoapp.ads.SplashActivity;
import com.example.duolingoapp.bocauhoi.QuestionListActivity;
import com.example.duolingoapp.taikhoan.DatabaseAccess;
import com.example.duolingoapp.taikhoan.LoginActivity;
import com.example.duolingoapp.taikhoan.ProfileActivity;
import com.example.duolingoapp.taikhoan.SettingActivity;
import com.example.duolingoapp.taikhoan.User;
import com.example.duolingoapp.thongbao.NotificationActivity;
import com.example.duolingoapp.thongbao.NotificationService;
import com.example.duolingoapp.tudien.DictionaryActivity;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity{

//    private DrawerLayout mDrawerLayout;

    private AppBarConfiguration mAppBarConfiguration;
    String username;

    TextView txtName, txtEmail;

    DatabaseAccess DB;

    User user;

    private NotificationService notificationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Toolbar toolbar =findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        mDrawerLayout = findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
//                R.string.navigation_drawer_open,R.string.navigation_drawer_close);
//        mDrawerLayout.addDrawerListener(toggle);
//        toggle.syncState();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        txtName = headerView.findViewById(R.id.txtName_nav);
        txtEmail = headerView.findViewById(R.id.txtEmail_nav);

        DB = DatabaseAccess.getInstance(getApplicationContext());
        if (DB.iduser!=null){
            Log.d("userid", DB.iduser);
            user = DB.getUser(DB.iduser);
            txtName.setText(user.getHoTen());
            txtEmail.setText(user.getEmail());
        }

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //ads
        MobileAds.initialize(this);
        Application application = getApplication();
        ((MyApplication) application).loadAd(this);

        createTimer();

        //notification
        notificationService = new NotificationService(MainActivity.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requirePermission();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void requirePermission(){
        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
            ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean o) {
                    if (o) {
                        notificationService.registerNotification();
                        Toast.makeText(MainActivity.this, "Accept permission", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            activityResultLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }


    public void createTimer() {
        CountDownTimer countDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                Application application = getApplication();
                ((MyApplication) application).showAdIfAvailable(MainActivity.this, new MyApplication.OnShowAdCompleteListener() {
                    @Override
                    public void onAdShown() {
                        ((MyApplication) application).closeAd();
                    }
                });
            }
        };
        countDownTimer.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Xử lý sự kiện khi người dùng nhấn vào mục menu
        if (item.getItemId() == R.id.action_settings){
                // Chuyển hướng sang trang SettingsActivity
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                intent.putExtra("back", "settingHome");
                startActivity(intent);
                return true;
        }
        return false;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    public static int getIDSkill(String nameSkill){
        switch (nameSkill){
            case "Học từ vựng":
                return 1;
            case "Điền khuyết":
                return 2;
            case "Sắp xếp câu":
                return 3;
            case "Luyện nghe":
                return 4;
            case "Trắc nghiệm":
                return 5;
        }
        return 0;
    }

}