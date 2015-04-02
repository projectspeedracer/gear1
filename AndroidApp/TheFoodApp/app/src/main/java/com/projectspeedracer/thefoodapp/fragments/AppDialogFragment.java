package com.projectspeedracer.thefoodapp.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

public class AppDialogFragment extends DialogFragment {
	private Dialog dlg;

	public AppDialogFragment() {
		super();
		setDialog(null);
	}

	@Override @NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return dlg;
	}

	public void setDialog(Dialog dlg) {
		this.dlg = dlg;
	}
}
