package com.example.imageanalyzer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.imageanalyzer.beans.ImageData;
import com.example.imageanalyzer.beans.OverviewActivityPair;
import com.example.imageanalyzer.utils.Constants;
import com.example.imageanalyzer.utils.ImageUtils;
import com.example.imageanalyzer.utils.JSONMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.stream.Collectors;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ImageDB";
    private static final int DATABASE_VERSION = 5;
    private static final String TABLE_IMAGES = "images";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_CONTEXT = "context";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_IMAGES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT UNIQUE, " +
                COLUMN_CONTEXT + " TEXT)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(Constants.DBHELPER_CLASS,"Table Recreation Upgrade table:  " + TABLE_IMAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);
        onCreate(db);
    }

    public void addImage(String name, ImageData imageData) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_CONTEXT, JSONMapper.toJSON(imageData));

        long rowId = db.insertWithOnConflict(TABLE_IMAGES, null, values, SQLiteDatabase.CONFLICT_IGNORE);

        // If rowId is -1, that means a conflict occurred and no row was inserted
        if (rowId == -1) {
            // Update COLUMN_CONTEXT instead
            ContentValues updateValues = new ContentValues();
            updateValues.put(COLUMN_CONTEXT, JSONMapper.toJSON(imageData));
            String whereClause = COLUMN_NAME + "=?";
            String[] whereArgs = new String[]{name};

            int rowsAffected = db.update(TABLE_IMAGES, updateValues, whereClause, whereArgs);
            Log.i(Constants.DBHELPER_CLASS, "Rows affected: " + rowsAffected);
        }

        db.close();
    }

    public List<ImageData> fetchImageForObjectKeywords(String keyword){
        List<ImageData> result = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_IMAGES;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);
        Log.i(Constants.DBHELPER_CLASS, "DB Image Size: " + cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
                int colIndex = cursor.getColumnIndex(COLUMN_CONTEXT);
                if(colIndex >= 0) {
                    ImageData imageObj = JSONMapper.toObject(cursor.getString(colIndex),ImageData.class);
                    Log.i(Constants.DBHELPER_CLASS, "DB imageObj: " + JSONMapper.toJSON(imageObj));
                    if(imageObj.getObjectsRecognition() != null ){
                        if(imageObj.getObjectsRecognition().getObjectsDetected() != null && !imageObj.getObjectsRecognition().getObjectsDetected().isEmpty()){
                            Set<String> objects = imageObj.getObjectsRecognition().getObjectsDetected();
                            Log.i(Constants.DBHELPER_CLASS,"Found objects" + JSONMapper.toJSON(objects));
                            if(objects.contains(keyword.toLowerCase())){
                                result.add(imageObj);
                            }
                        }
                    }
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return result;
    }

    public List<ImageData> fetchImages(){
        List<ImageData> result = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_IMAGES;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);
        Log.i(Constants.DBHELPER_CLASS, "fetchImages DB Image Size: " + cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
                int colIndex = cursor.getColumnIndex(COLUMN_CONTEXT);
                if(colIndex >= 0) {
                    ImageData imageObj = JSONMapper.toObject(cursor.getString(colIndex),ImageData.class);
                    result.add(imageObj);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return result;
    }

    public List<ImageData> getImageContext(String name) {
        List<ImageData> result = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_IMAGES, new String[]{COLUMN_CONTEXT},
                COLUMN_NAME + "=?", new String[]{name}, null, null, null);
        Log.i(Constants.DBHELPER_CLASS, "getImageContext DB Image Size: " + cursor.getCount());
        if (cursor.moveToFirst()) {
            do{
                int colIndex = cursor.getColumnIndex(COLUMN_CONTEXT);
                if(colIndex >= 0) {
                    result.add(JSONMapper.toObject(cursor.getString(colIndex),ImageData.class));
                }
            }while(cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return result;
    }

    public void updateImageContext(ImageData imageData) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CONTEXT, JSONMapper.toJSON(imageData));
        String whereClause = COLUMN_NAME + "=?";
        String[] whereArgs = new String[]{String.valueOf(imageData.getImageName())};

        int rowsAffected = db.update(TABLE_IMAGES, values, whereClause, whereArgs);
        Log.i(Constants.DBHELPER_CLASS, "updateImageContext Rows affected: " + rowsAffected);
        if (rowsAffected > 0) {
            Log.i(Constants.DBHELPER_CLASS, "updateImageContext Update successful for id: " + imageData.getImageId());
        } else {
            Log.e(Constants.DBHELPER_CLASS, "updateImageContext Update failed for id: " + imageData.getImageId());
        }
        db.close();
    }

    public List<OverviewActivityPair> fetchImage(int objectLimit, int imageLimit){
        return ImageUtils.getObjectsForScreens(fetchImages(), objectLimit , imageLimit);
    }


}
