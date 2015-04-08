package com.projectspeedracer.thefoodapp.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.internal.id;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.TheFoodApplication;
import com.projectspeedracer.thefoodapp.activities.LoginActivity;
import com.projectspeedracer.thefoodapp.activities.PickRestaurantActivity;
import com.projectspeedracer.thefoodapp.fragments.AppDialogFragment;
import com.projectspeedracer.thefoodapp.fragments.PlateRateDialogFragment;
import com.projectspeedracer.thefoodapp.models.Dish;
import com.projectspeedracer.thefoodapp.models.Rating;
import com.projectspeedracer.thefoodapp.models.Restaurant;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by avkadam on 3/28/15.
 */
public class FoodAppUtils {

    public static void LogToast(Context context, String message) {
        Log.e(Constants.TAG, message);
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static String getShortDistance (Float distance) {
        Double distanceMi = distance * Constants.MILES_PER_METER; // converting to float
        distanceMi = Math.round(distanceMi * 100.0) / 100.0;
        String distanceStr = Double.toString(distanceMi);
        String distanceShort;
        String [] splitDistance = distanceStr.split("\\.");
        String integerPart = splitDistance[0];
        String fractionalPart = splitDistance[1];
        if (integerPart.length() > 1) {
            distanceShort = integerPart; // sets 10.02 --> 10
        }
        else if ((!integerPart.equals("0") && (fractionalPart.charAt(0) == '0'))) {
            distanceShort = integerPart; // sets 2.06 --> 2
        }
        else if (fractionalPart.charAt(0) != '0') {
            distanceShort = integerPart+"."+fractionalPart.charAt(0);
            // sets 0.77 --> 0.7
            // sets 2.71 --> 2.7
        }
        else {
            distanceShort = distanceStr; // keeps 0.06 --> 0.06
        }

        return distanceShort;
    }

    public static boolean isGooglePlayServicesAvailable(FragmentActivity activity, int RESULT_CODE, Activity a) {

        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity.getApplicationContext());

        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        }

	    final Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, a, RESULT_CODE);

	    if (errorDialog == null) {
		    return false;
	    }

	    final AppDialogFragment edFragment = new AppDialogFragment();
	    edFragment.setDialog(errorDialog);
	    edFragment.show(activity.getSupportFragmentManager(), "Location Updates");

	    return false;
    }

    public static void lowerEmphasis(Marker marker) {
        final BitmapDescriptor oldMarkerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
        marker.setAlpha(0.5f);
        marker.setIcon(oldMarkerIcon);
    }

    public static void emphasisMarker(Marker marker, Restaurant restaurant) {

	    final boolean inRange = PlacesUtils.IsRestaurantInRange(restaurant, PickRestaurantActivity.mGoogleApiClient);
	    final BitmapDescriptor markerIcon = BitmapDescriptorFactory.defaultMarker(inRange
			    ? BitmapDescriptorFactory.HUE_ORANGE
			    : BitmapDescriptorFactory.HUE_ORANGE);

        float alpha = inRange ? 1 : 0.5f;

	    marker.setIcon(markerIcon);

        marker.setTitle(restaurant.getName());
        marker.showInfoWindow();
        marker.setAlpha(alpha);
    }


    public static void fetchDish(String dishObjectId, GetCallback<Dish> callback) {
        final ParseQuery<Dish> query = ParseQuery.getQuery(Dish.class);
        query.getInBackground(dishObjectId, callback);
    }

    // unused!!
    public static void getAllDishesForRestaurant(int pageNum, FindCallback callback) {

    }

    // Get list of Rating posts for a particular dish - For "Dish Details"
    public static void getAllPostsForDish(final Dish dish, FindCallback callback) {

        ParseRelation<Rating> relationDish = dish.getRelation("DishToPosts");
        ParseQuery query = relationDish.getQuery();

        // include respective User objects
        query.include(Rating.Fields.USER);
        query.include(Rating.Fields.DISH);
        // Recent first
        query.orderByDescending("createdAt");

        //todo: add 7 days constraint !!!
        query.findInBackground(callback);
    }

    // Get list of Rating posts for current Restaurant - For "Restaurant Wall"
    public static void getAllPostsForRestaurant(int pageNum, FindCallback callback) {
        // orderby CreatedAt
        // include repective User objects

        Restaurant restaurant = TheFoodApplication.getCurrentRestaurant();
        if (restaurant == null) {
            Log.e(Constants.TAG, "getAllDishesForRestaurant: Restaurant not selected while getting Dish details");
            return;
        }
        ParseRelation<ParseObject> relationRestaurant = restaurant.getRelation("RestaurantToPosts");
        ParseQuery query = relationRestaurant.getQuery();
        // include respective User objects
        query.include(Rating.Fields.USER);
        query.include(Rating.Fields.DISH);
        //todo: add 7 days constraint !!!

        //for pagination
        query.setLimit(Constants.NUM_ITEMS_PER_QUERY);
        query.setSkip((pageNum - 1) * Constants.NUM_ITEMS_PER_QUERY);

        // Recent first
        query.orderByDescending("createdAt");

        query.findInBackground(callback);
    }

    public static String getRelativeTimeAgo(String rawJsonDate) {
        String outRelativeDate ="";

        //2015-04-03T07:50:39.851Z
        //String parseFormat = "yyyy'-'MM'-'dd'T'HH':'mm':'ss.SSS'Z'";

        // Sat Apr 04 15:18:01 PDT 2015
        String parseFormat = "EEE MMM dd HH:mm:ss ZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(parseFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        String date_info[] = relativeDate.split("\\s+");
        outRelativeDate = relativeDate;

        //override 'outRelativeDate' if it can be shortened further
        if (date_info[0].equals("Yesterday")){
            outRelativeDate = "1d";
        } else if (date_info.length > 1){
            if (date_info[2].equals("ago")) {
                outRelativeDate = date_info[0] + date_info[1].charAt(0);
            }
        }


        return outRelativeDate;
    }

    private static final Random _rnd = new Random();

    public static int GetRandomInt(int min, int max) {
        return _rnd.nextInt((max - min) + 1) + min;
    }

    /**
     * Returns a pseudo-random number between min and max, inclusive.
     * The difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @param min Minimum value
     * @param max Maximum value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see java.util.Random#nextInt(int)
     */
    public static int randInt(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    public static String extractExpressiveMessage(String []availabelMessages, String dishName) {
        String selectedMessage;
        int selectedIndex;
        selectedIndex = randInt(0, availabelMessages.length-1);
        selectedMessage = availabelMessages[selectedIndex];
        return String.format(selectedMessage, dishName);
    }

    public static String getExpressionFromRating(int ratingStar, String dishName) {
        // Harry potter loved guacamole
        // Harry thought guac was awesome!!!

        // Harry says tiramisu is not so good
        // Harry says tiramisu was disappointing...

        // Happy found cake to be okay

        String goodMessage[] = {"loved %s",
                                "%s was awesome!!!"};
        String okayMessage[] = {"found %s to be okay...",
                                "says %s is not bad"};
        String badMessage[] = {"says %s is not so good",
                               "says %s was disappointing"};
        String expressiveMessage = String.format("says %s was okay...", dishName); // catchAll

        if (ratingStar > 2) {
            expressiveMessage = FoodAppUtils.extractExpressiveMessage(goodMessage, dishName);
        }
        else if (ratingStar > 1) {
            expressiveMessage = FoodAppUtils.extractExpressiveMessage(okayMessage, dishName);
        }
        else {
            expressiveMessage = FoodAppUtils.extractExpressiveMessage(badMessage, dishName);
        }

        return expressiveMessage;
    }

    public static void showSignOutDialog(FragmentActivity a) {
        FragmentManager fm = a.getSupportFragmentManager();
        if (ParseUser.getCurrentUser() == null) {
            Log.e(Constants.TAG, "Request to sign out, user not signed in.");
            FoodAppUtils.logOutConfirmed(a);
            return;
        }
        String title = "Signed in as " + ParseUser.getCurrentUser().get("appUserName").toString();
        String message = a.getString(R.string.sign_out_message);
        PlateRateDialogFragment alertDialog =
                PlateRateDialogFragment.newInstance(title, message, a.getString(R.string.sign_out_dialog_cmd));
        alertDialog.show(fm, "fragment_alert");
    }

    public static void showGoodByeDialog(FragmentActivity a) {
        FragmentManager fm = a.getSupportFragmentManager();
        Restaurant restaurant = TheFoodApplication.getCurrentRestaurant();
        if (restaurant == null) {
            Log.e(Constants.TAG, "Request to go out out restaurant, restaurant not selected.");
            FoodAppUtils.logOutConfirmed(a);
            return;
        }

        String title = "Coming out of " + restaurant.getName() + "?";
        String message = a.getString(R.string.go_out_message);
        PlateRateDialogFragment alertDialog =
                PlateRateDialogFragment.newInstance(title, message, a.getString(R.string.go_out_dialog_cmd));
        alertDialog.show(fm, "fragment_alert");
    }

    // called from PlateRateDialogFragment after confirmation from user.
    public static void logOutConfirmed(Activity a) {

        ParseUser.logOut();
        // Start and intent for the dispatch activity
        Intent intent = new Intent(a, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        a.startActivity(intent);
    }

    public static void assignProgressBarStyle(Activity a, ProgressBar pb) {
        pb.getIndeterminateDrawable().setColorFilter(
                a.getResources().getColor(R.color.primary),
                android.graphics.PorterDuff.Mode.SRC_IN);
    }

    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}
