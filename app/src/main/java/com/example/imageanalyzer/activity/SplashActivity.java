package com.example.imageanalyzer.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.imageanalyzer.R;

public class SplashActivity extends AppCompatActivity {

    private ImageView logoImageView;

    private TextView logoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        logoImageView = findViewById(R.id.logoImageView);
        logoTextView = findViewById(R.id.logoTextView);

        ObjectAnimator rotateLeftToRight = ObjectAnimator.ofFloat(logoImageView, "rotationY", 0f, 360f);
        rotateLeftToRight.setDuration(800);
        rotateLeftToRight.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator rotateRightToLeft = ObjectAnimator.ofFloat(logoImageView, "rotationY", 0f, -360f);
        rotateRightToLeft.setDuration(800);
        rotateRightToLeft.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator logoFlipZoomOut = ObjectAnimator.ofPropertyValuesHolder(
                logoImageView,
                android.animation.PropertyValuesHolder.ofFloat("rotationX", 0f, 90f),
                android.animation.PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.0f),
                android.animation.PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.0f),
                android.animation.PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.0f)
        );
        logoFlipZoomOut.setDuration(600);
        logoFlipZoomOut.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator textFlipZoomOut = ObjectAnimator.ofPropertyValuesHolder(
                logoTextView,
                android.animation.PropertyValuesHolder.ofFloat("rotationX", 0f, 90f),
                android.animation.PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.0f),
                android.animation.PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.0f),
                android.animation.PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.0f)
        );
        textFlipZoomOut.setDuration(600);
        textFlipZoomOut.setInterpolator(new AccelerateDecelerateInterpolator());

        AnimatorSet logoAndTextSet = new AnimatorSet();
        logoAndTextSet.playTogether(logoFlipZoomOut, textFlipZoomOut);


        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(rotateLeftToRight, rotateRightToLeft, logoAndTextSet);


        animatorSet.addListener(new android.animation.Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(android.animation.Animator animation) {

            }

            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                Intent intent = new Intent(SplashActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAnimationCancel(android.animation.Animator animation) {
            }

            @Override
            public void onAnimationRepeat(android.animation.Animator animation) {
            }
        });

        // Start the animation set
        animatorSet.start();
    }
}