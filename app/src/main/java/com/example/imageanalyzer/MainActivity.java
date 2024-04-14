package com.example.imageanalyzer;

import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VIDEO;
import static android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED;

import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.bumptech.glide.Glide;
import com.example.imageanalyzer.beans.ImageData;
import com.example.imageanalyzer.database.DBHelper;
import com.example.imageanalyzer.utils.ImageUtils;
import com.example.imageanalyzer.utils.JSONMapper;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText imageNameEditText;
    private TextView imageSizeTextView;
    private Spinner spinnerAttribute;
    private DBHelper dbHelper;
    private LinearLayout imageContainer;


    private static final int REQUEST_READ_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageNameEditText = findViewById(R.id.imageNameEditText);
        spinnerAttribute = findViewById(R.id.spinnerAttribute);
        imageSizeTextView = findViewById(R.id.imageSizeTextView);
        imageContainer = findViewById(R.id.imageContainer);

        ArrayAdapter<CharSequence> attributeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.attributes_array,
                android.R.layout.simple_spinner_item
        );

        attributeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAttribute.setAdapter(attributeAdapter);

        dbHelper = new DBHelper(this);

        Button readImagesButton = findViewById(R.id.readImagesButton);
        readImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionAndReadImages();
            }
        });

        // Button click event to fetch image size and display
        Button fetchImageButton = findViewById(R.id.fetchImageButton);
        fetchImageButton.setOnClickListener(view -> {
            searchImageMetaData();
        });
    }

    private void searchImageMetaData(){
        String imageName = imageNameEditText.getText().toString().trim();
        String selectedAttribute = spinnerAttribute.getSelectedItem().toString();

        if(imageName.isEmpty() || selectedAttribute.isEmpty()){
            imageSizeTextView.setText("Incorrect values selected");
            displaySelectedImages(null);
            return;
        }
        switch(selectedAttribute){
            case "Object Detection":
                imageSizeTextView.setText("Object Detection in progress");
                displaySelectedImages(null);
                break;
            case "Text Identification":
                imageSizeTextView.setText("Text Identification in progress");
                displaySelectedImages(null);
                break;
            case "Attribute Analysis":
                List<ImageData> imageContext = dbHelper.getImageContext(imageName);
                //imageSizeTextView.setText("Path: " + imageContext + " bytes");
                displaySelectedImages(imageContext);
                break;
            default:
                break;
        }

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

    private void displaySelectedImages(List<ImageData> imageDataList) {
        if(imageDataList == null || imageDataList.isEmpty()){
            Toast.makeText(this, "No images to display", Toast.LENGTH_SHORT).show();
            return;
        }
        for (ImageData eachImage : imageDataList) {
            Uri uri = Uri.parse(eachImage.getImagePath());
            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(0, 0, 0, 16);
            imageView.setLayoutParams(layoutParams);

            Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(imageView);

            imageContainer.addView(imageView);
        }
    }

    private void readImagesFromGalleryAndStoreInDatabase() {

        List<ImageData> imageNames = ImageUtils.getAllImageNames(this);

        for (ImageData imageData : imageNames) {
            long imageSize = ImageUtils.getImageSize(this, imageData.getImageName());
            if (imageSize != -1) {
                dbHelper.addImage(imageData.getImageName(), imageData);
            }
        }

        Toast.makeText(this, "Images stored in database", Toast.LENGTH_SHORT).show();
    }

}