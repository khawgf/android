package com.example.duolingoapp.thongbao;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.example.duolingoapp.MainActivity;
import com.example.duolingoapp.R;

import java.util.ArrayList;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String notificationID = "notificationID";
    public static final String channelID = "channel1";
    public static final String titleExtra = "titleExtra";
    public static final String messageExtra = "messageExtra";

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelID)
                .setSmallIcon(R.drawable.bird)
                .setContentTitle(intent.getStringExtra(titleExtra))
                .setContentText(intent.getStringExtra(messageExtra))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(intent.getIntExtra(notificationID, 1), notificationBuilder.build());

        // Remove announced notification
        ArrayList<Item> items = NotificationActivity.itemAdapter.getItems();
        for(int i = 0;i < items.size();i++){
            if(items.get(i).getId() == intent.getIntExtra(notificationID, -1)){
                items.remove(i);
                NotificationActivity.itemAdapter.notifyDataSetChanged();
            }
        }
    }
}
