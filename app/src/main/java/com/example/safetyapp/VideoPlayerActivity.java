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

        // ✅ IMPORTANT SETTINGS
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        webView.setWebViewClient(new WebViewClient());

        // ✅ GET VIDEO ID
        String videoId = getVideoId(videoUrl);

        // ✅ EMBED PLAYER
        String embedHtml =
                "<html><body style='margin:0;padding:0;'>" +
                        "<iframe width='100%' height='100%' " +
                        "src='https://www.youtube-nocookie.com/embed/" + videoId + "?autoplay=1&controls=1' " +
                        "frameborder='0' allow='autoplay; encrypted-media' allowfullscreen></iframe>" +
                        "</body></html>";

        // 🔥 MAIN FIX (VERY IMPORTANT)
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        webView.loadDataWithBaseURL(
                "https://www.youtube.com",
                embedHtml,
                "text/html",
                "utf-8",
                null
        );
    }

    // ✅ SAFE VIDEO ID METHOD
    private String getVideoId(String url) {
        try {
            if (url.contains("v=")) {
                String id = url.split("v=")[1];
                return id.contains("&") ? id.substring(0, id.indexOf("&")) : id;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}