package com.example.duolingoapp.taikhoan;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class StorageService {

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    DatabaseAccess DB;
    String path;
    Context context;

    public StorageService(String path, Context context){
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        this.context = context;
        this.path = path != null ? path : "image/user";
    }

    public void assignAccountImage(String idUser){
        SQLiteOpenHelper openHelper = new DatabaseOpenHelper(context);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        String filename = idUser;
        StorageReference imageRef = storageReference.child(path + "/" + filename);
        final long FIVE_MEGABYTE = 1024 * 1024 * 5;
        imageRef.getBytes(FIVE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                ContentValues cv = new ContentValues();
                cv.put("Image", bytes);
                db.update("User", cv, "ID_User = ?", new String[]{idUser});
                System.out.println("Get image successful");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("Storage", "Get image failed", exception);
            }
        });
    }

    public void uploadImage(byte[] data, String filename){
        StorageReference imageRef = storageReference.child(path + "/" + filename);
        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                System.out.println("Upload image failed");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                System.out.println("Upload image successfully");
            }
        });
    }
}
