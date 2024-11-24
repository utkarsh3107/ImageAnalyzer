package com.example.imageanalyzer.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.imageanalyzer.R;
import com.example.imageanalyzer.activity.DashboardActivity;
import com.example.imageanalyzer.activity.DetailsActivity;
import com.example.imageanalyzer.beans.ImageData;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private List<ImageData> imageList;
    private Context context;

    public ImageAdapter(Context context, List<ImageData> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ImageData object = imageList.get(position);
        String imageUrl = object.getImagePath();

        // Load image using Glide or other image loading library
        Glide.with(context)
                .load(imageUrl)
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), DetailsActivity.class);
            intent.putExtra("backendObject", object);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public void updateImageList(List<ImageData> newImageList) {
        this.imageList = newImageList;
        notifyDataSetChanged();
    }


    public List<String> convertObjToURL(){
        List<String> imageList = new ArrayList<>();
        for(ImageData eachImage: this.imageList){
            imageList.add(eachImage.getImagePath());
        }
        return imageList;
    }

}
