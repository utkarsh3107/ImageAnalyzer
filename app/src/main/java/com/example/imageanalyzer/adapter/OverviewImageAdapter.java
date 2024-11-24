package com.example.imageanalyzer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.imageanalyzer.R;
import com.example.imageanalyzer.activity.DetailsActivity;
import com.example.imageanalyzer.beans.ImageData;
import com.example.imageanalyzer.beans.ImageOverviewPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OverviewImageAdapter extends RecyclerView.Adapter<OverviewImageAdapter.OverviewImageHolder>{

    private List<ImageOverviewPair> imageList;
    private Context context;


    public OverviewImageAdapter(Context context, List<ImageOverviewPair> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    public static class OverviewImageHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        TextView textView;
        public OverviewImageHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.overviewImage);
            textView = itemView.findViewById(R.id.overviewText);
        }
    }

    @NonNull
    @Override
    public OverviewImageAdapter.OverviewImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.overview_image, parent, false);
        return new OverviewImageAdapter.OverviewImageHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull OverviewImageAdapter.OverviewImageHolder holder, int position) {
        ImageOverviewPair object = imageList.get(position);
        String imageUrl = object.getImageData().getImagePath();
        String objectName = object.getObjectName();

        // Load image using Glide or other image loading library
        Glide.with(context)
                .load(imageUrl)
                .into(holder.imageView);
        Objects.requireNonNull(holder).textView.setText(objectName.substring(0, 1).toUpperCase() + objectName.substring(1).toLowerCase());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), DetailsActivity.class);
            intent.putExtra("overviewObject", object.getImageData());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public void updateImageList(List<ImageOverviewPair> newImageList) {
        this.imageList = newImageList;
        notifyDataSetChanged();
    }


    public List<String> convertObjToURL(){
        List<String> imageList = new ArrayList<>();
        for(ImageOverviewPair eachImage: this.imageList){
            imageList.add(eachImage.getImageData().getImagePath());
        }
        return imageList;
    }

}
