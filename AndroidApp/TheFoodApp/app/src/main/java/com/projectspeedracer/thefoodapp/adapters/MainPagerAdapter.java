package com.projectspeedracer.thefoodapp.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.projectspeedracer.thefoodapp.fragments.MenuFragment;

public class MainPagerAdapter extends FragmentPagerAdapter {
    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        if (i == 1) {
            return new MenuFragment();
        } else {
            return new MenuFragment();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int i) {
        if (i == 1) {
            return "Entree";
        } else {
            return "Main Dish";
        }
    }
}
