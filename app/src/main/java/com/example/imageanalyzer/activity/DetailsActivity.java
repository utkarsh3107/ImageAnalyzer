package com.example.imageanalyzer.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.WindowCompat;

import com.bumptech.glide.Glide;
import com.example.imageanalyzer.R;
import com.example.imageanalyzer.beans.ImageData;
import com.example.imageanalyzer.beans.ObjectsRecognition;
import com.example.imageanalyzer.database.DBHelper;
import com.example.imageanalyzer.search.ImageDataCache;
import com.example.imageanalyzer.service.CustomToast;
import com.example.imageanalyzer.utils.Constants;
import com.example.imageanalyzer.utils.ImageDataManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DetailsActivity extends AppCompatActivity {

    private ImageView backgroundImage, backImg;
    private LinearLayout roundedBar, detailedInfoSection;
    private AppCompatButton overviewButton;
    private ImageData image;
    private TextView imageDimensions, imageSize, imageDate, objectsIdentified, textIdentified;
    private ImageButton editIcon, saveIcon, cancelIcon;
    private RelativeLayout editLayout;
    private EditText objectsEditText;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details);

        image = (ImageData) getIntent().getSerializableExtra("backendObject");
        if (image == null) {
            Log.i(Constants.DETAILS_ACTIVITY, "Got empty image object: ");
        }

        dbHelper = new DBHelper(this);

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
        imageDate = findViewById(R.id.image_date_value);
        objectsIdentified = findViewById(R.id.objects_identified_value);
        textIdentified = findViewById(R.id.text_identified_value);
        backImg = findViewById(R.id.backImg);
        editIcon = findViewById(R.id.edit_icon);
        editLayout = findViewById(R.id.edit_layout);
        saveIcon = findViewById(R.id.save_icon);
        cancelIcon = findViewById(R.id.cancel_icon_edittext);
        objectsEditText = findViewById(R.id.image_dimensions_edit_text);

        String imagePath = image.getImagePath();

        // Set the background image dynamically
        if (imagePath != null) {
            Glide.with(this)
                    .load(imagePath)
                    .into(backgroundImage);
        }

        backImg.setOnClickListener(v -> finish());

        overviewButton.setOnClickListener(view -> {
            RelativeLayout.LayoutParams roundedBarParams = (RelativeLayout.LayoutParams) roundedBar.getLayoutParams();

            if (detailedInfoSection.getVisibility() == View.GONE) {
                detailedInfoSection.setVisibility(View.VISIBLE);

                roundedBarParams.addRule(RelativeLayout.ABOVE, R.id.detailed_info_section);
                roundedBarParams.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                roundedBar.setLayoutParams(roundedBarParams);

            } else {
                detailedInfoSection.setVisibility(View.GONE);
                roundedBarParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                roundedBarParams.removeRule(RelativeLayout.ABOVE);
                roundedBar.setLayoutParams(roundedBarParams);
            }
        });

        editIcon.setOnClickListener(view -> {
            editLayout.setVisibility(View.VISIBLE);
            editIcon.setVisibility(View.GONE);
            objectsIdentified.setVisibility(View.GONE);
            objectsEditText.setText(objectsIdentified.getText());
        });

        saveIcon.setOnClickListener(view -> {
            String updatedObjects = objectsEditText.getText().toString();
            Set<String> updatedSet = new HashSet<>(Arrays.asList(updatedObjects.split(",\\s*")));

            if(image.getObjectsRecognition() == null){
                image.setObjectsRecognition(new ObjectsRecognition());
            }
            objectsIdentified.setText(updatedObjects);
            editLayout.setVisibility(View.GONE);
            editIcon.setVisibility(View.VISIBLE);
            objectsIdentified.setVisibility(View.VISIBLE);

            image.getObjectsRecognition().setObjectsDetected(updatedSet);
            dbHelper.updateImageContext(image);

            ImageDataManager.getInstance().updateImage(image);
            ImageDataCache.markCacheDirty();
            CustomToast.makeText(this, "Objects updated successfully!").show();
        });

        cancelIcon.setOnClickListener(view -> {
            editLayout.setVisibility(View.GONE);
            editIcon.setVisibility(View.VISIBLE);
            objectsIdentified.setVisibility(View.VISIBLE);
            objectsEditText.setText(objectsIdentified.getText());
        });

        init(image);
    }

    private void init(ImageData image) {

        if (image.getImageHeight() != 0 && image.getImageWidth() != 0) {
            imageDimensions.setText(String.format("%d x %d", image.getImageHeight(), image.getImageWidth()));
        }
        if (image.getImageSize() != 0) {
            imageSize.setText(String.format("%d KB", image.getImageSize()));
        }

        if (image.getImageDateTaken() != 0) {
            imageDate.setText(String.format("%d", image.getImageDateTaken()));
        }

        if (image.getObjectsRecognition() != null && image.getObjectsRecognition().getObjectsDetected() != null && !image.getObjectsRecognition().getObjectsDetected().isEmpty()) {
            objectsIdentified.setText(joinStrings(image.getObjectsRecognition().getObjectsDetected()));
        }

        if(image.getImgText() != null && image.getImgText().getImageText() != null && !image.getImgText().getImageText().isEmpty()){
            textIdentified.setText(image.getImgText().getImageText());
        }
    }

    private String joinStrings(Set<String> set) {
        StringBuilder sb = new StringBuilder();
        for (String item : set) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(item);
        }
        return sb.toString();
    }
}
