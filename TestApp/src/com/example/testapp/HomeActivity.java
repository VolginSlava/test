package com.example.testapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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
	private static final String CURRENT_MUSIC_DOWNLOADING_PROGRESS_KEY = "currentProgress";
	private static final int MAX_PROGRESS = 100;

	private ProgressDialog musicDownloadingProgressDialog;
	// private Thread dialogUpdatingThread;

	private DownloadAsyncTash downloadAsyncTash;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		downloadAsyncTash = new DownloadAsyncTash(this);
		downloadAsyncTash.execute(FILE_URL);
	}

	class DownloadAsyncTash extends AsyncTask<URL, Integer, byte[]> {
		private static final int BUFFER_SIZE = 2 * 1024;

		private Context context;
		private ProgressDialog musicDownloadingProgressDialog;

		private Button play;
		private TextView label;

		public DownloadAsyncTash(Context context) {
			this.context = context;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			musicDownloadingProgressDialog = new ProgressDialog(context);
			initializeProgressDialog(musicDownloadingProgressDialog);

			play = (Button) findViewById(R.id.v_play_button);
			play.setEnabled(false);

			label = (TextView) findViewById(R.id.v_status_label);
			label.setText(R.string.home_status_label_downloading);
		}

		private void initializeProgressDialog(final ProgressDialog pd) {
			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		
			// pd.setCancelable(false);
		
			pd.setTitle("Title");
			pd.setMessage("Message");
			pd.setMax(MAX_PROGRESS);
		
			pd.show();
		}

		@Override
		protected byte[] doInBackground(URL... urls) {
			try {
				return download(urls[0]);
			} catch (IOException e) {
				Log.d(ACTIVITY_SERVICE, "", e);
				return fakeDownload(urls[0]);
			}
		}

		private byte[] fakeDownload(URL url) {
			byte[] result = new byte[1024 * 1024];
			for (int i = 0; i <= MAX_PROGRESS; i += MAX_PROGRESS / 100) {
				try {Thread.sleep(25);} catch (InterruptedException e) {}
				publishProgress(i);
			}
			return result;
		}

		public byte[] download(URL url) throws IOException {
			ByteArrayOutputStream file = new ByteArrayOutputStream();
		
			InputStream in = null;
			OutputStream out = file;
			try {
				URLConnection connection = url.openConnection();
				in = connection.getInputStream();
				download(in, out, connection.getContentLength());
			} finally {
				out.close();
				if (in != null) {
					in.close();
				}
			}
		
			return file.toByteArray();
		}

		private void download(InputStream in, OutputStream out, int size)
				throws IOException {
			byte[] b = new byte[BUFFER_SIZE];
			int readBytes;
			int downloaded = 0;
			while ((readBytes = in.read(b)) > 0) {
				out.write(b, 0, readBytes);
				downloaded += readBytes;
				publishProgress(MAX_PROGRESS * downloaded / size);
				
				Log.d(ACTIVITY_SERVICE, "" + downloaded);
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			musicDownloadingProgressDialog.setProgress(values[0]);
		}

		@Override
		protected void onPostExecute(byte[] result) {
			super.onPostExecute(result);

			play.setEnabled(true);

			label.setText(R.string.home_status_label_idle);
			label.setText(label.getText() + "\ndownloaded bytes: "
					+ result.length);

			musicDownloadingProgressDialog.dismiss();
		}
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
		// if (dialogUpdatingThread != null) {
		// dialogUpdatingThread.interrupt();
		// }
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


