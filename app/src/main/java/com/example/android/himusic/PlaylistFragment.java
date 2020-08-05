package com.example.android.himusic;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class PlaylistFragment extends Fragment {

    private String TAG="PlaylistFragment";

    private ArrayList<String> userPlayList;
    DatabaseHandler db;
    private ListView playListView;
    private boolean isViewShown = false;

    public PlaylistFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: PlaylistFragment");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: PlaylistFragment");
        View viewPlaylistFragment=  inflater.inflate(R.layout.fragment_playlist, container, false);

        userPlayList=new ArrayList<String>();
        db=new DatabaseHandler(getActivity());
        userPlayList=db.getPlaylistTables();

        ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, userPlayList);
              playListView = (ListView) viewPlaylistFragment.findViewById(R.id.play_list);
              playListView.setAdapter(adapter);


              playListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                  @Override
                  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                      String tablename=userPlayList.get(position);
                      Log.d(TAG, "onItemClick: from playlistFragment"+ tablename +" clicked");
                      ((MainActivity) getActivity()).selectTabText(2,tablename);
                      ((MainActivity) getActivity()).selectFragment(3);
                  }
              });
              playListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                  @Override
                  public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                      AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                      alertDialogBuilder.setMessage("Are you sure, You want to delete this Playlist ?");
                              alertDialogBuilder.setPositiveButton("yes",
                                      new DialogInterface.OnClickListener() {
                                          @Override
                                          public void onClick(DialogInterface arg0, int arg1) {
                                              String tableName= userPlayList.get(position);
                                              if(tableName.equals("LikedSongs")==false) {
                                                  db.deleteTable(tableName);
                                                  userPlayList = db.getPlaylistTables();
                                                  ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, userPlayList);
                                                  playListView.setAdapter(adapter);
                                              }
                                              else {
                                                  Toast.makeText(getActivity(), "Cannot delete Liked Songs folder from database!", Toast.LENGTH_LONG).show();
                                              }
                                          }
                                      });

                      alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialog, int which) {
                              dialog.dismiss();
                          }
                      });

                      AlertDialog alertDialog = alertDialogBuilder.create();
                      alertDialog.show();
                      return true;
                  }
              });

        return viewPlaylistFragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: PlaylistFragment");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: PlaylistFragment");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: PlaylistFragment");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: PlaylistFragment");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: PlaylistFragment");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: PlaylistFragment");
        userPlayList=db.getPlaylistTables();
        ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, userPlayList);
        playListView.setAdapter(adapter);

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: PlaylistFragment");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: PlaylistFragment");
    }
}