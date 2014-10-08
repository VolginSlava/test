package com.example.testapp;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class HomeActivity extends Activity implements ProgressListener {
	private static final String IS_PLAYING_KEY = "isPlaying";
	private static final String DOWNLOAD_ASYNC_TASK_KEY = "downloadAsyncTask";

	private static final int PROGRESS_DIALOG_ID = 1;
	private static final String FILE_URL_STRING = "https://upload.wikimedia.org/wikipedia/commons/6/66/Whitenoisesound.ogg";
	private static final URL FILE_URL;
	static {
		URL url;
		try {
			url = new URL(FILE_URL_STRING);
		} catch (MalformedURLException e) {
			Log.e(ACTIVITY_SERVICE, "", e);
			url = null;
		}
		FILE_URL = url;
	}

	private DownloadAsyncTask downloadAsyncTask;
	private ProgressDialog pd;
	private Button playButton;
	private boolean isPlaying;
	private TextView label;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		playButton = (Button) findViewById(R.id.v_play_button);
		playButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isPlaying()) {
					setIdleState();
				} else {
					setPlayingState();
				}
				Log.d(ACTIVITY_SERVICE, "OnPlayButtonEvent. IsPlaying: "
						+ isPlaying());
			}
		});

		label = (TextView) findViewById(R.id.v_status_label);

		if (getLastNonConfigurationInstance() == null) {
			downloadAsyncTask = new DownloadAsyncTask();
			downloadAsyncTask.execute(FILE_URL);
		} else {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> map = (HashMap<String, Object>) getLastNonConfigurationInstance();

			downloadAsyncTask = (DownloadAsyncTask) map
					.get(DOWNLOAD_ASYNC_TASK_KEY);
			isPlaying = (Boolean) map.get(IS_PLAYING_KEY);

			onScreenRotationLogging(map);
		}
	}

	private boolean isPlaying() {
		return isPlaying;
	}

	private void onScreenRotationLogging(HashMap<String, Object> map) {
		String msg = map.toString();
		if (isDownloadingFinished()) {
			try {
				msg += ". File size: " + downloadAsyncTask.get().length;
			} catch (InterruptedException e) {
				Log.e(ACTIVITY_SERVICE, "", e);
			} catch (ExecutionException e) {
				Log.e(ACTIVITY_SERVICE, "", e);
			}
		}
		Log.d(ACTIVITY_SERVICE, msg);
	}

	@Override
	@Deprecated
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PROGRESS_DIALOG_ID:
			ProgressDialog pd = new ProgressDialog(this);
			initializeProgressDialog(pd);
			return pd;
		default:
			return null;
		}
	}

	@Override
	@Deprecated
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case PROGRESS_DIALOG_ID:
			pd = (ProgressDialog) dialog;
			break;
		default:
			break;
		}
	}

	private void initializeProgressDialog(ProgressDialog pd) {
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

		pd.setTitle("Title");
		pd.setMessage("Downloading...");
	}

	@Override
	@Deprecated
	public Object onRetainNonConfigurationInstance() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(DOWNLOAD_ASYNC_TASK_KEY, downloadAsyncTask);
		map.put(IS_PLAYING_KEY, isPlaying);

		Log.d(ACTIVITY_SERVICE, map
				+ " onRetainNonConfigurationInstance called.");
		return map;
	}


	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		super.onPause();

		if (downloadAsyncTask != null) {
			downloadAsyncTask.removeProgressListener(this);
			if (!isDownloadingFinished()) {
				dismissDialog(PROGRESS_DIALOG_ID);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();

		if (!isDownloadingFinished()) {
			downloadAsyncTask.addProgressListener(this);
			showDialog(PROGRESS_DIALOG_ID);

			setDownloadingState();
		} else if (isPlaying()){
			setPlayingState();
		} else {
			setIdleState();
		}
	}

	private void setDownloadingState() {
		playButton.setEnabled(false);
		isPlaying = false;
		label.setText(R.string.home_status_label_downloading);
	}

	private void setIdleState() {
		playButton.setEnabled(true);
		playButton.setText(R.string.home_play_button_play);
		isPlaying = false;

		label.setText(R.string.home_status_label_idle);
	}

	private void setPlayingState() {
		playButton.setEnabled(true);
		playButton.setText(R.string.home_play_button_pause);
		isPlaying = true;

		label.setText(R.string.home_status_label_playing);
	}

	private boolean isDownloadingFinished() {
		return downloadAsyncTask != null
				&& downloadAsyncTask.getStatus() == Status.FINISHED;
	}

	@Override
	public void onProgress(int val, int maxVal) {
		if (pd != null) {
			pd.setMax(maxVal);
			pd.setProgress(val);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onPostExecute() {
		dismissDialog(PROGRESS_DIALOG_ID);
		removeDialog(PROGRESS_DIALOG_ID);

		setIdleState();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}


