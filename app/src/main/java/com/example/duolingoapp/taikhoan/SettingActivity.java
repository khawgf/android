package com.example.duolingoapp.taikhoan;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.pm.PermissionInfoCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.duolingoapp.MainActivity;
import com.example.duolingoapp.R;
import com.google.firebase.auth.FirebaseAuth;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SettingActivity extends AppCompatActivity {

    TextView txtBack, txtChangeAvatar, txtDone;

    ImageView imgUser;

    EditText edName, edPassword, edEmail, edPhoneNumber;

    Button btnCancelDelete, btnDeleteAccount, btnConfirmDelete, btnSignOut;

    DatabaseAccess DB;

    User user;

    private final int PICK_IMAGE_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        DB = DatabaseAccess.getInstance(getApplicationContext());

        txtBack = findViewById(R.id.txtBackSetting);
        txtChangeAvatar = findViewById(R.id.txtChangeAvatar);
        imgUser = findViewById(R.id.imgUser_Setting);
        edName = findViewById(R.id.edName_Setting);
        edPassword = findViewById(R.id.edPassword_Setting);
        edEmail = findViewById(R.id.edEmail_Setting);
        edPhoneNumber = findViewById(R.id.edPhoneNumber_Setting);
        txtDone = findViewById(R.id.txtDone);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        btnSignOut = findViewById(R.id.btnSignOut_Setting);

        user = DB.getUser(DB.iduser);

        edName.setText(user.getHoTen());
        edPassword.setText(user.getPassword());
        edEmail.setText(user.getEmail());
        edPhoneNumber.setText(user.getSDT());

        if (user.getImg() == null || user.getImg().length == 0) {
            // Nếu không có dữ liệu, gán hình ảnh mặc định
            imgUser.setImageResource(R.drawable.icon_user);
        } else {
            Bitmap img = BitmapFactory.decodeByteArray(user.getImg(), 0, user.getImg().length);
            imgUser.setImageBitmap(img);
        }

        String screenBack = getIntent().getStringExtra("back");


        //nhấn nút xóa tài khoản
        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hiển thị popup xác nhận xóa tài khoản
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_confirmdelete_account, null);
                // Tạo AlertDialog từ AlertDialog.Builder
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setView(dialogView);

                // Gắn các thành phần của dialog từ dialogView
                btnCancelDelete = dialogView.findViewById(R.id.btnCancelDelete);
                btnConfirmDelete = dialogView.findViewById(R.id.btnConfirmDelete);


                // Xây dựng dialog từ AlertDialog.Builder và gán vào biến alertDialog
                AlertDialog alertDialog = builder.create();
                // Hiển thị dialog khi cần
                alertDialog.show();

                // Bắt sự kiện khi nút "Xóa" được nhấn
                btnConfirmDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Xóa tài khoản từ SQLite, Firebase Authentication và Firebase Realtime Database
                        deleteAccount();
                        // Đóng dialog sau khi xóa thành công
                        alertDialog.dismiss();
                    }
                });

                // Bắt sự kiện khi nút "Hủy" được nhấn
                btnCancelDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Đóng dialog khi người dùng chọn Hủy
                        alertDialog.dismiss();
                    }
                });
            }
        });


        txtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                if (screenBack==null){
                    intent = new Intent(SettingActivity.this, MainActivity.class);
                }else if (screenBack.equals("profile")){
                    intent = new Intent(SettingActivity.this, ProfileActivity.class);
                } else if (screenBack.equals("settingHome")){
                    intent = new Intent(SettingActivity.this, MainActivity.class);
                }
                startActivity(intent);
            }
        });

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("hideLaunchScreen", true);
                startActivity(intent);
                finish();
            }
        });

        txtDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hoTen = edName.getText().toString();
                String sdt = edPhoneNumber.getText().toString();

                // Chuyển đổi hình ảnh từ ImageView thành mảng byte
                BitmapDrawable drawable = (BitmapDrawable) imgUser.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] imgByteArray = stream.toByteArray();

                boolean updateSuccess = DB.updateInfoUser(DB.iduser, hoTen, sdt, imgByteArray);
                if (updateSuccess){
                    Toast.makeText(SettingActivity.this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                    finish();
                    Intent intent = new Intent(SettingActivity.this, ProfileActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(SettingActivity.this, "Cập nhật thông tin thất bại", Toast.LENGTH_SHORT).show();
                }
            }
        });

        txtChangeAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mở Intent để chọn ảnh từ Gallery hoặc File Manager
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Lấy đường dẫn của ảnh từ Intent
            Uri uri = data.getData();

            try {
                // Load ảnh từ đường dẫn và hiển thị lên ImageView
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                imgUser.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void deleteAccount() {
        // Gọi phương thức deleteUser từ SQLite, Firebase Authentication và Firebase Realtime Database
        DB.deleteUser(DB.iduser, new DatabaseAccess.OnUserDeleteListener() {
            @Override
            public void onUserDeleteSuccess() {
                // Xử lý khi xóa tài khoản thành công
                Toast.makeText(SettingActivity.this, "Xóa tài khoản thành công", Toast.LENGTH_SHORT).show();

                FirebaseAuth.getInstance().signOut();
                // Đóng Activity hiện tại để ngăn người dùng quay lại màn hình đã xóa
                Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("hideLaunchScreen", true);
                startActivity(intent);
                finish();
            }

            @Override
            public void onUserDeleteError(String errorMessage) {
                // Xử lý khi xảy ra lỗi khi xóa tài khoản
                Log.d("error delete user", errorMessage);
                Toast.makeText(SettingActivity.this, "Lỗi: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }



    // Phương thức hiển thị dialog xác nhận xóa tài khoản
//    private void showConfirmationDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        // Thêm nút Xác nhận
//        builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                // Xóa tài khoản từ SQLite, Firebase Authentication và Firebase Realtime Database
//                deleteAccount()
//
//                // Chuyển người dùng đến màn hình đăng nhập sau khi xóa tài khoản thành công
//                // Trong trường hợp này, bạn cần chuyển đến màn hình đăng nhập
//                // startActivity(new Intent(YourActivity.this, LoginActivity.class));
//                finish(); // Đóng Activity hiện tại để ngăn người dùng quay lại màn hình cũ
//            }
//        });
//
//        // Thêm nút Hủy
//        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                // Đóng hộp thoại nếu người dùng chọn Hủy
//                dialog.dismiss();
//            }
//        });
//
//        // Hiển thị hộp thoại
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }
}