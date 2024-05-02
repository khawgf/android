package com.example.duolingoapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;

import com.example.duolingoapp.trangthai.StatusDB;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        String status = getConnectivityStatusString(context);
        Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
    }

    private String getConnectivityStatusString(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            // Kiểm tra trạng thái kết nối mạng
            android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
//                StatusDB db = StatusDB.getInstance(context);
//                db.syncUnsyncedRecordsToFirebase();
                return "Connected";
            } else {
                return "Disconnected";
            }
        }
        return "Unknown";
    }

}