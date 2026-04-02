package com.example.safetyapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;

public class SafetyVideosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_videos);

        YouTubePlayerView player1 = findViewById(R.id.player1);
        YouTubePlayerView player2 = findViewById(R.id.player2);
        YouTubePlayerView player3 = findViewById(R.id.player3);
        YouTubePlayerView player4 = findViewById(R.id.player4);

        getLifecycle().addObserver(player1);
        getLifecycle().addObserver(player2);
        getLifecycle().addObserver(player3);
        getLifecycle().addObserver(player4);

        // 🔴 Replace with real video IDs
        cueVideo(player1, "6wEjW3B_wPW8K-Q3"); // example
        cueVideo(player2, "Cqc1QlwPDux9A0Is");
        cueVideo(player3, "WcW3uoDu7LNQ9pv0");
        cueVideo(player4, "2vNYWfr4cSj4CwnI");
    }

    private void cueVideo(YouTubePlayerView player1, String s) {
    }

    private void loadVideo(YouTubePlayerView playerView, String videoId) {
        playerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(YouTubePlayer youTubePlayer) {
                youTubePlayer.cueVideo(videoId, 0); // better than loadVideo
            }
        });
    }
}