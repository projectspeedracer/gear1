package com.projectspeedracer.thefoodapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.adapters.MenuArrayAdapter;
import com.projectspeedracer.thefoodapp.models.Dish;

import java.util.ArrayList;

public class MenuFragment extends Fragment {

    protected ArrayList<Dish> dish;
    protected MenuArrayAdapter aDish;
    protected ListView lvMenuCategory;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){

        ArrayList<Dish> listDish = new ArrayList<>();
        aDish = new MenuArrayAdapter(getActivity(), listDish);

        // Defines the xml file for the fragment
        View view = inflater.inflate(R.layout.fragment_menu_category, container, false);
        lvMenuCategory = (ListView) view.findViewById(R.id.lvMenuCategory);
        lvMenuCategory.setAdapter(aDish);

        return view;
    }

}
