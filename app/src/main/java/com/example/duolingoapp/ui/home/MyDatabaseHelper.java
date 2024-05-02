package com.example.duolingoapp.ui.home;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "HocNgonNgu.db";
    private static final int DATABASE_VERSION = 1;

    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Đảm bảo bảng "users" đã được tạo trước đó trong cơ sở dữ liệu
        // Ví dụ: CREATE TABLE IF NOT EXISTS users (id TEXT PRIMARY KEY, name TEXT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xử lý khi cần nâng cấp cơ sở dữ liệu
    }
}