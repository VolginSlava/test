package com.example.task3;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

public class MyDialogFragment extends DialogFragment {

	private ProgressDialog pd;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		pd = new ProgressDialog(getActivity());

		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setTitle("Title");
		pd.setMessage("Downloading...");

		return pd;
	}
}
