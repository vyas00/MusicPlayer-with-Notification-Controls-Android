package com.example.android.himusic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private String TAG = "DatabaseHandler";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "songsManager.db";

    private static final String TABLE_SONGS = "songs";
    private static final String SONG_NAME = "name";
    private static final String SONG_ID = "id";
    private static final String SONG_ARTIST = "artist";
    private static final String SONG_DATA = "data";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SONGS_TABLE = "CREATE TABLE " + TABLE_SONGS + " (" + SONG_ID + " INTEGER," + SONG_NAME + " TEXT,"  + SONG_ARTIST + " TEXT,"+  SONG_DATA + " TEXT"+ ")";
        db.execSQL(CREATE_SONGS_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);


        onCreate(db);
    }


    void addSong(Song song) {

        Log.d(TAG, "Song is : " + song.getTitle() + "  " + song.getArtist());

        SQLiteDatabase db = this.getWritableDatabase();

        Log.d(TAG, "Song is : " + song.getTitle() + "  " + song.getArtist());
        ContentValues values = new ContentValues();
        values.put(SONG_ID, song.getID());
        values.put(SONG_NAME, song.getTitle());
        values.put(SONG_ARTIST, song.getArtist());
        values.put(SONG_DATA, song.getData());

        // Inserting Row
        db.insert(TABLE_SONGS, null, values);

        db.close();
    }


    public  Song getSong(long its_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_SONGS, new String[]{SONG_ID, SONG_NAME, SONG_ARTIST, SONG_DATA}, SONG_ID + "=?", new String[]{Long.toString(its_id)}, null, null, null, null);
        if(cursor.getCount()<=0){ cursor.close(); return null;}
        if (cursor != null) cursor.moveToFirst();


        Song song = new Song(Long.parseLong(cursor.getString(0)), cursor.getString(1), cursor.getString(2),cursor.getString(3));

        return song;
    }

    public ArrayList<Song> getAllSongs() {
        ArrayList<Song> songList = new ArrayList<Song>();

        String selectQuery = "SELECT  * FROM " + TABLE_SONGS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);


        if (cursor.moveToFirst() && cursor!=null) {
            do {
                Song song = new Song();
                  song.setID(Long.parseLong(cursor.getString(0)));
                  song.setTitle(cursor.getString(1));
                  song.setArtist(cursor.getString(2));
                  song.setData(cursor.getString(3));

                songList.add(song);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return songList;

    }

    public void deleteSong( long its_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SONGS, SONG_ID + " = ?", new String[]{Long.toString(its_id)});
        db.close();
    }


    public void deleteTableSongs() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_SONGS);
    }


    public int getSongsCount() {
        int count = 0;
        String countQuery = "SELECT  * FROM " + TABLE_SONGS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        if (cursor != null && !cursor.isClosed()) {
            count = cursor.getCount();
            cursor.close();
        }
        return count;
    }

}
