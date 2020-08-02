package com.example.android.himusic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SongAdapter extends BaseAdapter {
    private final String TAG="SongAdapter";
    Context context;

    private ArrayList<Song> songs;
    private LayoutInflater songInflate;
    DatabaseHandler db;

    public SongAdapter(Context c, ArrayList<Song> theSongs){
        this.context=c;
        songs=theSongs;
        songInflate =LayoutInflater.from(c);
        db=new DatabaseHandler(c);
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {

        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LinearLayout songLay = (LinearLayout) songInflate.inflate(R.layout.song, parent, false);

        TextView songView = (TextView)songLay.findViewById(R.id.tv_song_title);
        TextView artistView = (TextView)songLay.findViewById(R.id.tv_song_artist);
        ImageView songImage= (ImageView) songLay.findViewById(R.id.iv_song_image);
        final CheckBox checkBox= (CheckBox)songLay.findViewById(R.id.cb_playlist);
        final Song currSong = songs.get(position);
        if(db.getSong(currSong.getID())!=null) checkBox.setChecked(true);

        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());
        Bitmap bm= getAlbumImage(currSong.getData());
       if(bm!=null) {songImage.setImageBitmap(getRounded(bm));}
       else {
           songImage.setImageResource(R.drawable.logo_music);
       }

        songLay.setTag(position);

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBox.isChecked())
                {
                    db.addSong(currSong);
                    Toast.makeText(context, currSong.getTitle()+" added to playlist", Toast.LENGTH_LONG).show();
                }
                else{
                    db.deleteSong(currSong.getID());
                    Toast.makeText(context, currSong.getTitle()+" removed from playlist", Toast.LENGTH_LONG).show();
                }

            }
        });
        return songLay;

    }

    private Bitmap getAlbumImage(String path) {
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        byte[] data = mmr.getEmbeddedPicture();
        if (data != null) return BitmapFactory.decodeByteArray(data, 0, data.length);
      return null;
    }


    private Bitmap getRounded(Bitmap mbitmap)
    {
        Bitmap imageRounded=Bitmap.createBitmap(mbitmap.getWidth(), mbitmap.getHeight(), mbitmap.getConfig());
        Canvas canvas=new Canvas(imageRounded);
        Paint mpaint=new Paint();
        mpaint.setAntiAlias(true);
        mpaint.setShader(new BitmapShader(mbitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        canvas.drawRoundRect((new RectF(0, 0, mbitmap.getWidth(), mbitmap.getHeight())), 100, 100, mpaint);
        return  imageRounded;
    }


}