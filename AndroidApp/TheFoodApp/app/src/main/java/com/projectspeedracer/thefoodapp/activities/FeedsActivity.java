package com.projectspeedracer.thefoodapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.adapters.RestaurantAdapter;
import com.projectspeedracer.thefoodapp.models.Restaurant;

import java.util.ArrayList;
import java.util.List;

public class FeedsActivity extends ActionBarActivity {

    List<Restaurant> restaurants = new ArrayList<Restaurant>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeds);
        ListView lvRestaurants = (ListView) findViewById(R.id.lvRestaurants);
        // Lorem ipsuming the restaurant list
        Restaurant r = new Restaurant();
        r.setName("True Normand");
        r.setAddress("140 New Montgomery St, San Francisco, CA 94105");
        r.setPhotoUrl("https://cdn1.vox-cdn.com/thumbor/DlndxVgDozxAhQlpxLbUZi0CBaM=/0x59:624x410/350x197/cdn0.vox-cdn.com/uploads/chorus_image/image/44365122/Screen_Shot_2014-12-18_at_11.25.21_AM.0.0.png");
        r.setRating(4.5f);
        restaurants.add(r);
        r = new Restaurant();
        r.setName("Lorem Ipsum Trattoria");
        r.setAddress("200 Dolor Sit Amet, Lorem Ipsum, LI");
        r.setPhotoUrl("http://1.bp.blogspot.com/-IUNKJxWXI1Y/VIPTxwNDDxI/AAAAAAABF_E/rJ9YslS76g0/s1600/DSCF3818-001.jpg");
        r.setRating(3.9f);
        restaurants.add(r);
        lvRestaurants.setAdapter(new RestaurantAdapter(this,restaurants, 0));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feeds, menu);

        menu.findItem(R.id.action_settings).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(FeedsActivity.this, SettingsActivity.class));
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
