package com.example.android.himusic;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.widget.MediaController;

public class MusicController extends MediaController {
    private final String TAG="MediaController";

    Context context;

    public MusicController(Context c){
        super(c);
        this.context = c;
    }

    @Override
    public void show(int timeout) {
        super.show(timeout);
    }

    @Override
    public void hide() {
    }

    public void hidePermanent()
    {
        super.hide();
    }


    @Override
    public void setMediaPlayer(MediaPlayerControl player) {
        super.setMediaPlayer(player);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    @Override
    public void setAnchorView(View view) {
        super.setAnchorView(view);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if(keyCode == KeyEvent.KEYCODE_BACK){
            hidePermanent();
            ((MainActivity) context).onBackPressed();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

}
