package com.example.testapp;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class HomeActivity extends Activity implements ProgressListener {
	private static final String MEDIA_PLAYER_KEY = "mediaPlayer";
	private static final String IS_PLAYING_KEY = "isPlaying";
	private static final String DOWNLOAD_ASYNC_TASK_KEY = "downloadAsyncTask";

	private static final int PROGRESS_DIALOG_ID = 1;
	// private static final String FILE_URL_STRING =
	// "https://upload.wikimedia.org/wikipedia/commons/6/66/Whitenoisesound.ogg";
	private static final String FILE_URL_STRING = "http://www.audiocheck.net/download.php?filename=Audio/audiocheck.net_white_88k_-3dBFS.wav";
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

	private MediaPlayer mediaPlayer;

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
					stopPlaying(mediaPlayer);
				} else {
					setPlayingState();

					mediaPlayer = new MediaPlayer();
					AsyncTask<MediaPlayer, Integer, MediaPlayer> at = new AsyncTask<MediaPlayer, Integer, MediaPlayer>() {

						@Override
						protected MediaPlayer doInBackground(
								MediaPlayer... params) {
							MediaPlayer player = params[0];
							startPlaying(player);
							return player;
						}

						@Override
						protected void onPostExecute(MediaPlayer result) {
							super.onPostExecute(result);
							result.start();
							Log.d(AUDIO_SERVICE, "Music started.");
						}
					};
					at.execute(mediaPlayer);
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
			mediaPlayer = (MediaPlayer) map.get(MEDIA_PLAYER_KEY);

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
				msg += String.format(". File size: %,d bytes.",
						downloadAsyncTask.get().length);
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
		map.put(MEDIA_PLAYER_KEY, mediaPlayer);

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
			setDownloadingState();
			showDialog(PROGRESS_DIALOG_ID);
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
		setIdleState();

		dismissDialog(PROGRESS_DIALOG_ID);
		removeDialog(PROGRESS_DIALOG_ID);
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

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(IS_PLAYING_KEY, isPlaying);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		isPlaying = savedInstanceState.getBoolean(IS_PLAYING_KEY, false);

		Log.d(ACTIVITY_SERVICE, "onRestoreInstanceState: " + savedInstanceState);
	}

	private void startPlaying(MediaPlayer mediaPlayer) {
		File temp = null;
		try {
			temp = File.createTempFile("temp", "mp3", getCacheDir());
			temp.deleteOnExit();
		} catch (IOException e) {
			Log.e(ACTIVITY_SERVICE,
					"Exception while trying to create temp file.", e);
		}

		try {
			FileOutputStream out = new FileOutputStream(temp);
			out.write(downloadAsyncTask.get());
			out.close();
		} catch (FileNotFoundException e) {
			Log.e(ACTIVITY_SERVICE, "", e);
		} catch (IOException e) {
			Log.e(ACTIVITY_SERVICE, "", e);
		} catch (InterruptedException e) {
			Log.e(ACTIVITY_SERVICE, "", e);
		} catch (ExecutionException e) {
			Log.e(ACTIVITY_SERVICE, "", e);
		}


		try {
			FileInputStream in = new FileInputStream(temp);
			mediaPlayer.setDataSource(in.getFD());
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			in.close();
		} catch (IllegalArgumentException e) {
			Log.e(ACTIVITY_SERVICE, "", e);
		} catch (IllegalStateException e) {
			Log.e(ACTIVITY_SERVICE, "", e);
		} catch (IOException e) {
			Log.e(ACTIVITY_SERVICE, "", e);
		}

		try {
			mediaPlayer.prepare();
		} catch (IllegalStateException e) {
			Log.e(ACTIVITY_SERVICE, "", e);
		} catch (IOException e) {
			Log.e(ACTIVITY_SERVICE, "", e);
		}
	}

	private void stopPlaying(MediaPlayer mediaPlayer) {
		if (mediaPlayer != null) {
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}
}


