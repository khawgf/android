package com.example.duolingoapp.taikhoan;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.duolingoapp.trangthai.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Map;

public class DatabaseAccess {
    private SQLiteOpenHelper openHelper;
    SQLiteDatabase db;
    FirebaseDatabase rootNode; //f_instanse
    DatabaseReference userref; //f_db
    StorageService storageService;
    private Context context;
    private  static  DatabaseAccess instance;
    Cursor c= null;
    public  String iduser;
    Map<String,String> user; // Map lưu dữ liệu dạng String : String --> Hoten: Thien
    Map<String,Long> diem; //Firebase sử dụng kiểu Long thay vì Int

    private OnEmailCheckListener onEmailCheckListener;

    public interface OnEmailCheckListener {
        void onEmailCheck(boolean emailExists, String idUser);
    }

    // Constructor để truyền listener vào
    public DatabaseAccess(OnEmailCheckListener listener) {
        this.onEmailCheckListener = listener;
    }

    // Phương thức để thiết lập listener
    public void setOnEmailCheckListener(OnEmailCheckListener listener) {
        this.onEmailCheckListener = listener;
    }

    private DatabaseAccess(Context context){
        this.context = context;
        this.openHelper = new DatabaseOpenHelper(context);
        this.storageService = new StorageService(null, context);
    }

    public  static  DatabaseAccess getInstance(Context context){
        if(instance==null){
            instance = new DatabaseAccess(context);
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
    public Boolean insertData(String iduser,String hoten, String email,String sdt, String password)
    {
        db = openHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("ID_User",iduser);
        contentValues.put("HoTen",hoten);
        contentValues.put("Email",email);
        contentValues.put("SDT",sdt);
        contentValues.put("Password",password);
        contentValues.put("Network", 1);
        long result = db.insert("User",null,contentValues);
        if(result==-1) {
            return false;
        }
        else {
            return true;
        }

    }
    public Boolean checktaikhoan(String email)
    {
        db = openHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from User where Email = ?", new String[]{email});
        if(cursor.getCount() >0)
        {
            return  true;
        }
        else
        {
            return false;
        }

    }


      // Hàm kiểm tra xem email đã tồn tại trong Realtime Firebase hay không
    public void checkEmailByFirebase(String email) {
        rootNode = FirebaseDatabase.getInstance();
        userref = rootNode.getReference("User");

        Query query = userref.orderByChild("email").equalTo(email);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Email tồn tại, lấy iduser và gọi phương thức của listener để trả về kết quả
                    String idUser = dataSnapshot.getChildren().iterator().next().getKey();
                    if (onEmailCheckListener != null) {
                        onEmailCheckListener.onEmailCheck(true, idUser);
                    }
                } else {
                    // Email không tồn tại, gọi phương thức của listener để trả về kết quả
                    if (onEmailCheckListener != null) {
                        onEmailCheckListener.onEmailCheck(false, null);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("lỗi", "checkEmailByFirebase");
            }
        });
    }

    public void CapNhatUser(String iduser) {

        //Kiểm tra xem dữ liệu đã có trong SQLite chưa
        db = openHelper.getWritableDatabase();
        //Lấy dữ liệu từ Firebase xuống
        rootNode = FirebaseDatabase.getInstance();
        userref = rootNode.getReference("User").child(iduser);
        Cursor cursor = db.rawQuery("Select * from User where ID_User = ?", new String[]{iduser});
        if(cursor.getCount() >0)
        {
            //Cập Nhật User từ FireBase
            //TH1: Đã có dữ liệu ở SQLite
            userref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    user = (Map<String, String>) dataSnapshot.getValue();
                    diem = (Map<String, Long>) dataSnapshot.getValue();

                    db = openHelper.getWritableDatabase();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("HoTen",user.get("hoTen"));
                    contentValues.put("SDT",user.get("sdt"));
                    //db.rawQuery("Select * from User where ID_User = ?", new String[]{iduser});
                    db.update("User",contentValues,"ID_User = ?", new String[]{iduser});
                }

                @Override
                public void onCancelled(DatabaseError error) {

                }
            });
        }
        else
        {
            //Cập Nhật User từ FireBase
            //TH2: Chưa có dữ liệu ở SQLite
            rootNode = FirebaseDatabase.getInstance();
            userref = rootNode.getReference("User").child(iduser);

            userref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    user = (Map<String, String>) dataSnapshot.getValue();
                    diem = (Map<String, Long>) dataSnapshot.getValue();

                    db = openHelper.getWritableDatabase();

                    ContentValues contentValues = new ContentValues();
                    contentValues.put("ID_User",iduser);
                    contentValues.put("HoTen",user.get("hoTen"));
                    contentValues.put("Email",user.get("email"));
                    contentValues.put("SDT",user.get("sdt"));
                    contentValues.put("Password",user.get("password"));
                    contentValues.put("Network",1);
                    db.insert("User",null,contentValues);
                }

                @Override
                public void onCancelled(DatabaseError error) {

                }
            });
        }
    }

    public void upgradeImage(String iduser) {
        new StorageAsyncTask(context).execute(iduser);
    }

    public boolean updateInfoUser(String iduser, String hoten,String sdt, byte[] img){
        rootNode = FirebaseDatabase.getInstance();
        userref = rootNode.getReference("User").child(iduser);

        userref.child("hoTen").setValue(hoten);
        userref.child("sdt").setValue(sdt);
        // Cập nhật hình ảnh trong Firebase Realtime Database
        // Ở đây, bạn có thể lưu hình ảnh dưới dạng Base64 String hoặc một URL đến hình ảnh trong Storage

        db = openHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("HoTen",hoten);
        contentValues.put("SDT",sdt);
        // Chuyển đổi hình ảnh thành dạng Blob để lưu trong SQLite
        contentValues.put("Image", img);
        Cursor cursor = db.rawQuery("Select * from User where ID_User = ?", new String[]{iduser});
        if(cursor.getCount()>0) {
            long result = db.update("User",contentValues,"ID_User = ?", new String[]{iduser});
            if(result==-1) {
                return false;
            }
            else {
                // Upload image to firebase storage
                storageService.uploadImage(img, iduser);

                // Cập nhật thông tin người dùng trên Firebase Authentication
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(hoten) // Cập nhật tên hiển thị
                            // Bạn cũng có thể cập nhật ảnh đại diện bằng cách sử dụng .setPhotoUri(uri)
                            .build();

                    currentUser.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("UpdateInfoUser", "User profile updated.");
                                    } else {
                                        Log.e("UpdateInfoUser", "Failed to update user profile.", task.getException());
                                    }
                                }
                            });
                }

                return true;
            }
        }
        else {
            return  false;
        }
    }

    // Phương thức kiểm tra đăng nhập bằng SQLite
    public boolean checkLoginSQLite(String email, String password) {
        SQLiteDatabase db = openHelper.getReadableDatabase();

        // Thiết lập câu truy vấn
        String query = "SELECT * FROM User WHERE Email = ? AND Password = ?";

        // Thực hiện truy vấn
        Cursor cursor = db.rawQuery(query, new String[]{email, password});

        // Kiểm tra xem có dòng dữ liệu nào trả về không
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToNext();
            iduser = cursor.getString(0);
            cursor.close();
            return true; // Đăng nhập thành công
        } else {
            cursor.close();
            return false; // Đăng nhập không thành công
        }
    }

    public User getUser(String idUser) {
        SQLiteDatabase db = openHelper.getReadableDatabase();

        // Thiết lập câu truy vấn
        String query = "SELECT * FROM User WHERE ID_User = ? ";

        // Thực hiện truy vấn
        Cursor cursor = db.rawQuery(query, new String[]{idUser});

        // Kiểm tra xem có dòng dữ liệu nào trả về không
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToNext();
            String iduser = cursor.getString(0);
            String hoTen = cursor.getString(1);
            String email = cursor.getString(2);
            String password = cursor.getString(3);
            String sdt = cursor.getString(4);
            byte[] img = cursor.getBlob(5);
            int network = cursor.getInt(6);
            User user = new User(iduser, hoTen, email, password, sdt, img, network);
            return user;
        } else {
            cursor.close();
            return null;
        }
    }

    @SuppressLint("Range")
    public ArrayList<User> getUserListFromSQLite() {
        ArrayList<User> userList = new ArrayList<>();
        db = openHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT * FROM User", null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String iduser = cursor.getString(0);
                    String hoTen = cursor.getString(1);
                    String email = cursor.getString(2);
                    String password = cursor.getString(3);
                    String sdt = cursor.getString(4);
                    byte[] img = cursor.getBlob(5);
                    int network = cursor.getInt(6);
                    User user = new User(iduser, hoTen, email, password, sdt, img, network);
                    userList.add(user);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("StatusDB Error", "Error getting status list from SQLite: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return userList;
    }

    public void getListUser(final OnUserListListener userListListener) {
        rootNode = FirebaseDatabase.getInstance();
        userref = rootNode.getReference("User");

        userref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<User> userList = new ArrayList<>();

                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String iduser = snapshot.child("iduser").getValue(String.class);
                        String hoTen = snapshot.child("hoTen").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String password = snapshot.child("password").getValue(String.class);
                        String sdt = snapshot.child("SDT").getValue(String.class);
                        User user = new User(iduser, hoTen, email, password, sdt);
                        userList.add(user);
                    }
                    userListListener.onUserListRetrieved(userList);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                userListListener.onUserListError(databaseError.getMessage());
            }
        });
    }

    public interface OnUserListListener {
        void onUserListRetrieved(ArrayList<User> userList);
        void onUserListError(String errorMessage);
    }

    public void deleteUser(final String idUser, final OnUserDeleteListener listener) {
        // Xóa tài khoản người dùng trên SQLite
        db = openHelper.getWritableDatabase();
        int deletedRows = db.delete("User", "ID_User = ?", new String[]{idUser});
        if (deletedRows > 0) {
            // Tài khoản đã được xóa thành công trên SQLite
            deleteUserFromRealtime(idUser, listener);
        } else {
            // Xảy ra lỗi khi xóa tài khoản trên SQLite
            if (listener != null) {
                listener.onUserDeleteError("Error deleting user data from SQLite.");
            }
        }
    }

    private void deleteUserFromRealtime(final String idUser, final OnUserDeleteListener listener) {
        // Xóa tài khoản người dùng trên Realtime Database
        rootNode = FirebaseDatabase.getInstance();
        userref = rootNode.getReference("User").child(idUser);
        userref.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    // Tài khoản đã được xóa thành công trên Realtime Database
                    // Xóa TrangThai người dùng trên Realtime Database
                    rootNode = FirebaseDatabase.getInstance();
                    DatabaseReference statusRef = rootNode.getReference("TrangThai");

                    // Tạo một truy vấn để lấy tất cả các bản ghi có idUser tương ứng
                    Query query = statusRef.orderByChild("idUser").equalTo(idUser);

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Duyệt qua tất cả các bản ghi có idUser tương ứng và xóa chúng
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                snapshot.getRef().removeValue();
                            }
                            deleteUserFromAuthen(idUser, listener);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("DeleteStatusError", "Error deleting status: " + databaseError.getMessage());
                            // Xảy ra lỗi khi xóa TrangThai trên Realtime Database
                            if (listener != null) {
                                listener.onUserDeleteError(databaseError.getMessage());
                            }
                        }
                    });

                } else {
                    // Xảy ra lỗi khi xóa tài khoản trên Realtime Database
                    if (listener != null) {
                        listener.onUserDeleteError(error.getMessage());
                    }
                }
            }
        });
    }


    private void deleteUserFromAuthen(final String idUser, final OnUserDeleteListener listener) {
        // Xóa tài khoản người dùng trên Firebase Authentication
        FirebaseAuth.getInstance().getCurrentUser().delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Tài khoản đã được xóa thành công trên Firebase Authentication
                            // Tiến hành xóa tài khoản trên Realtime Database và SQLite
                            if (listener != null) {
                                listener.onUserDeleteSuccess();
                            }
                        } else {
                            // Xảy ra lỗi khi xóa tài khoản trên Firebase Authentication
                            if (listener != null) {
                                listener.onUserDeleteError(task.getException().getMessage());
                            }
                        }
                    }
                });
    }

    public interface OnUserDeleteListener {
        void onUserDeleteSuccess();
        void onUserDeleteError(String errorMessage);
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

}
