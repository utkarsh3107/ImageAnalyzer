package com.example.imageanalyzer.activity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imageanalyzer.R;
import com.example.imageanalyzer.adapter.OverviewActivityAdapter;
import com.example.imageanalyzer.adapter.OverviewImageAdapter;
import com.example.imageanalyzer.beans.ImageData;
import com.example.imageanalyzer.beans.OverviewActivityPair;
import com.example.imageanalyzer.database.DBHelper;
import com.example.imageanalyzer.utils.ImageUtils;

import java.util.List;

public class ObjectsOverviewActivity extends AppCompatActivity {

    private RecyclerView overviewRecyclerView;

    private DBHelper dbHelper;

    private ImageView backImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_objects_overview);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Optional: if you need to hide the status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        backImg = findViewById(R.id.backImg);
        this.dbHelper = new DBHelper(this);

       init();
    }

    private void init(){

        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        List<ImageData> imageNames = dbHelper.fetchImages();
        List<OverviewActivityPair> overviewImgList = ImageUtils.getImagesPerObjectType(imageNames, 3);

        RecyclerView recyclerView = findViewById(R.id.overviewRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        OverviewActivityAdapter overviewAdapter = new OverviewActivityAdapter(this, overviewImgList);
        recyclerView.setAdapter(overviewAdapter);
    }
}