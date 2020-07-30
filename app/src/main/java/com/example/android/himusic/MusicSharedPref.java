package com.example.android.himusic;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class MusicSharedPref {
    Context context;
    private String TAG="MusicSharedPref";

    private static final String LONG_ID_KEY="id";
    private static final String SONG_NAME_KEY="name";
    private static final String ARTIST_NAME_KEY="artist";

    private static final String SCHEDULE_LONG_ID_KEY="id";
    private static final String SCHEDULE_SONG_NAME_KEY="name";
    private static final String SCHEDULE_ARTIST_NAME_KEY="artist";

    private static final String MUSIC_PREF="music";




    private static SharedPreferences eSharedPref;
    private static SharedPreferences.Editor editor;

    private  MusicSharedPref(){}


    public static void setContext(Context context)
    {
        if(eSharedPref== null && editor==null)
        {    eSharedPref= context.getSharedPreferences(MUSIC_PREF, Activity.MODE_PRIVATE);
            editor= eSharedPref.edit();
        }

    }

    public static  void setSongName(String name) {
        editor.putString(SONG_NAME_KEY, name).commit();
    }
    public static  void setArtistName(String artist) {
        editor.putString(ARTIST_NAME_KEY, artist).commit();
    }
    public static  void setLongId(Long id) {
        editor.putLong(LONG_ID_KEY,id).commit();
    }

    public static String getSongName() {
        return eSharedPref.getString(SONG_NAME_KEY, "");
    }
    public static String getArtistName() {
        return eSharedPref.getString(ARTIST_NAME_KEY, "");
    }

    public static Long getLongId() {
        return eSharedPref.getLong(LONG_ID_KEY,0);
    }



    public static  void setScheduleSongName(String name) {
        editor.putString(SCHEDULE_SONG_NAME_KEY, name).commit();
    }
    public static  void setScheduleArtistName(String artist) {
        editor.putString(SCHEDULE_ARTIST_NAME_KEY, artist).commit();
    }
    public static  void setScheduleLongId(Long id) {
        editor.putLong(SCHEDULE_LONG_ID_KEY,id).commit();
    }

    public static String getScheduleSongName() {
        return eSharedPref.getString(SCHEDULE_SONG_NAME_KEY, "");
    }
    public static String getScheduleArtistName() {
        return eSharedPref.getString(SCHEDULE_ARTIST_NAME_KEY, "");
    }
    public static Long getScheduleLongId() {
        return eSharedPref.getLong(SCHEDULE_LONG_ID_KEY,0);
    }

}
