package com.example.imageanalyzer.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
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

    private ImageView backgroundImage, backImg;
    private LinearLayout roundedBar, detailedInfoSection;
    private AppCompatButton overviewButton;
    private ImageData image;
    private TextView imageDimensions, imageSize,imageDate, objectsIdentified, textIdentified;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details);

        image = (ImageData) getIntent().getSerializableExtra("backendObject");
        if (image == null) {
            Log.i("DetailsActivity", "Got empty imageobject: ");
        }

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Optional: if you need to hide the status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        backgroundImage = findViewById(R.id.background_image);
        roundedBar = findViewById(R.id.rounded_bar);
        overviewButton = findViewById(R.id.overviewButton);
        detailedInfoSection = findViewById(R.id.detailed_info_section);
        imageDimensions = findViewById(R.id.image_dimensions_value);
        imageSize = findViewById(R.id.image_size_value);
        imageDate=  findViewById(R.id.image_date_value);
        objectsIdentified=  findViewById(R.id.objects_identified_value);
        textIdentified =  findViewById(R.id.text_identified_value);
        backImg = findViewById(R.id.backImg);
        RelativeLayout.LayoutParams buttonLayoutParams = (RelativeLayout.LayoutParams) overviewButton.getLayoutParams();

        // Get the image path passed from the previous screen
        String imagePath = image.getImagePath();

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

        backImg.setOnClickListener(v ->finish());

        init(image);
    }

    private void init(ImageData image){

        if(image.getImageHeight() != 0 && image.getImageWidth() != 0){
            imageDimensions.setText(String.format("%d x %d",image.getImageHeight() , image.getImageWidth()));
        }
        if(image.getImageSize() != 0 ){
            imageSize.setText(String.format("%d KB", image.getImageSize()));
        }

        if(image.getImageDateTaken() != 0){
            imageDate.setText(String.format("%d", image.getImageDateTaken()));
        }

        if(image.getObjectsRecognition() != null && !image.getObjectsRecognition().getObjectsDetected().isEmpty()){
            objectsIdentified.setText(String.join(", ", image.getObjectsRecognition().getObjectsDetected().toString()));
        }
    }
}