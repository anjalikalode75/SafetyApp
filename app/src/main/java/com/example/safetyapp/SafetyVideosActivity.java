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
        videoList = new ArrayList<>();

        videoList.add(new VideoModel(
                "Self Defense for Girls",
                "https://www.youtube.com/watch?v=KVpxP3ZZtAc"
        ));

        videoList.add(new VideoModel(
                "Emergency Safety Tips",
                "https://youtu.be/Gx3_x6RH1J4?si=F9SLZpSGw5k01GpB"
        ));

        videoList.add(new VideoModel(
                "Basic Self Defense Moves",
                "https://youtu.be/M4_8PoRQP8w?si=luVz3VM65N7EWfza"
        ));

        videoList.add(new VideoModel(
                "SELF DEFENSE MOVES EVERY WOMAN SHOULD KNOW",
                "https://youtu.be/k9Jn0eP-ZVg?si=TrwdvyBevA1vBzIp"
        ));

        videoList.add(new VideoModel(
                "7 Self-Defense Techniques for Women from Professionals",
                "https://youtu.be/T7aNSRoDCmg?si=WVim0r6AgVTuwI1S"
        ));
        videoList.add(new VideoModel(
                "3 Strikes Every Woman Should Know",
                "https://youtu.be/vQhHlk4KJoo?si=wBtfdM4jXbglnVDq"
        ));
        videoList.add(new VideoModel(
                "Women's Self-defense That Actually Works!",
                "https://youtu.be/pndPbpHLpos?si=UOp3aOtqYdkDhHnL"
        ));

        adapter = new SafetyVideoAdapter(this, videoList);
        recyclerView.setAdapter(adapter);
    }
}