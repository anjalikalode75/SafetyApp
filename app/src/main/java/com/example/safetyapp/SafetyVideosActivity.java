package com.example.safetyapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SafetyVideosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<VideoModel> videoList;
    private SafetyVideoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_videos);

        recyclerView = findViewById(R.id.recyclerVideos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        videoList = new ArrayList<>();

        // 🔥 ADD VIDEOS HERE
        videoList.add(new VideoModel(
                "Self Defense for Girls",
                "https://www.youtube.com/watch?v=KVpxP3ZZtAc"
        ));

        videoList.add(new VideoModel(
                "Emergency Safety Tips",
                "https://www.youtube.com/watch?v=8V0b2G6h7rM"
        ));

        videoList.add(new VideoModel(
                "How to Escape Danger",
                "https://www.youtube.com/watch?v=Jw6Q8d0z3Zk"
        ));

        adapter = new SafetyVideoAdapter(this, videoList);
        recyclerView.setAdapter(adapter);
    }
}