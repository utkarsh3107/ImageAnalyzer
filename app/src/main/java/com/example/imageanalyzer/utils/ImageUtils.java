package com.example.imageanalyzer.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class ImageUtils {
    public static List<String> getAllImageNames(Context context) {
        List<String> imageNames = new ArrayList<>();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.Media.DISPLAY_NAME };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                imageNames.add(name);
            } while (cursor.moveToNext());
            cursor.close();
        }

        System.out.println(imageNames);
        return imageNames;
    }

    public static long getImageSize(Context context, String imageName) {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.Media.SIZE };
        Cursor cursor = context.getContentResolver().query(uri, projection,
                MediaStore.Images.Media.DISPLAY_NAME + "=?", new String[]{imageName}, null);
        long size = -1;
        if (cursor != null && cursor.moveToFirst()) {
            size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
            cursor.close();
        }
        return size;
    }
}

