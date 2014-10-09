package com.example.task3;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;

public class HomeActivity extends Activity implements LoaderCallbacks<byte[]> {

	private static final int FILE_LOADER_ID = 1;
	private static final String FILE_URL_STRING = "http://www.audiocheck.net/download.php?filename=Audio/audiocheck.net_white_88k_-3dBFS.wav";
	private static final String FILE_URL_KEY = "url";
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

	private LoaderManager loaderManager;
	private Button playButton;
	private TextView label;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		playButton = (Button) findViewById(R.id.v_play_button);
		label = (TextView) findViewById(R.id.v_status_label);

		// LoaderManager lm = new LoaderManager();

		loaderManager = getLoaderManager();

		Bundle bundle = new Bundle();
		bundle.putSerializable(FILE_URL_KEY, FILE_URL);
		// loaderManager.initLoader(FILE_LOADER_ID, bundle, this);
		loaderManager.restartLoader(FILE_LOADER_ID, bundle, this);

	}

	private void setDownloadingState() {
		playButton.setEnabled(false);
		label.setText(R.string.home_status_label_downloading);
	}

	private void setIdleState() {
		playButton.setEnabled(true);
		playButton.setText(R.string.home_play_button_play);
	
		label.setText(R.string.home_status_label_idle);
	}

	private void setPlayingState() {
		playButton.setEnabled(true);
		playButton.setText(R.string.home_play_button_pause);
	
		label.setText(R.string.home_status_label_playing);
	}

	@Override
	public Loader<byte[]> onCreateLoader(int id, Bundle args) {

		switch (id) {
		case FILE_LOADER_ID:
			setDownloadingState();
			Log.d(ACTIVITY_SERVICE,
					"onCreateLoader. New loader has been created.");

			return new FileLoader(getApplicationContext(),
					(URL) args.getSerializable(FILE_URL_KEY));
		default:
			Log.e(ACTIVITY_SERVICE,
					String.format("onCreateLoader. Unknown loader id: %d.", id));
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<byte[]> loader, byte[] bytes) {
		Log.d(ACTIVITY_SERVICE,
				"onLoadFinished. File downloading was finished. Result: "
						+ (bytes != null ? String.format("%,d bytes.",
								bytes.length) : null));

		setIdleState();
	}

	@Override
	public void onLoaderReset(Loader<byte[]> arg0) {
		Log.d(ACTIVITY_SERVICE, "onLoaderReset.");
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
