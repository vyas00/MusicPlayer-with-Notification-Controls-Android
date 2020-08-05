package com.example.android.himusic;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

class CategoryAdapter extends FragmentPagerAdapter {
    Context context;
    int totalTabs;
    public CategoryAdapter (Context c, FragmentManager fm, int totalTabs) {
        super(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
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
                SelectedSongsFragment scheduledFragment = new SelectedSongsFragment();

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
