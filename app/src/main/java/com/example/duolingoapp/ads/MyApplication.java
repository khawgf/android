package com.example.duolingoapp.ads;

import android.app.Activity;
import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;


import com.example.duolingoapp.network;
import com.example.duolingoapp.taikhoan.DatabaseAccess;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MyApplication extends Application implements Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {
    private AppOpenAdManager appOpenAdManager;
    private Activity currentActivity;
    private DatabaseReference usersRef; // Tham chiếu đến "users" trong Firebase Realtime Database
    private com.example.duolingoapp.ui.home.MyDatabaseHelper dbHelper; // Helper class để quản lý cơ sở dữ liệu SQLite

    DatabaseAccess DB;

    @Override
    public void onCreate() {
        super.onCreate();
        this.registerActivityLifecycleCallbacks(this);

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        appOpenAdManager = new AppOpenAdManager();

        startService(new Intent(this, network.class));
        fetchDatauserNew();
        fetchDatatrangthaiNew();
        fetchDatapremiumNew();
    }

    public void fetchDatauserNew() {
        DB = DatabaseAccess.getInstance(getApplicationContext());
        usersRef = FirebaseDatabase.getInstance().getReference("User");
        dbHelper = new com.example.duolingoapp.ui.home.MyDatabaseHelper(getApplicationContext());

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.beginTransaction();

                try {
                    db.delete("User", null, null);

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Lấy giá trị id từ Firebase
                        String userId = snapshot.child("iduser").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String hoTen = snapshot.child("hoTen").getValue(String.class);
                        String password = snapshot.child("password").getValue(String.class);
                        String sdt = snapshot.child("sdt").getValue(String.class);

                        if (userId != null) {
                            ContentValues values = new ContentValues();
                            values.put("ID_User", userId);
                            values.put("Email", email);
                            values.put("HoTen", hoTen);
                            values.put("Password", password);
                            values.put("SDT", sdt);
                            values.put("Network", 1);

                            long newRowId = db.insert("User", null, values);
                            DB.upgradeImage(userId);
                            if (newRowId != -1) {
                                Log.d("MyApplication", "User added to SQLite: " + userId);
                            } else {
                                Log.e("MyApplication", "Failed to add user to SQLite");
                            }
                        }
                    }

                    db.setTransactionSuccessful(); // Đánh dấu giao dịch thành công
                } catch (Exception e) {
                    Log.e("MyApplication", "Error adding users to SQLite", e);
                } finally {
                    db.endTransaction(); // Kết thúc giao dịch, đảm bảo tài nguyên được giải phóng
                    db.close(); // Đóng kết nối đến cơ sở dữ liệu
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MyApplication", "Failed to read value.", databaseError.toException());
            }
        });
    }

    public void fetchDatatrangthaiNew() {
        usersRef = FirebaseDatabase.getInstance().getReference("TrangThai");
        dbHelper = new com.example.duolingoapp.ui.home.MyDatabaseHelper(getApplicationContext());

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.beginTransaction();

                try {
                    db.delete("TrangThai", null, null);

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Lấy giá trị id từ Firebase
                        Long idbo = snapshot.child("idBo").getValue(Long.class);
                        Long idskill = snapshot.child("idSkill").getValue(Long.class);
                        String iduser = snapshot.child("idUser").getValue(String.class);
                        Long score = snapshot.child("score").getValue(Long.class);
                        String timedone = snapshot.child("timedone").getValue(String.class);

                        if (iduser != null) {
                            ContentValues values = new ContentValues();
                            values.put("ID_User", iduser);
                            values.put("ID_Bo", idbo);
                            values.put("ID_Skill", idskill);
                            values.put("Score", score);
                            values.put("Timedone", timedone);

                            long newRowId = db.insert("TrangThai", null, values);

                            if (newRowId != -1) {
                                Log.d("MyApplication", "trangthai added to SQLite: " + iduser);
                            } else {
                                Log.e("MyApplication", "Failed to add trangthai to SQLite");
                            }
                        }
                    }

                    db.setTransactionSuccessful(); // Đánh dấu giao dịch thành công
                } catch (Exception e) {
                    Log.e("MyApplication", "Error adding trangthai to SQLite", e);
                } finally {
                    db.endTransaction(); // Kết thúc giao dịch, đảm bảo tài nguyên được giải phóng
                    db.close(); // Đóng kết nối đến cơ sở dữ liệu
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MyApplication", "Failed to read value.", databaseError.toException());
            }
        });
    }

    public void fetchDatapremiumNew() {
        usersRef = FirebaseDatabase.getInstance().getReference("Premium");
        dbHelper = new com.example.duolingoapp.ui.home.MyDatabaseHelper(getApplicationContext());

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.beginTransaction();

                try {
                    db.delete("Premium", null, null);

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Lấy giá trị id từ Firebase
                        Long idbo = snapshot.child("idBo").getValue(Long.class);
                        String iduser = snapshot.child("idUser").getValue(String.class);

                        if (iduser != null) {
                            ContentValues values = new ContentValues();
                            values.put("ID_User", iduser);
                            values.put("ID_Bo", idbo);

                            long newRowId = db.insert("Premium", null, values);

                            if (newRowId != -1) {
                                Log.d("MyApplication", "Premium added to SQLite: " + iduser);
                            } else {
                                Log.e("MyApplication", "Failed to add Premium to SQLite");
                            }
                        }
                    }

                    db.setTransactionSuccessful(); // Đánh dấu giao dịch thành công
                } catch (Exception e) {
                    Log.e("MyApplication", "Error adding Premium to SQLite", e);
                } finally {
                    db.endTransaction(); // Kết thúc giao dịch, đảm bảo tài nguyên được giải phóng
                    db.close(); // Đóng kết nối đến cơ sở dữ liệu
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MyApplication", "Failed to read value.", databaseError.toException());
            }
        });
    }
    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStart(owner);
        appOpenAdManager.showAdIfAvailable(currentActivity);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity;
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }

    public void loadAd(@NonNull Activity activity) {
        appOpenAdManager.loadAd(activity);
    }

    public interface OnShowAdCompleteListener {
        void onAdShown();
    }

    public void showAdIfAvailable(Activity activity, OnShowAdCompleteListener onShowAdCompleteListener) {
        appOpenAdManager.showAdIfAvailable(activity, onShowAdCompleteListener);
    }

    public void closeAd() {
        appOpenAdManager.closeAd();
    }

    private static class AppOpenAdManager {
        private static final String AD_ID = "ca-app-pub-3940256099942544/9257395921";

        private AppOpenAd appOpenAd = null;
        private boolean isLoadingAd = false;
        private boolean isShowingAd = false;

        public AppOpenAdManager() {

        }

        private void loadAd(Context context) {
            if (isLoadingAd || isAdAvailable()) {
                return;
            }

            isLoadingAd = true;
            AdRequest request = new AdRequest.Builder().build();
            AppOpenAd.load(context, AD_ID, request, new AppOpenAd.AppOpenAdLoadCallback() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    isLoadingAd = false;
                    // Log ra nguyên nhân lỗi
                    String errorReason = "Ad failed to load: " + loadAdError.getMessage();
                    Log.e("AdLoadError", errorReason);
                    // Hiển thị thông báo Toast
                    Toast.makeText(context, "There was an error while loading ad", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAdLoaded(@NonNull AppOpenAd openAd) {
                    super.onAdLoaded(openAd);
                    appOpenAd = openAd;
                    isLoadingAd = false;
                    Toast.makeText(context, "Ad loaded", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private boolean isAdAvailable() {
            return appOpenAd != null;
        }

        private void showAdIfAvailable(Activity activity) {
            showAdIfAvailable(activity, new OnShowAdCompleteListener() {
                @Override
                public void onAdShown() {

                }
            });
        }

        private void showAdIfAvailable(Activity activity, OnShowAdCompleteListener onShowAdCompleteListener) {
            if (isShowingAd) {
                return;
            }

            if (!isAdAvailable()) {
                onShowAdCompleteListener.onAdShown();
                return;
            }

            appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    isShowingAd = false;
                    onShowAdCompleteListener.onAdShown();
                    appOpenAd = null;
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    isShowingAd = false;
                    onShowAdCompleteListener.onAdShown();
                    appOpenAd = null;
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent();
                }
            });

            isShowingAd = true;
            appOpenAd.show(activity);
        }
        private void closeAd() {
            if (appOpenAd != null) {
                appOpenAd = null;
            }
        }
    }
}