package com.example.testapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class HomeActivity extends Activity {

	private static final String CURRENT_MUSIC_DOWNLOADING_PROGRESS_KEY = "currentProgress";
	private static final int MAX_PROGRESS = 10000;

	ProgressDialog musicDownloadingProgressDialog;
	private Thread dialogUpdatingThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		int currentProgress = getSavedProgress(savedInstanceState);
		if (currentProgress < MAX_PROGRESS) {
			musicDownloadingProgressDialog = new ProgressDialog(this);
			initializeProgressDialog(musicDownloadingProgressDialog,
					currentProgress);
		}
	}

	private void initializeProgressDialog(final ProgressDialog pd,
			int currentProgress) {
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

		pd.setTitle("Title");
		pd.setMessage("Message");
		pd.setMax(MAX_PROGRESS);

		pd.show();
		pd.setProgress(currentProgress);

		dialogUpdatingThread = new Thread() {
			@Override
			public void run() {
				while (pd.getProgress() < MAX_PROGRESS
						&& !Thread.interrupted()) {
					try {sleep(50);} catch (InterruptedException e) {}
					pd.incrementProgressBy(100);
				}
				pd.dismiss();
			}
		};
		dialogUpdatingThread.start();
	}

	private int getSavedProgress(Bundle savedInstanceState) {
		int progress = savedInstanceState != null ? savedInstanceState
				.getInt(CURRENT_MUSIC_DOWNLOADING_PROGRESS_KEY) : 0;

		Log.d(ACTIVITY_SERVICE, "progress get: " + progress);
		return progress;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		int downloadingProgress = musicDownloadingProgressDialog != null ? musicDownloadingProgressDialog
				.getProgress() : MAX_PROGRESS;

		outState.putInt(CURRENT_MUSIC_DOWNLOADING_PROGRESS_KEY, downloadingProgress);
		Log.d(ACTIVITY_SERVICE, "progress saved: " + downloadingProgress);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dialogUpdatingThread != null) {
			dialogUpdatingThread.interrupt();
		}
		if (musicDownloadingProgressDialog != null) {
			musicDownloadingProgressDialog.dismiss();
		}
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
