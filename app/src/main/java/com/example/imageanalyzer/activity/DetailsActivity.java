package com.example.imageanalyzer.activity;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.imageanalyzer.R;
import com.example.imageanalyzer.beans.ImageData;
import com.example.imageanalyzer.utils.ImageUtils;

import java.util.List;

public class DetailsActivity extends AppCompatActivity {

    private ImageView backgroundImage;
    private LinearLayout roundedBar;
    private Button orangeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Optional: if you need to hide the status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        backgroundImage = findViewById(R.id.background_image);
        roundedBar = findViewById(R.id.rounded_bar);
        orangeButton = findViewById(R.id.orange_button);

        // Get the image path passed from the previous screen
        List<ImageData> imageNames = ImageUtils.getAllImageNames(this);
        String imagePath = imageNames.get(0).getImagePath();

        // Set the background image dynamically
        if (imagePath != null) {
            Glide.with(this)
                    .load(imagePath)
                    .into(backgroundImage);
        }

        // Set up button click listener
        orangeButton.setOnClickListener(view -> {
            // Handle button click action
            Toast.makeText(this, "Button Clicked", Toast.LENGTH_SHORT).show();
        });
    }
}