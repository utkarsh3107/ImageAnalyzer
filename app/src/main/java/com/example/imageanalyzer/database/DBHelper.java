package com.example.imageanalyzer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ImageDB";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_IMAGES = "images";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_SIZE = "size";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_IMAGES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_SIZE + " INTEGER)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);
        onCreate(db);
    }

    public void addImage(String name, long size) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_SIZE, size);
        db.insert(TABLE_IMAGES, null, values);
        db.close();
    }

    public long getImageSize(String name) {
        long size = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_IMAGES, new String[]{COLUMN_SIZE},
                COLUMN_NAME + "=?", new String[]{name}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int colIndex = cursor.getColumnIndex(COLUMN_SIZE);
            if(colIndex >= 0) {
                size = cursor.getLong(colIndex);
            }
            cursor.close();
        }
        db.close();
        return size;
    }
}

