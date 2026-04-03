package com.example.safetyapp;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class VideoPlayerActivity extends AppCompatActivity {

    WebView webView;
    TextView videoTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        webView = findViewById(R.id.webView);
        videoTitle = findViewById(R.id.videoTitle);

        String videoUrl = getIntent().getStringExtra("url");
        String title = getIntent().getStringExtra("title");

        videoTitle.setText(title);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        // 🔥 EMBED YOUTUBE VIDEO (REAL PLAYER UI)
        String embedHtml =
                "<html><body style='margin:0;padding:0;'>" +
                        "<iframe width='100%' height='100%' " +
                        "src='https://www.youtube.com/embed/" + getVideoId(videoUrl) + "' " +
                        "frameborder='0' allowfullscreen></iframe>" +
                        "</body></html>";

        webView.loadData(embedHtml, "text/html", "utf-8");
    }

    // Extract video ID
    private String getVideoId(String url) {
        return url.split("v=")[1];
    }
}