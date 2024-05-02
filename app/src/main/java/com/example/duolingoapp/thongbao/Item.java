package com.example.duolingoapp.thongbao;

import android.app.PendingIntent;

public class Item {
    private int id;
    private String title;
    private PendingIntent pendingIntent;

    public Item(int id, String title, PendingIntent pendingIntent) {
        this.id = id;
        this.title = title;
        this.pendingIntent = pendingIntent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public PendingIntent getPendingIntent() {
        return pendingIntent;
    }

    public void setPendingIntent(PendingIntent pendingIntent) {
        this.pendingIntent = pendingIntent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
