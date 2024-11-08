package com.example.imageanalyzer.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imageanalyzer.R;
import com.example.imageanalyzer.adapter.ImageAdapter;
import com.example.imageanalyzer.beans.ImageData;
import com.example.imageanalyzer.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
    }

    private void init(){
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        List<ImageData> imageNames = ImageUtils.getAllImageNames(this);

        Log.i("asdasdas", "adadasdas" + imageNames.size());
        List<String> imageList = new ArrayList<>();
        for(ImageData eachImage: imageNames){
            imageList.add(eachImage.getImagePath());
        }
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        recyclerView.setAdapter(new ImageAdapter(this, imageList));
    }
}