package com.example.testapp;

import android.app.Activity;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class HomeActivity extends Activity {
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

	private DownloadAsyncTash downloadAsyncTash;

	private static final String DOWNLOADED_FILE_KEY = "downloadedFile";
	private byte[] downloadedFile;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		if (getLastNonConfigurationInstance() == null) {
			downloadAsyncTash = new DownloadAsyncTash();
			downloadAsyncTash.setNewActivity(this);
			downloadAsyncTash.execute(FILE_URL);
		} else {
			downloadAsyncTash = (DownloadAsyncTash) getLastNonConfigurationInstance();
			downloadAsyncTash.setNewActivity(this);
			String msg = "getLastNonConfigurationInstance != null. Current state: "
					+ downloadAsyncTash.getStatus();
			if (downloadAsyncTash.getStatus() == Status.FINISHED) {
				try {
					msg += ". File size: " + downloadAsyncTash.get().length;
				} catch (InterruptedException e) {
					Log.e(ACTIVITY_SERVICE, "", e);
				} catch (ExecutionException e) {
					Log.e(ACTIVITY_SERVICE, "", e);
				}
			}
			Log.d(ACTIVITY_SERVICE, msg);
		}

	}

	@Override
	@Deprecated
	public Object onRetainNonConfigurationInstance() {
		Log.d(ACTIVITY_SERVICE, "onRetainNonConfigurationInstance");
		return downloadAsyncTash;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putByteArray(DOWNLOADED_FILE_KEY, downloadedFile);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		downloadedFile = savedInstanceState.getByteArray(DOWNLOADED_FILE_KEY);
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


