package com.projectspeedracer.thefoodapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.adapters.PostsAdapter;
import com.projectspeedracer.thefoodapp.models.Rating;

import java.util.ArrayList;


// Show posts
public class PostsListFragment extends Fragment {
    ListView lvPosts;
    PostsAdapter postsAdapter;
    ArrayList<Rating> lPosts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v  = inflater.inflate(R.layout.fragment_list_posts, container, false);

        setupViews(v);

//        fetchTweets(FIRST_PAGE);

        fetchPosts();
        return v;
    }

    private void setupViews(View parent) {
        lvPosts = (ListView) parent.findViewById(R.id.lvPosts);
        lPosts = new ArrayList();
        postsAdapter = new PostsAdapter(getActivity(), lPosts);
        lvPosts.setAdapter(postsAdapter);
    }

    // Override for specifics
    public void fetchPosts() {
        Log.e("NOTHING", "should not hit this");
        // caller should override this.
    }
}
