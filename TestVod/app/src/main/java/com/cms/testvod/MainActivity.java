package com.cms.testvod;

import androidx.appcompat.app.AppCompatActivity;

import com.cms.ffmpegplayer.FfmpegPlayer;
import com.cms.player.*;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FfmpegPlayer ffmpegPlayer = new FfmpegPlayer();
//        ffmpegPlayer.testFfmpeg("hello", "world");
    }
}
