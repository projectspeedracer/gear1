package com.projectspeedracer.thefoodapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.adapters.RatingsAdapter;
import com.projectspeedracer.thefoodapp.models.Rating;

import java.util.ArrayList;

public abstract class AbstractRatingsFragment extends Fragment {
	protected RatingsAdapter ratingsAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_list_posts, container, false);

		setupViews(view);
		// fetchTweets(FIRST_PAGE);

		return view;
	}

    @Override
    public void onResume() {
        super.onResume();
        fetchPosts();
    }

    private void setupViews(View parent) {
		ListView lvPosts = (ListView) parent.findViewById(R.id.lvPosts);
		ArrayList<Rating> lPosts = new ArrayList<>();
		ratingsAdapter = new RatingsAdapter(getActivity(), lPosts);
		lvPosts.setAdapter(ratingsAdapter);
	}

	public abstract void fetchPosts();
}
