package com.example.imageanalyzer.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
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
    private LinearLayout roundedBar, detailedInfoSection;
    private AppCompatButton overviewButton;


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
        overviewButton = findViewById(R.id.overviewButton);
        detailedInfoSection = findViewById(R.id.detailed_info_section);
        RelativeLayout.LayoutParams buttonLayoutParams = (RelativeLayout.LayoutParams) overviewButton.getLayoutParams();

        // Get the image path passed from the previous screen
        List<ImageData> imageNames = ImageUtils.getAllImageNames(this);
        String imagePath = imageNames.get(0).getImagePath();

        // Set the background image dynamically
        if (imagePath != null) {
            Glide.with(this)
                    .load(imagePath)
                    .into(backgroundImage);
        }


        overviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RelativeLayout.LayoutParams roundedBarParams = (RelativeLayout.LayoutParams) roundedBar.getLayoutParams();
                RelativeLayout.LayoutParams detailedSectionParams = (RelativeLayout.LayoutParams) detailedInfoSection.getLayoutParams();

                if (detailedInfoSection.getVisibility() == View.GONE) {
                    // Show detailed section and move rounded_bar above it
                    detailedInfoSection.setVisibility(View.VISIBLE);

                    // Set rounded_bar above detailed_info_section
                    roundedBarParams.addRule(RelativeLayout.ABOVE, R.id.detailed_info_section);
                    roundedBarParams.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    roundedBar.setLayoutParams(roundedBarParams);

                } else {
                    // Hide detailed section and move rounded_bar to original position
                    detailedInfoSection.setVisibility(View.GONE);

                    // Set rounded_bar at the bottom of the screen
                    roundedBarParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    roundedBarParams.removeRule(RelativeLayout.ABOVE);
                    roundedBar.setLayoutParams(roundedBarParams);
                }
            }
        });
        /*
        overviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (detailedInfoSection.getVisibility() == View.GONE) {
                    // Set detailed section to visible
                    detailedInfoSection.setVisibility(View.VISIBLE);

                    // Move the button higher on the screen when expanded
                    buttonLayoutParams.removeRule(RelativeLayout.ABOVE);
                    buttonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                    buttonLayoutParams.topMargin = 520;  // Adjust the value as needed
                    overviewButton.setLayoutParams(buttonLayoutParams);
                } else {
                    // Set detailed section to gone
                    detailedInfoSection.setVisibility(View.GONE);


                    // Return the button to its original position
                    buttonLayoutParams.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
                    buttonLayoutParams.addRule(RelativeLayout.ABOVE, R.id.rounded_bar);
                    buttonLayoutParams.bottomMargin = -60; // Adjust the value as needed
                    overviewButton.setLayoutParams(buttonLayoutParams);
                }
            }
        });*/

    }
}