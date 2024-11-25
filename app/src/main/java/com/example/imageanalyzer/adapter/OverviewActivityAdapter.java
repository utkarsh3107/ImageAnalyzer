package com.example.imageanalyzer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.imageanalyzer.R;
import com.example.imageanalyzer.activity.DetailsActivity;
import com.example.imageanalyzer.beans.ImageData;
import com.example.imageanalyzer.beans.ImageOverviewPair;
import com.example.imageanalyzer.beans.OverviewActivityPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OverviewActivityAdapter extends RecyclerView.Adapter<OverviewActivityAdapter.OverviewActivityImageHolder>{

    private List<OverviewActivityPair> imageList;
    private Context context;

    public OverviewActivityAdapter(Context context, List<OverviewActivityPair> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    public static class OverviewActivityImageHolder extends RecyclerView.ViewHolder {
        ImageView leftImageView, rightTopImageView, rightBottomImageView;
        TextView overviewObjectTitle;
        ImageButton overviewExpandButton;

        public OverviewActivityImageHolder(View itemView) {
            super(itemView);
            leftImageView = itemView.findViewById(R.id.leftImageView);
            rightTopImageView = itemView.findViewById(R.id.rightTopImageView);
            rightBottomImageView = itemView.findViewById(R.id.rightBottomImageView);
            overviewObjectTitle = itemView.findViewById(R.id.overviewObjectTitle);
            overviewExpandButton = itemView.findViewById(R.id.overviewExpandButton);
        }
    }

    @NonNull
    @Override
    public OverviewActivityImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.overview_layout, parent, false);
        return new OverviewActivityImageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OverviewActivityImageHolder holder, int position) {
        OverviewActivityPair object =  imageList.get(position);
        String objectName = object.getObjectName();
        List<ImageData> imageDataList = object.getImageList();

        if(!imageDataList.isEmpty()){
            Glide.with(context)
                    .load(imageDataList.get(0).getImagePath())
                    .into(holder.leftImageView);
        }

        if(imageDataList.size() > 1){
            Glide.with(context)
                    .load(imageDataList.get(1).getImagePath())
                    .into(holder.rightTopImageView);
        }else{
            Glide.with(context)
                    .load(R.drawable.placeholder_image)
                    .into(holder.rightTopImageView);
        }

        if (imageDataList.size() > 2) {
            Glide.with(context)
                    .load(imageDataList.get(2).getImagePath())
                    .into(holder.rightBottomImageView);
        }else{
            Glide.with(context)
                    .load(R.drawable.placeholder_image)
                    .into(holder.rightBottomImageView);
        }

        // Set the object title
        holder.overviewObjectTitle.setText(objectName);

        // Set click listener for the plus button
       /* holder.overviewExpandButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra("objectName", objectData.getTitle());
            context.startActivity(intent);
        });


        Glide.with(context)
                .load(imageDataList.get(1).getImagePath())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(holder.rightTopImageView);

        Glide.with(context)
                .load(imageDataList.get(1).getImagePath())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(holder.rightTopImageView);
                */
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

}
