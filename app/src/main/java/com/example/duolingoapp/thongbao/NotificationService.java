package com.example.duolingoapp.thongbao;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class NotificationService {

    Context context;
    public NotificationService(Context context){
        this.context = context;
    }

    public void registerNotification(){
        FirebaseMessaging.getInstance().subscribeToTopic("Event")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed";
                        if (!task.isSuccessful()) {
                            msg = "Subscribe failed";
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
