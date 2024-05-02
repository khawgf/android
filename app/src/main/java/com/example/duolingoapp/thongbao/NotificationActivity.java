package com.example.duolingoapp.thongbao;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duolingoapp.MainActivity;
import com.example.duolingoapp.R;
import com.example.duolingoapp.bocauhoi.QuestionListActivity;
import com.example.duolingoapp.premium.PremiumActivity;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class NotificationActivity extends AppCompatActivity {

    private Button submitButton;
    private TimePicker timePicker;
    private DatePicker datePicker;
    private TextView timeSet, txtBack;
    private RecyclerView recyclerView;
    public static ItemAdapter itemAdapter;
    private ArrayList<Item> listItem;
    public static AlarmManager alarmManager;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        recyclerView = findViewById(R.id.recyclerView);
        listItem = new ArrayList<>();
        itemAdapter = new ItemAdapter(this, listItem);
        recyclerView.setAdapter(itemAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        createNotificationChannel();

        submitButton = findViewById(R.id.submitButton);
        timePicker = findViewById(R.id.timePicker);
        datePicker = findViewById(R.id.datePicker);
        timeSet = findViewById(R.id.timeSet);
        txtBack = findViewById(R.id.txtBack_Notification);


        submitButton.setOnClickListener(view -> {
            if (checkNotificationPermissions(NotificationActivity.this)) {
                scheduleNotification();
            }
        });

        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            String mode = hourOfDay > 12 ? "PM" : "AM";
            String time = hourOfDay + ":" + minute + " " + mode;
            timeSet.setText(time);
            System.out.println("New time: " + hourOfDay + ":" + minute + " " + mode);
        });

        txtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(NotificationActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @SuppressLint({"ShortAlarm", "NotifyDataSetChanged"})
    private void scheduleNotification() {
        int notificationID = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        intent.putExtra(NotificationReceiver.titleExtra, "Lingo");
        intent.putExtra(NotificationReceiver.messageExtra, "Tới giờ học rồi");
        intent.putExtra(NotificationReceiver.notificationID, notificationID);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), notificationID, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        listItem.add(new Item(notificationID, getStringTime(), pendingIntent));
        itemAdapter.notifyDataSetChanged();
        long time = getTime();
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        showAlert(time);
    }

    private void showAlert(long time) {
        Date date = new Date(time);
        DateFormat dateFormat = android.text.format.DateFormat.getLongDateFormat(getApplicationContext());
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());

        new AlertDialog.Builder(this)
                .setTitle("Notification Scheduled")
                .setMessage("Receive remainder message at: " + dateFormat.format(date) + " " + timeFormat.format(date))
                .setPositiveButton("Okay", (dialogInterface, i) -> {})
                .show();
    }

    private long getTime() {
        int minute = timePicker.getMinute();
        int hour = timePicker.getHour();
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute);

        return calendar.getTimeInMillis();
    }

    private String getStringTime(){
        int minute = timePicker.getMinute();
        int hour = timePicker.getHour();
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth()+1;
        int year = datePicker.getYear();

        return day + "/" + month + "/" + year + " - " + hour + ":" + minute;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        String name = "Notify Channel";
        String desc = "A Description of the Channel";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(NotificationReceiver.channelID, name, importance);
        channel.setDescription(desc);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private boolean checkNotificationPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            boolean isEnabled = notificationManager.areNotificationsEnabled();
            if (!isEnabled) {
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                context.startActivity(intent);
                return false;
            }
        } else {
            boolean areEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled();
            if (!areEnabled) {
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                context.startActivity(intent);
                return false;
            }
        }
        return true;
    }
}
