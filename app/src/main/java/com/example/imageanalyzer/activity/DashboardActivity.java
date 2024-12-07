package com.example.imageanalyzer.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.example.imageanalyzer.beans.OverviewActivityPair;
import com.example.imageanalyzer.beans.enums.ModelTypes;
import com.example.imageanalyzer.database.DBHelper;
import com.example.imageanalyzer.search.ImageDataCache;
import com.example.imageanalyzer.service.CustomToast;
import com.example.imageanalyzer.service.MLModelHelper;
import com.example.imageanalyzer.utils.Constants;
import com.example.imageanalyzer.utils.ImageDataManager;
import com.example.imageanalyzer.utils.ImageUtils;
import com.example.imageanalyzer.utils.JSONMapper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private EditText searchEditText;

    private TextView gallerySubHeaderText;

    private DBHelper dbHelper;

    private RecyclerView recyclerView;

    private FloatingActionButton readImagesButton;

    private ImageView toDashboardActivityBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        dbHelper = new DBHelper(this);
        ImageDataCache.refreshCacheIfNeeded(dbHelper);
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
                // No action required for anything before this operation
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();

                if (query.isEmpty()) {
                    // If the search box is cleared, load the full gallery again
                    loadFullGallery();
                } else {
                    // Trigger a backend search for each character entered by the user.
                    searchImages(query);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // No action required for anything after this operation
            }
        });

        toDashboardActivityBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ObjectsOverviewActivity.class);
            startActivity(intent);
        });

        readImagesButton.setOnClickListener(v -> checkPermissionAndReadingGallery());
    }

    private void loadFullGallery(){
        ImageDataCache.refreshCacheIfNeeded(dbHelper);

        List<ImageData> imageNames = dbHelper.fetchImages();

        ImageDataManager.getInstance().setImageDataList(imageNames);
        gallerySubHeaderText.setText(R.string.current_gallery);

        StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setAdapter(new ImageAdapter(this, imageNames));

        List<OverviewActivityPair> overviewData = ImageUtils.getObjectsForScreens(imageNames, 5 ,1);

        RecyclerView recyclerView = findViewById(R.id.overviewImgRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        OverviewImageAdapter overviewAdapter = new OverviewImageAdapter(this, overviewData, searchEditText);
        recyclerView.setAdapter(overviewAdapter);
    }

    private void searchImages(String query){
        ImageDataCache.refreshCacheIfNeeded(dbHelper);

        List<ImageData> objectImages = ImageDataCache.searchImages(query);
        gallerySubHeaderText.setText(R.string.search_results);

        StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new ImageAdapter(this, objectImages));
    }

    private void checkPermissionAndReadingGallery() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.i(Constants.DASHBOARD_ACTIVITY, "Requesting permission for Android 13+");
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_MEDIA_IMAGES},
                        Constants.REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                Log.i(Constants.DASHBOARD_ACTIVITY, "Permission granted already for Android 13+, reading gallery");
                readImagesFromGalleryAndStoreInDatabase();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.i(Constants.DASHBOARD_ACTIVITY, "Requesting permission for Android 12 and below");
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        Constants.REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                Log.i(Constants.DASHBOARD_ACTIVITY, "Permission granted already for Android 12 or less, reading gallery");
                readImagesFromGalleryAndStoreInDatabase();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Constants.REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readImagesFromGalleryAndStoreInDatabase();
            } else {
                CustomToast.makeText(this, "Permission denied. Cannot read images.").show();
            }
        }
    }


    private void readImagesFromGalleryAndStoreInDatabase() {
        List<ImageData> imageNames = ImageUtils.getAllImageNames(this);

        MLModelHelper helper = new MLModelHelper(this, imageNames);
        helper.executeModels(Arrays.asList(ModelTypes.YOLOV5_IITJ, ModelTypes.TESSERACT_ANDROID));

        for (ImageData imageData : imageNames) {
            long imageSize = ImageUtils.getImageSize(this, imageData.getImageName());
            if (imageSize != -1) {
                Log.i(Constants.DASHBOARD_ACTIVITY, "Data collected for image: " + JSONMapper.toJSON(imageData));
                dbHelper.addImage(imageData.getImageName(), imageData);
            }
        }
        ImageDataCache.initializeCache(dbHelper);
        loadFullGallery();
        CustomToast.makeText(this, "Images stored in database!").show();
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
        Log.i(Constants.DASHBOARD_ACTIVITY, "Come to resume");
        ImageAdapter adapter = (ImageAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.updateImageList(ImageDataManager.getInstance().getImageDataList());
        }
        Log.i(Constants.DASHBOARD_ACTIVITY, "ImageAdapter found");
        Intent intent = getIntent();
        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent != null && intent.hasExtra("objectOverviewData")) {
            String objectName = intent.getStringExtra("objectOverviewData");
            if (objectName != null) {
                Log.i(Constants.DASHBOARD_ACTIVITY, "Received object name: " + objectName);
                searchEditText.setText(objectName);
                searchEditText.setSelection(objectName.length()); // Move cursor to the end
            }
        }
    }

}