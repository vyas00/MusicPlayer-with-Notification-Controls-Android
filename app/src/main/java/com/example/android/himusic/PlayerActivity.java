package com.example.android.himusic;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class PlayerActivity extends AppCompatActivity {

    Button btnPause;
    Button btnNext;
    Button btnPrevious;
    TextView songName;
    SeekBar songSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        btnNext=findViewById(R.id.btn_next);
        btnPause=findViewById(R.id.btn_pause);
        btnPrevious=findViewById(R.id.btn_previous);
        songSeekBar=findViewById(R.id.seek_bar);
    }
}