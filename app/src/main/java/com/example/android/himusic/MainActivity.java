package com.example.android.himusic;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.widget.ListView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class MainActivity extends AppCompatActivity {

    ListView listViewForSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listViewForSongs=findViewById(R.id.song_listView);

        runTimePermission();


    }

    public void runTimePermission()
    {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                 .withListener(new PermissionListener() {
                     @Override
                     public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                     }

                     @Override
                     public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                     }

                     @Override
                     public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                    permissionToken.continuePermissionRequest();
                     }
                 }).check();
    }

}