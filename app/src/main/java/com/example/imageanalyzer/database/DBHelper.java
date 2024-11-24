package com.example.imageanalyzer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.imageanalyzer.beans.ImageData;
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
    private static final int DATABASE_VERSION = 4;
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
        Log.i("Database","Table Recreation Upgrade table:  " + TABLE_IMAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);
        onCreate(db);
    }

    public void addImage(String name, ImageData imageData) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_CONTEXT, JSONMapper.toJSON(imageData));
        db.insertWithOnConflict(TABLE_IMAGES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }

    public List<ImageData> fetchImageForObjectKeywords(String keyword){
        List<ImageData> result = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_IMAGES;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);
        Log.i("Database", "DB Image Size: " + cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
                int colIndex = cursor.getColumnIndex(COLUMN_CONTEXT);
                if(colIndex >= 0) {
                    ImageData imageObj = JSONMapper.toObject(cursor.getString(colIndex),ImageData.class);
                    Log.i("Database", "DB imageObj: " + JSONMapper.toJSON(imageObj));
                    if(imageObj.getObjectsRecognition() != null ){
                        if(imageObj.getObjectsRecognition().getObjectsDetected() != null && !imageObj.getObjectsRecognition().getObjectsDetected().isEmpty()){
                            Set<String> objects = imageObj.getObjectsRecognition().getObjectsDetected();
                            Log.i("Checking","Found objects" + JSONMapper.toJSON(objects));
                            if(objects.contains(keyword)){
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
        Log.i("Database", "DB Image Size: " + cursor.getCount());
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
        Log.i("Database", "DB Image Size: " + cursor.getCount());
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
        Log.i("Updating","Found objects" + JSONMapper.toJSON(imageData));
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CONTEXT, JSONMapper.toJSON(imageData));
        String whereClause = COLUMN_NAME + "=?";
        String[] whereArgs = new String[]{String.valueOf(imageData.getImageName())};

        int rowsAffected = db.update(TABLE_IMAGES, values, whereClause, whereArgs);
        Log.i("SQL Update", "Rows affected: " + rowsAffected);
        if (rowsAffected > 0) {
            Log.i("SQL Update", "Update successful for id: " + imageData.getImageId());
        } else {
            Log.e("SQL Update", "Update failed for id: " + imageData.getImageId());
        }
        db.close();
    }

    public List<ImageData> getTopCommonObjects(int limit) {
        List<ImageData> allImages = fetchImages();
        Map<String, List<ImageData>> objectFrequencyMap = getObjectMap(allImages);

        // Below function first sorts the objectFrequencyMap to fetch all list based on the object count.
        // Then we limit the objects based on input and flatten the list for each top object finally selecting only distinct values.
        List<ImageData> result =  objectFrequencyMap.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .limit(limit)
                .flatMap(entry -> entry.getValue().stream())
                .distinct()
                .collect(Collectors.toList());

        Log.i("Database", "Selecting Top objects: " + JSONMapper.toJSON(result));
        return result;
    }

    private static Map<String, List<ImageData>> getObjectMap(List<ImageData> allImages) {
        Map<String, List<ImageData>> objectFrequencyMap = new HashMap<>();

        for (ImageData image : allImages) {
            if (image.getObjectsRecognition() != null && image.getObjectsRecognition().getObjectsDetected() != null) {
                //Extract object count for eachObject
                for (String object : image.getObjectsRecognition().getObjectsDetected()) {
                    objectFrequencyMap.computeIfAbsent(object, k -> new ArrayList<>());
                    Objects.requireNonNull(objectFrequencyMap.get(object)).add(image);
                }
            }
        }

        Log.i("Database","Total Objects Frequency: " + JSONMapper.toJSON(objectFrequencyMap));
        return objectFrequencyMap;
    }

}
