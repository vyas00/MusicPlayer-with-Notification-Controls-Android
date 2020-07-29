package com.example.android.himusic;

import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.MediaController;

public class MusicController extends MediaController {
    private final String TAG="MediaController";

    Context context;

    public MusicController(Context c){
        super(c);
        this.context = c;
    }

/*    public void hide(){ }*/

    @Override
    public void show() {
        super.show(10000);
    }

    @Override
    public void hide() {
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
            hide();
            ((MainActivity) context).onBackPressed();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

}
