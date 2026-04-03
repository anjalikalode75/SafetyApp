package com.example.safetyapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import com.bumptech.glide.Glide;

public class SafetyVideoAdapter extends RecyclerView.Adapter<SafetyVideoAdapter.ViewHolder> {

    private Context context;
    private List<VideoModel> videoList;

    public SafetyVideoAdapter(Context context, List<VideoModel> videoList) {
        this.context = context;
        this.videoList = videoList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        VideoModel video = videoList.get(position);

        holder.txtTitle.setText(video.getTitle());

        // 🔥 GET YOUTUBE THUMBNAIL
        String videoId = video.getVideoUrl().split("v=")[1];
        String thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/0.jpg";

        // 🔥 LOAD IMAGE USING GLIDE
        Glide.with(context)
                .load(thumbnailUrl)
                .into(holder.imgThumbnail);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, VideoPlayerActivity.class);
            intent.putExtra("url", video.getVideoUrl());
            intent.putExtra("title", video.getTitle());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgThumbnail;
        TextView txtTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgThumbnail);
            txtTitle = itemView.findViewById(R.id.txtTitle);
        }
    }
}