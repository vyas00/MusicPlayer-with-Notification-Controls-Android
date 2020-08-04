package com.example.android.himusic;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

class CategoryAdapter extends FragmentPagerAdapter {
    Context context;
    int totalTabs;
    public CategoryAdapter (Context c, FragmentManager fm, int totalTabs) {
        super(fm);
        context = c;
        this.totalTabs = totalTabs;
    }
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                SongFragment songFragment = new SongFragment();

                return songFragment;
            case 1:
                PlaylistFragment playlistFragment = new PlaylistFragment();

                return playlistFragment;
            case 2:
                ScheduledSongsFragment scheduledFragment = new ScheduledSongsFragment();

                return scheduledFragment;
            default:
                return null;
        }
    }
    @Override
    public int getCount() {
        return totalTabs;
    }
}
