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

        // ✅ GET VIDEO ID SAFELY
        String videoId = getVideoId(video.getVideoUrl());

        // ✅ LOAD THUMBNAIL
        if (!videoId.isEmpty()) {
            String thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";

            Glide.with(context)
                    .load(thumbnailUrl)
                    .placeholder(android.R.drawable.ic_menu_report_image)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.imgThumbnail);
        } else {
            holder.imgThumbnail.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        // 🔥 FINAL FIX → OPEN IN YOUTUBE APP (100% WORKING)
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(video.getVideoUrl()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    // ✅ STRONG VIDEO ID METHOD
    private String getVideoId(String url) {
        try {
            if (url.contains("v=")) {
                String id = url.split("v=")[1];
                return id.contains("&") ? id.substring(0, id.indexOf("&")) : id;

            } else if (url.contains("youtu.be/")) {
                String id = url.split("youtu.be/")[1];
                return id.contains("?") ? id.substring(0, id.indexOf("?")) : id;

            } else if (url.contains("shorts/")) {
                String id = url.split("shorts/")[1];
                return id.contains("?") ? id.substring(0, id.indexOf("?")) : id;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
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