package com.example.android.himusic;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.TextView;


public class ControllerFragment extends Fragment implements MediaController.MediaPlayerControl {

    public ControllerFragment() { }
    private final String TAG="ControllerFragment";

    public   MusicController controller;
    private MusicService musicService;
    private MainActivity instanceOfMainactivity;
    private TextView tv_controller;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View viewController=inflater.inflate(R.layout.fragment_controller, container, false);



        tv_controller =viewController.findViewById(R.id.tv_controller);
        instanceOfMainactivity = (MainActivity) getActivity();
        if (instanceOfMainactivity.getInstanceOfService() != null) {
            musicService = instanceOfMainactivity.getInstanceOfService();
        }

        if(musicService==null) Log.d(TAG, "onCreateView of controller fragment: music service is null");
        else Log.d(TAG, "onCreateView of controller fragment: music service is not null");
            setController();


        return viewController;
    }


    public void setController(){
       if(controller==null)  controller = new MusicController(getActivity());
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.playPrev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(tv_controller);
        controller.setEnabled(true);
        controller.show(1000000);
        Log.d(TAG, "setController: called");
    }

    @Override
    public void start() {
        musicService.go();
        musicService.songPlaying=true;
        musicService.startNotification();
        setController();
    }

    @Override
    public void pause() {
        musicService.songPlaying=false;
        musicService.pausePlayer();
        musicService.startNotification();
        setController();
    }

    @Override
    public int getDuration() {
        if(musicService.isPng()) return musicService.getDur();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicService.isPng()) return musicService.getPosn();
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicService.seek(pos);
    }

    @Override
    public boolean isPlaying() {

        return musicService.isPng();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(controller!=null) controller.show();
        Log.d(TAG, "onStart: controller showing");
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ControllerFragment");
        if(controller!=null && controller.isShowing()) controller.hide();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: ControllerFragment");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ControllerFragment");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: ControllerFragment");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: ControllerFragment");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ControllerFragment");
        if(controller!=null && controller.isShowing()){ controller.hide();
            Log.d(TAG, "onPause: controller hide called");}
    }
}