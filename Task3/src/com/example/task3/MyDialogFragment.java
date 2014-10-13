package com.example.task3;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import java.util.HashSet;

public class MyDialogFragment extends DialogFragment {

	public static interface CancelListener {
		void onCancel();
	}

	private static final String DIALOG_FRAGMENT = "MyDialog";

	private HashSet<CancelListener> cancelListeners = new HashSet<CancelListener>();
	private ProgressDialog pd;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Log.d(DIALOG_FRAGMENT, "MyDialogFragment # onCreateDialog");

		pd = new ProgressDialog(getActivity());

		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setTitle("Title");
		pd.setMessage("Downloading...");

		pd.setCanceledOnTouchOutside(false);
		return pd;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);

		Log.d(DIALOG_FRAGMENT, "MyDialogFragment # onCancel");

		for (CancelListener listener : cancelListeners) {
			listener.onCancel();
		}
	}

	public void addCancelListener(CancelListener listener) {
		Log.d(DIALOG_FRAGMENT, "MyDialogFragment # addProgressListener");

		cancelListeners.add(listener);
	}

	public void removeCancelListener(CancelListener listener) {
		Log.d(DIALOG_FRAGMENT, "MyDialogFragment # removeCancelListener");

		cancelListeners.remove(listener);
	}
}
