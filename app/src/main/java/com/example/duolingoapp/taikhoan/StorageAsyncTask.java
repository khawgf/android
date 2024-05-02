package com.example.duolingoapp.taikhoan;

import android.content.Context;
import android.os.AsyncTask;

public class StorageAsyncTask extends AsyncTask<String, Void, Void> {
    private StorageService storageService;

    public StorageAsyncTask(Context context){
        storageService = new StorageService(null, context);
    }

    @Override
    protected Void doInBackground(String... strings) {
        storageService.assignAccountImage(strings[0]);
        return null;
    }
}
