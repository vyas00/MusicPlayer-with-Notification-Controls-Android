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

    public void setID(long its_id){this.id=its_id;}
    public void setTitle(String name){this.title=name;}
    public void setArtist(String its_artist){this.artist=its_artist;}
    public void setData(String its_data){this.data=its_data;}
}
