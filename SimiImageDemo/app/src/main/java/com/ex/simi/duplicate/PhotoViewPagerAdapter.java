package com.ex.simi.duplicate;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by shewenbiao on 18-7-17.
 */

public class PhotoViewPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragmentList;

    public PhotoViewPagerAdapter(FragmentManager fm, List<Fragment> list) {
        super(fm);
        mFragmentList = list;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList == null? 0 : mFragmentList.size();
    }
}
