package com.example.android.himusic;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;


public class SelectedSongsFragment extends Fragment {

    public SelectedSongsFragment() { }
    private  String TAG="SelectedSongsFragment";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: SelectedSongsFragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: SelectedSongsFragment");

        return inflater.inflate(R.layout.fragment_selected_songs, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: SelectedSongsFragment");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: SelectedSongsFragment");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: SelectedSongsFragment");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: SelectedSongsFragment");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: SelectedSongsFragment");
        ((MainActivity) getActivity()).selectTabText(2,"Songs");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: SelectedSongsFragment");

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: SelectedSongsFragment");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: SelectedSongsFragment");
    }
}