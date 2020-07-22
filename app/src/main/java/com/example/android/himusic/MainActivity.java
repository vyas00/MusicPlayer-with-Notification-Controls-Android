package com.example.android.himusic;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final String TAG="MainActivity";

    ListView listViewForSongs;
    String[] items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        final ArrayList<File> mySongs= findSong(Environment.getExternalStorageDirectory());
        items= new String[mySongs.size()];

        for(int i=0;i<mySongs.size();i++)
        {
            items[i]= mySongs.get(i).getName().toString().replace(".mp3","")/*.replace(".wav", "")*/;
        }
        ArrayAdapter<String> myAdapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        listViewForSongs.setAdapter(myAdapter);

        listViewForSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String songName= listViewForSongs.getItemAtPosition(position).toString();

                startActivity(new Intent(getApplicationContext(), PlayerActivity.class)
                        .putExtra("songs",mySongs)
                        .putExtra("songName", songName).putExtra("pos", position));


            }
        });
    }


}