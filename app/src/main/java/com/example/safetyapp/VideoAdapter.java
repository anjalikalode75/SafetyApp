package com.example.safetyapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    Context context;
    List<VideoModel> list;

    public VideoAdapter(Context context, List<VideoModel> list) {
        this.context = context;
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.videoTitle);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_video, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        VideoModel model = list.get(position);

        holder.title.setText(model.getTitle());

        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(context, VideoPlayerActivity.class);
            VideoModel video = null;
            intent.putExtra("url", video.getVideoUrl());
            intent.putExtra("title", video.getTitle()); // 🔥 ADD THIS

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}