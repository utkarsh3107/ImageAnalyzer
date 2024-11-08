package com.example.imageanalyzer.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.imageanalyzer.R;
import com.example.imageanalyzer.adapter.ImageAdapter;
import com.example.imageanalyzer.beans.ImageData;
import com.example.imageanalyzer.database.DBHelper;
import com.example.imageanalyzer.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private EditText searchEditText;

    private DBHelper dbHelper;

    private RecyclerView recyclerView;
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
    }

    private void loadFullGallery(){
        List<ImageData> imageNames = ImageUtils.getAllImageNames(this);

        List<String> imageList = new ArrayList<>();
        for(ImageData eachImage: imageNames){
            imageList.add(eachImage.getImagePath());
        }

        StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setAdapter(new ImageAdapter(this, imageList));
    }

    private void searchImages(String query){
        List<ImageData> objectImages = dbHelper.fetchImageForObjectKeywords(query);

        List<String> imageList = new ArrayList<>();
        for(ImageData eachImage: objectImages){
            imageList.add(eachImage.getImagePath());
        }

        StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setAdapter(new ImageAdapter(this, imageList));

    }
}