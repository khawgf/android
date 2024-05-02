package com.example.duolingoapp.premium;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.duolingoapp.Helper.CreateOrder;
import com.example.duolingoapp.Helper.AppInfo;

import org.json.JSONObject;

import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.listeners.PayOrderListener;

import com.example.duolingoapp.Helper.Helpers;
import com.example.duolingoapp.MainActivity;
import com.example.duolingoapp.R;
import com.example.duolingoapp.bocauhoi.QuestionListActivity;
import com.example.duolingoapp.dienkhuyet.FillBlanksActivity;
import com.example.duolingoapp.luyennghe.ListeningActivity;
import com.example.duolingoapp.taikhoan.DatabaseAccess;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;




public class    PremiumActivity extends AppCompatActivity {

    Button btnPay,btnQR,btnScan;
    DatabaseAccess DB;
    PremiumDB preDB;
    String idUser, nameUser, nameScreen, tenBo_ENG;
    int idBo;
    String amount = "500";

    ImageView qrCodeImageView;

    TextView txtBack;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.premium);

        btnPay = findViewById(R.id.btnStartMyFree2Weeks);
        btnQR = findViewById(R.id.btnCreateQRCode);
        btnScan = findViewById(R.id.btnScan);
        DB = DatabaseAccess.getInstance(getApplicationContext());
        preDB = PremiumDB.getInstance(getApplicationContext());
        qrCodeImageView = findViewById(R.id.qrCodeImageView);
        txtBack = findViewById(R.id.txtBack_Premium);

        Intent intent = getIntent();
        idBo = intent.getIntExtra("idBo", 0);
        nameScreen = intent.getStringExtra("screenCurrent");
        tenBo_ENG = intent.getStringExtra("tenBo_ENG");

        idUser = DB.iduser;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ZaloPaySDK.init(AppInfo.APP_ID, Environment.SANDBOX);

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ID USER: ", idUser);
                Log.d("idBo: ", String.valueOf(idBo));
                CreateOrder orderApi = new CreateOrder();
                try {

                    JSONObject data = orderApi.createOrder(amount, idBo, tenBo_ENG);
                    Log.d("Showdata: ",data.toString());
                    String code = data.getString("returncode");
                    if (code.equals("1")) {
                        String token = data.getString("zptranstoken");
                        Log.d("Token: ",token);

                        // Thực hiện thanh toán thông qua ZaloPay SDK khi người dùng quét mã QR code
                        ZaloPaySDK.getInstance().payOrder(PremiumActivity.this, token, "demozpdk://app", new PayOrderListener() {
                            @Override
                            public void onPaymentSucceeded(final String transactionId, final String transToken, final String appTransID) {
                                Log.d("Payment:", "Thanh tóan thành công");
                                preDB.addPremium(DB.iduser, idBo);
                                Toast.makeText(PremiumActivity.this, "Thanh toán thành công", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(PremiumActivity.this, QuestionListActivity.class);
                                intent.putExtra("screenCurrent", nameScreen);
                                startActivity(intent);
                                finish(); // Kết thúc PremiumActivity
                            }

                            @Override
                            public void onPaymentCanceled(String zpTransToken, String appTransID) {
                                Toast.makeText(PremiumActivity.this, "Thanh toán bị hủy", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransID) {
                                Toast.makeText(PremiumActivity.this, "Thanh toán thất bại", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btnQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createOrderAndGenerateQR();
                Toast.makeText(PremiumActivity.this, "Tạo QR cho bộ " +idBo+ "cho user:" +idUser, Toast.LENGTH_SHORT).show();
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra nếu qrCodeImageView đang hiển thị
                if (qrCodeImageView.getVisibility() == View.VISIBLE) {
                    // Hiển thị nút Pay
                    Toast.makeText(PremiumActivity.this, "Scan thành công", Toast.LENGTH_SHORT).show();
                    btnPay.setVisibility(View.VISIBLE);

                } else {
                    // Ẩn nút Pay
                    btnPay.setVisibility(View.GONE);
                }
            }
        });

        txtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(PremiumActivity.this, QuestionListActivity.class);
                intent.putExtra("screenCurrent", nameScreen);
                startActivity(intent);
            }
        });

    }

    private void createOrderAndGenerateQR() {
        CreateOrder orderApi = new CreateOrder();
        try {
            JSONObject data = orderApi.createOrder(amount, idBo, tenBo_ENG);
            Log.d("Showdata: ",data.toString());
            String code = data.getString("returncode");
            if (code.equals("1")) {
                String orderId = Helpers.getAppTransId();
                String orderurl = data.getString("orderurl");

                // Hiển thị mã QR code
                Bitmap qrCodeBitmap = generateQRCode(orderurl);
                if (qrCodeBitmap != null) {
                    showQRCode(qrCodeBitmap);
                    btnScan.setVisibility(View.VISIBLE);
                    Log.d("OrderID",orderId);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    private Bitmap generateQRCode(String token) {
        // Tạo chuỗi dữ liệu cho mã QR code, trong trường hợp này chỉ cần sử dụng token
        String qrData = token;

        try {
            // Sử dụng MultiFormatWriter để tạo mã QR code với định dạng ZaloPay
            BitMatrix bitMatrix = new MultiFormatWriter().encode(qrData, BarcodeFormat.QR_CODE, 512, 512);

            // Chuyển đổi bitMatrix thành Bitmap
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void showQRCode(Bitmap qrCodeBitmap) {
        // Hiển thị ImageView chứa mã QR code
        qrCodeImageView.setImageBitmap(qrCodeBitmap);
        qrCodeImageView.setVisibility(View.VISIBLE); // Hiển thị ImageView
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }



}
