package com.example.task3;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.HashSet;

import com.example.task3.tools.Logging;

public class ProgressDialogFragment extends DialogFragment {

	public static interface CancelListener {
		void onCancel();
	}

	private static final String DIALOG_FRAGMENT = "MyDialog";

	private HashSet<CancelListener> cancelListeners = new HashSet<CancelListener>();
	private ProgressDialog pd;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Logging.logEntrance(DIALOG_FRAGMENT);
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
		Logging.logEntrance(DIALOG_FRAGMENT);

		for (CancelListener listener : cancelListeners) {
			listener.onCancel();
		}
	}

	public void addCancelListener(CancelListener listener) {
		Logging.logEntrance(DIALOG_FRAGMENT);

		cancelListeners.add(listener);
	}

	public void removeCancelListener(CancelListener listener) {
		Logging.logEntrance(DIALOG_FRAGMENT);

		cancelListeners.remove(listener);
	}
}
