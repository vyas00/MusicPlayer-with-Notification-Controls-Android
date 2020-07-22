package com.example.android.himusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    Button btnPause;
    Button btnNext;
    Button btnPrevious;
    TextView tvSongName;
    SeekBar songSeekBar;
    private  final  String TAG="PlayerActivity";

  private static MediaPlayer myMediaPlayer;
    int position;
    String sName;
    ArrayList<File> mySongs;

    Thread updateSeekBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        Log.d(TAG,"onCreated invoked");

        btnNext=findViewById(R.id.btn_next);
        btnPause=findViewById(R.id.btn_pause);
        btnPrevious=findViewById(R.id.btn_previous);
        songSeekBar=findViewById(R.id.seek_bar);
        tvSongName =findViewById(R.id.current_song);

        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent= getIntent();
        Bundle bundle= intent.getExtras();

        mySongs= (ArrayList) bundle.getParcelableArrayList("songs");
        sName=mySongs.get(position).getName().toString();

        String songName= intent.getStringExtra("songName");

        tvSongName.setText(songName);
        tvSongName.setSelected(true);

        position=bundle.getInt("pos");


        if(myMediaPlayer!=null)
        {
            myMediaPlayer.stop();
            myMediaPlayer.release();
        }



        updateSeekBar=new Thread()
        {
            @Override
            public void run() {
                super.run();
                int totalDuration= myMediaPlayer.getDuration();
                int currentPositon=0;

                while(currentPositon<totalDuration)
                {
                    try {
                        sleep(500);
                        currentPositon=myMediaPlayer.getCurrentPosition();
                        songSeekBar.setProgress(currentPositon);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        };


        Uri uriSong= Uri.parse(mySongs.get(position).toString());
        myMediaPlayer= MediaPlayer.create(getApplicationContext(), uriSong);
        myMediaPlayer.start();

        songSeekBar.setMax(myMediaPlayer.getDuration());

        updateSeekBar.start();

        songSeekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        songSeekBar.getThumb().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);


        songSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                myMediaPlayer.seekTo(seekBar.getProgress());
            }
        });


        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                songSeekBar.setMax(myMediaPlayer.getDuration());

                if(myMediaPlayer.isPlaying())
                {
                    btnPause.setBackgroundResource(R.drawable.icon_play);
                    myMediaPlayer.pause();
                }
                else{
                    btnPause.setBackgroundResource(R.drawable.icon_pause);
                    myMediaPlayer.start();
                }
            }
        });



        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMediaPlayer.stop();
                myMediaPlayer.release();
                position=((position+1)%mySongs.size());

                Uri uriSong= Uri.parse(mySongs.get(position).toString());
                myMediaPlayer=MediaPlayer.create(getApplicationContext(), uriSong);

                sName=mySongs.get(position).getName().toString();
                tvSongName.setText(sName);

                myMediaPlayer.start();
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMediaPlayer.stop();
                myMediaPlayer.release();
                position=((position-1)<0) ?(mySongs.size()-1):(position-1);

                Uri uriSong= Uri.parse(mySongs.get(position).toString());
                myMediaPlayer=MediaPlayer.create(getApplicationContext(), uriSong);

                sName=mySongs.get(position).getName().toString();
                tvSongName.setText(sName);
                myMediaPlayer.start();
            }
        });


    }

/*
    @Override
    public void onBackPressed() {
        startActivity(new Intent(PlayerActivity.this, MainActivity.class));
    }
*/

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==android.R.id.home) onBackPressed();
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onStart invoked");

    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume invoked");
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"onPause invoked");
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStop invoked");
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG,"onRestart invoked");


    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy invoked");
    }
}