package com.projectspeedracer.thefoodapp.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParseUser;
import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.activities.LoginActivity;
import com.projectspeedracer.thefoodapp.utils.Constants;
import com.projectspeedracer.thefoodapp.utils.FoodAppUtils;

/**
 * Created by avkadam on 4/6/15.
 */
public class PlateRateDialogFragment extends DialogFragment {
    public PlateRateDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    String okayActionMsg;



    public static PlateRateDialogFragment newInstance(String title, String message, String actionMsg) {
        PlateRateDialogFragment frag = new PlateRateDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        args.putString("actionMsg", actionMsg);

        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        String message = getArguments().getString("message");
        String btnMsg = getArguments().getString("actionMsg");
        okayActionMsg = btnMsg;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton(btnMsg,  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // on success
                if (okayActionMsg.equals(getString(R.string.sign_out_dialog_cmd))) {
                    FoodAppUtils.logOutConfirmed(getActivity());
                }
                else {
                    Log.e(Constants.TAG, "Unsupported action in dialog box: "+okayActionMsg);
                    Toast.makeText(getActivity(), "No action taken!!!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return alertDialogBuilder.create();
    }


}