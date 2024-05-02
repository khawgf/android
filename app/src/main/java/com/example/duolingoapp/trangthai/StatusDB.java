package com.example.duolingoapp.trangthai;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.duolingoapp.bocauhoi.QuestionList;
import com.example.duolingoapp.bocauhoi.QuestionListActivity;
import com.example.duolingoapp.taikhoan.DatabaseOpenHelper;
import com.example.duolingoapp.ui.home.Database;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class StatusDB {
    private SQLiteOpenHelper openHelper;
    SQLiteDatabase db;
    FirebaseDatabase rootNode; //f_instanse
    DatabaseReference reference; //f_db
    private  static StatusDB instance;
    Cursor c= null;

    private StatusDB(Context context){
        this.openHelper = new DatabaseOpenHelper(context);

    }

    public  static  StatusDB getInstance(Context context){
        if(instance==null){
            instance = new StatusDB(context);
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

    public void addStatus(String idUser, int idBo, int idSkill, int score, String timedone, Context context) {
        // Kiểm tra xem các ID_User, ID_Bo, ID_Skill đã được khai báo hay chưa
        if (idUser != null && idBo > 0 && idSkill > 0) {
            //Add firebase
            rootNode= FirebaseDatabase.getInstance();
            reference= rootNode.getReference("TrangThai");
            Status newstatus = new Status(idUser, idBo,idSkill, score, timedone);
            // Tạo một key ngẫu nhiên cho đối tượng mới
            String key = reference.push().getKey();
            reference.child(key).setValue(newstatus);

            //Add SQLite
            db = openHelper.getWritableDatabase();

            // Kiểm tra DB có null không trước khi sử dụng
            if (db != null) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("ID_User",idUser);
                contentValues.put("ID_Bo",idBo);
                contentValues.put("ID_Skill",idSkill);
                contentValues.put("Score",score);
                contentValues.put("Timedone",timedone);
                contentValues.put("Network", isNetworkConnected(context) ? 1 : 0);
                long result = db.insert("TrangThai",null,contentValues);
                if (result != -1) {
                    Log.d("Add Status", "Status added successfully");
                } else {
                    Log.d("Add Status Error", "Failed to add status");
                }

            } else {
                Log.d("DB Error: ", "Database is null");
            }
        } else {
            Log.d("Status Data Error: ", "User or IDs not initialized");
        }
    }

    public boolean updateStatusToFirebase(String idUser, int idBo, int idSkill, int score, String timedone) {
        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference("TrangThai");

        // Tạo query để tìm key tương ứng dựa trên idUser, idBo, idSkill
        Query query = reference.orderByChild("idUser").equalTo(idUser);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Kiểm tra các ràng buộc bổ sung
                    if (snapshot.child("idBo").getValue(Integer.class) == idBo &&
                            snapshot.child("idSkill").getValue(Integer.class) == idSkill) {
                        // Lấy key của dữ liệu
                        String key = snapshot.getKey();

                        // Cập nhật dữ liệu
                        dataSnapshot.child(key).child("score").getRef().setValue(score);
                        dataSnapshot.child(key).child("timedone").getRef().setValue(timedone);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });

        return true; // Trả về true nếu không có lỗi xảy ra
    }

    public boolean updateStatus(String idUser, int idBo, int idSkill, int score, String timedone, Context context){

        updateStatusToFirebase(idUser, idBo, idSkill, score, timedone);

        db = openHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Score",score);
        contentValues.put("Timedone",timedone);
        contentValues.put("Network", isNetworkConnected(context) ? 1 : 0);
        Cursor cursor = db.rawQuery("SELECT * FROM TrangThai WHERE ID_User = ? AND ID_Bo = ? AND ID_Skill = ? ", new String[]{idUser, String.valueOf(idBo), String.valueOf(idSkill)});
        if(cursor.getCount()>0) {
            long result = db.update("TrangThai",contentValues,"ID_User = ? AND ID_Bo = ? AND ID_Skill = ?", new String[]{idUser, String.valueOf(idBo), String.valueOf(idSkill)});
            if(result==-1) {
                return false;
            }
            else {
                return true;
            }
        }
        else {
            return  false;
        }
    }



    @SuppressLint("Range")
    public ArrayList<Status> getListStatusOfUser(String idUser, int idSkill) {
        ArrayList<Status> statusList = new ArrayList<>();
        db = openHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT * FROM TrangThai WHERE ID_User = ? AND ID_Skill = ? ";
            cursor = db.rawQuery(query, new String[]{idUser, String.valueOf(idSkill)});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String iduser = cursor.getString(cursor.getColumnIndex("ID_User"));
                    int idbo = cursor.getInt(cursor.getColumnIndex("ID_Bo"));
                    int idskill = cursor.getInt(cursor.getColumnIndex("ID_Skill"));
                    int score = cursor.getInt(cursor.getColumnIndex("Score"));
                    String timedone = cursor.getString(cursor.getColumnIndex("Timedone"));

                    Status status = new Status(iduser, idbo, idskill, score, timedone);
                    statusList.add(status);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("StatusDB Error", "Error getting status list: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return statusList;
    }

    @SuppressLint("Range")
    public Status checkStatus(String idUser, int idBo, int idSkill) {
        db = openHelper.getReadableDatabase();

        String query = "SELECT * FROM TrangThai WHERE ID_User = ? AND ID_Bo = ? AND ID_Skill = ? ";
        Cursor cursor = db.rawQuery(query, new String[]{idUser, String.valueOf(idBo), String.valueOf(idSkill)});

        // Kiểm tra xem có dòng dữ liệu nào trả về không
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            Status status = new Status(
                    cursor.getString(cursor.getColumnIndex("ID_User")),
                    cursor.getInt(cursor.getColumnIndex("ID_Bo")),
                    cursor.getInt(cursor.getColumnIndex("ID_Skill")),
                    cursor.getInt(cursor.getColumnIndex("Score")),
                    cursor.getString(cursor.getColumnIndex("Timedone"))
            );
            return status;
        } else {
            return null;
        }
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }


    @SuppressLint("Range")
    public void syncUnsyncedRecordsToFirebase() {
        db = openHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TrangThai WHERE Network = 0", null);

        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference("TrangThai");

        if (cursor != null && cursor.moveToFirst()) {
            try {
                db.beginTransaction(); // Bắt đầu giao dịch để xử lý nhiều thao tác cùng một lúc

                do {
                    String idUser = cursor.getString(cursor.getColumnIndex("ID_User"));
                    Long idBo = cursor.getLong(cursor.getColumnIndex("ID_Bo"));
                    Long idSkill = cursor.getLong(cursor.getColumnIndex("ID_Skill"));
                    Long score = cursor.getLong(cursor.getColumnIndex("Score"));
                    String timedone = cursor.getString(cursor.getColumnIndex("Timedone"));

                    // Tạo một key ngẫu nhiên cho đối tượng mới trên Firebase
                    String key = reference.push().getKey();

                    // Thêm dữ liệu mới lên Firebase
                    Status newStatus = new Status(idUser, Math.toIntExact(idBo), Math.toIntExact(idSkill), Math.toIntExact(score), timedone);
                    reference.child(key).setValue(newStatus);

                    // Cập nhật lại trường Network trong SQLite thành 1
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("Network", 1);
                    db.update("TrangThai", contentValues, "ID_User = ? AND ID_Bo = ? AND ID_Skill = ?",
                            new String[]{idUser, String.valueOf(idBo), String.valueOf(idSkill)});

                    Log.d("MyApplication", "Record synced to Firebase: " + idUser);
                } while (cursor.moveToNext());

                db.setTransactionSuccessful(); // Đánh dấu giao dịch thành công
            } catch (Exception e) {
                Log.e("MyApplication", "Error syncing records to Firebase", e);
            } finally {
                db.endTransaction(); // Kết thúc giao dịch
                cursor.close(); // Đóng con trỏ
                db.close(); // Đóng kết nối đến cơ sở dữ liệu
            }
        }
    }


    @SuppressLint("Range")
    public ArrayList<Status> getStatusListFromSQLite() {
        ArrayList<Status> statusList = new ArrayList<>();
        db = openHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT * FROM TrangThai", null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String idUser = cursor.getString(cursor.getColumnIndex("ID_User"));
                    int idBo = cursor.getInt(cursor.getColumnIndex("ID_Bo"));
                    int idSkill = cursor.getInt(cursor.getColumnIndex("ID_Skill"));
                    int score = cursor.getInt(cursor.getColumnIndex("Score"));
                    String timedone = cursor.getString(cursor.getColumnIndex("Timedone"));

                    Status status = new Status(idUser, idBo, idSkill, score, timedone);
                    statusList.add(status);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("StatusDB Error", "Error getting status list from SQLite: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return statusList;
    }


    //Thống kê
    @SuppressLint("Range")
    //Tính tổng điểm theo từng kỹ năng
    public HashMap<Integer, Integer> getScoreBySkill(String idUser) {
        HashMap<Integer, Integer> scoreMap = new HashMap<>();
        db = openHelper.getReadableDatabase();

        String query = "SELECT ID_Skill, SUM(Score) AS TotalScore FROM TrangThai WHERE ID_User = ? GROUP BY ID_Skill";
        Cursor cursor = db.rawQuery(query, new String[]{idUser});

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int idSkill = cursor.getInt(cursor.getColumnIndex("ID_Skill"));
                    int totalScore = cursor.getInt(cursor.getColumnIndex("TotalScore"));
                    scoreMap.put(idSkill, totalScore);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("StatusDB Error", "Error getting score by skill: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return scoreMap;
    }
    @SuppressLint("Range")
    // Tính tổng số bộ câu hỏi mà người dùng đã làm theo từng kỹ năng
    public HashMap<Integer, Integer> getTotalBoBySkill(String idUser) {
        HashMap<Integer, Integer> totalBoMap = new HashMap<>();
        db = openHelper.getReadableDatabase();

        String query = "SELECT ID_Skill, COUNT(DISTINCT ID_Bo) AS TotalBo FROM TrangThai WHERE ID_User = ? GROUP BY ID_Skill";
        Cursor cursor = db.rawQuery(query, new String[]{idUser});

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int idSkill = cursor.getInt(cursor.getColumnIndex("ID_Skill"));
                    int totalBo = cursor.getInt(cursor.getColumnIndex("TotalBo"));
                    totalBoMap.put(idSkill, totalBo);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("StatusDB Error", "Error getting total bo by skill: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return totalBoMap;
    }

    @SuppressLint("Range")
    public HashMap<Integer, String> getSkills() {
        HashMap<Integer, String> skills = new HashMap<>();
        db = openHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT ID_Skill, Name_Skill FROM Skill";
            cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int idSkill = cursor.getInt(cursor.getColumnIndex("ID_Skill"));
                    String nameSkill = cursor.getString(cursor.getColumnIndex("Name_Skill"));
                    skills.put(idSkill, nameSkill);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("StatusDB Error", "Error getting skills: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return skills;
    }

    @SuppressLint("Range")
    public ArrayList<Integer> getBoList() {
        ArrayList<Integer> boList = new ArrayList<>();
        db = openHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT ID_Bo FROM BoCauHoi";
            cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int idBo = cursor.getInt(cursor.getColumnIndex("ID_Bo"));
                    boList.add(idBo);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("StatusDB Error", "Error getting Bo list: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return boList;
    }



}