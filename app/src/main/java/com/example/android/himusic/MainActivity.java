package com.example.android.himusic;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    private final String TAG="MainActivity";

    ListView listViewForSongs;
    String[] items;

    private ArrayList<File> mySongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,"onCreate invoked");

        listViewForSongs=findViewById(R.id.song_listView);

        displaySongs();


    }


    public ArrayList<File> findSong(File file)
    {
        ArrayList<File> arrayList=new ArrayList<>();
        File[] files= file.listFiles();

        for(File singleFile: files)
        {
            if(singleFile.isDirectory() && !singleFile.isHidden())
            {arrayList.addAll(findSong(singleFile));}

            else{
                if(singleFile.getName().endsWith(".mp3") /*|| singleFile.getName().endsWith(".wav")*/)
                {
                    arrayList.add(singleFile);
                }
            }

        }
        return arrayList;
    }

    void displaySongs()
    {
         mySongs= findSong(Environment.getExternalStorageDirectory());
         items= new String[mySongs.size()];

        for(int i=0;i<mySongs.size();i++)
        {
            items[i]= mySongs.get(i).getName().toString().replace(".mp3","").replace(".wav", "");
        }


/*
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if(musicCursor!=null && musicCursor.moveToFirst()){
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            do {
                String thisTitle = musicCursor.getString(titleColumn);
                items.add(thisTitle);
            }
            while (musicCursor.moveToNext());
        }
*/

        ArrayAdapter<String> myAdapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        listViewForSongs.setAdapter(myAdapter);
    }


    void ListItemListener()
    {
        listViewForSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String songName= listViewForSongs.getItemAtPosition(position).toString();

                startActivity(new Intent(getApplicationContext(), PlayerActivity.class)
                        .putExtra("songs",mySongs)
                        .putExtra("songName", songName)
                        .putExtra("pos", position));

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart invoked");
     ListItemListener();
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