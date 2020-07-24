package com.example.android.himusic;

import android.content.Context;
import android.view.KeyEvent;
import android.widget.MediaController;

public class MusicController extends MediaController {
    private final String TAG="MediaController";

    Context context;

    public MusicController(Context c){
        super(c);
        this.context = c;
    }

    public void hide(){}

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if(keyCode == KeyEvent.KEYCODE_BACK){
            ((MainActivity) context).onBackPressed();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

}
