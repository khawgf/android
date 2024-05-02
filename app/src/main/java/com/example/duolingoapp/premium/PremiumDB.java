package com.example.duolingoapp.premium;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.duolingoapp.taikhoan.DatabaseOpenHelper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class PremiumDB {
    private SQLiteOpenHelper openHelper;
    SQLiteDatabase db;
    FirebaseDatabase rootNode; //f_instanse
    DatabaseReference reference; //f_db
    private  static PremiumDB instance;
    Cursor c= null;

    private PremiumDB(Context context){
        this.openHelper = new DatabaseOpenHelper(context);

    }

    public static PremiumDB getInstance(Context context){
        if(instance==null){
            instance = new PremiumDB(context);
        }
        return instance;
    }

    public void open(){
        this.db = openHelper.getWritableDatabase();
    }
    public  void close(){
        if(db!=null){
            this.db.close();
        }
    }

    public void addPremium(String idUser, int idBo) {

        // Kiểm tra xem các ID_User, ID_Bo đã được khai báo hay chưa
        if (idUser != null && idBo > 0) {
            //Add firebase
            rootNode= FirebaseDatabase.getInstance();
            reference= rootNode.getReference("Premium");
            Premium newpremium = new Premium(idUser, idBo);
            // Tạo một key ngẫu nhiên cho đối tượng mới
            String key = reference.push().getKey();
            reference.child(key).setValue(newpremium);

            //Add SQLite
            db = openHelper.getWritableDatabase();

            // Kiểm tra DB có null không trước khi sử dụng
            if (db != null) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("ID_User",idUser);
                contentValues.put("ID_Bo",idBo);
                long result = db.insert("Premium",null,contentValues);
                if (result != -1) {
                    Log.d("Add Premium", "Status added successfully");
                } else {
                    Log.d("Add Premium Error", "Failed to add status");
                }

            } else {
                Log.d("Premium DB Error: ", "Database is null");
            }
        } else {
            Log.d("Premium Data Error: ", "User or IDs not initialized");
        }
    }
    @SuppressLint("Range")
    public ArrayList<Premium> getListPremiumOfUser(String idUser) {
        ArrayList<Premium> premiumList = new ArrayList<>();
        db = openHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT * FROM Premium WHERE ID_User = ? ";
            cursor = db.rawQuery(query, new String[]{idUser});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String iduser = cursor.getString(cursor.getColumnIndex("ID_User"));
                    int idbo = cursor.getInt(cursor.getColumnIndex("ID_Bo"));

                    Premium premium = new Premium(iduser, idbo);
                    premiumList.add(premium);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PremiumDB Error", "Error getting status list: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return premiumList;
    }

    public boolean checkPremium(String idUser, int idBo) {
        db = openHelper.getReadableDatabase();

        String query = "SELECT * FROM Premium WHERE ID_User = ? AND ID_Bo = ? ";
        Cursor cursor = db.rawQuery(query, new String[]{idUser, String.valueOf(idBo)});

        // Kiểm tra xem có dòng dữ liệu nào trả về không
        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }
}