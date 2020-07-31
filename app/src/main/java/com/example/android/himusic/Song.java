package com.example.android.himusic;

public class Song {
    private final String TAG="Song";
private String data;
    private long id;
    private String title;
    private String artist;

    public Song() {}

    public Song(long songID, String songTitle, String songArtist, String artData) {
        id=songID;
        title=songTitle;
        artist=songArtist;
        data=artData;
    }


    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public String getData(){return data;}

}
