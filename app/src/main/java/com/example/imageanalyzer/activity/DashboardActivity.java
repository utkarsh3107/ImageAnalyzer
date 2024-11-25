package com.example.imageanalyzer.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.imageanalyzer.R;
import com.example.imageanalyzer.adapter.ImageAdapter;
import com.example.imageanalyzer.adapter.OverviewImageAdapter;
import com.example.imageanalyzer.beans.ImageData;
import com.example.imageanalyzer.beans.ImageOverviewPair;
import com.example.imageanalyzer.database.DBHelper;
import com.example.imageanalyzer.ml.models.YoloV5Detector;
import com.example.imageanalyzer.utils.ImageDataManager;
import com.example.imageanalyzer.utils.ImageUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private EditText searchEditText;

    private TextView gallerySubHeaderText;

    private DBHelper dbHelper;

    private RecyclerView recyclerView;

    private FloatingActionButton readImagesButton;

    private ImageView toDashboardActivityBtn;

    private static final int REQUEST_READ_EXTERNAL_STORAGE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Optional: if you need to hide the status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        dbHelper = new DBHelper(this);
        recyclerView = findViewById(R.id.recyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        gallerySubHeaderText = findViewById(R.id.gallerySubHeaderText);
        readImagesButton = findViewById(R.id.readImagesButton);
        toDashboardActivityBtn = findViewById(R.id.toDashboardActivityBtn);
        init();
        loadFullGallery();
    }

    private void init(){
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();

                if (query.isEmpty()) {
                    // If the search box is cleared, load the full gallery again
                    loadFullGallery();
                } else {
                    // Trigger a backend search for each character typed
                    searchImages(query);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        toDashboardActivityBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ObjectsOverviewActivity.class);
            startActivity(intent);
        });

        readImages();
    }

    private void loadFullGallery(){
        List<ImageData> imageNames = dbHelper.fetchImages();

        ImageDataManager.getInstance().setImageDataList(imageNames);
        gallerySubHeaderText.setText(R.string.current_gallery);

        StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setAdapter(new ImageAdapter(this, imageNames));

        List<ImageOverviewPair> overiewData = ImageUtils.getTopImagesForTopObjects(imageNames, 5 ,1);

        RecyclerView recyclerView = findViewById(R.id.overviewImgRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        OverviewImageAdapter overviewAdapter = new OverviewImageAdapter(this, overiewData, searchEditText);
        recyclerView.setAdapter(overviewAdapter);
    }

    private void searchImages(String query){
        List<ImageData> objectImages = dbHelper.fetchImageForObjectKeywords(query);
        gallerySubHeaderText.setText(R.string.search_results);

        StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new ImageAdapter(this, objectImages));

    }

    private void readImages(){
        readImagesButton.setOnClickListener(v -> checkPermissionAndReadImages());
    }

    private void checkPermissionAndReadImages() {
        if (ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE")
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{"android.permission.READ_EXTERNAL_STORAGE"},
                    REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            // Permission is already granted, proceed to read images
            readImagesFromGalleryAndStoreInDatabase();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed to read images
                readImagesFromGalleryAndStoreInDatabase();
            } else {
                // Permission denied, show a message or handle accordingly
                Toast.makeText(this, "Permission denied. Cannot read images.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void readImagesFromGalleryAndStoreInDatabase() {

        List<ImageData> imageNames = ImageUtils.getAllImageNames(this);

        for (ImageData imageData : imageNames) {
            try{
                YoloV5Detector objectDetector = new YoloV5Detector(this);
                objectDetector.detectImages(imageData);
                System.out.println(imageData);
                long imageSize = ImageUtils.getImageSize(this, imageData.getImageName());
                if (imageSize != -1) {
                    dbHelper.addImage(imageData.getImageName(), imageData);
                }
            }catch(Exception ex){
                Log.i("MainActivity", "Got exception: " + ex);
            }

        }

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.custom_toast_root));

        TextView toastText = layout.findViewById(R.id.toast_text);
        toastText.setText("Images stored in database!");

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 250);
        toast.show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Update the intent for the current activity
        handleIncomingIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("MainActivity", "Come to resume");
        ImageAdapter adapter = (ImageAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.updateImageList(ImageDataManager.getInstance().getImageDataList());
        }
        Log.i("MainActivity", "ImageAdapter found");
        Intent intent = getIntent();
        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent != null && intent.hasExtra("objectOverviewData")) {
            String objectName = intent.getStringExtra("objectOverviewData");
            if (objectName != null) {
                Log.d("DashboardActivity", "221: Received object name: " + objectName);
                searchEditText.setText(objectName);
                searchEditText.setSelection(objectName.length()); // Move cursor to the end
            }
        }
    }

}