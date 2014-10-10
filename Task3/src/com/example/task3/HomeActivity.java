package com.example.task3;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.Loader;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

import com.example.task3.FileLoader.OnProgressListener;

public class HomeActivity extends Activity {

	private static final String MEDIA_PLAYER_KEY = "mediaPlayer";
	private static final String FILE_URL_KEY = "url";
	private static final String FILE_URL_STRING = "http://www.audiocheck.net/download.php?filename=Audio/audiocheck.net_white_88k_-3dBFS.wav";
	private static final URL FILE_URL;
	private static final int FILE_LOADER_ID = 1;
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
	private byte[] downloadedMusicFile;
	private MyDialogFragment progressDialog;
	private MediaPlayer mediaPlayer;

	private StatesUtils statesUtils = new StatesUtils();
	private MediaPlayerUtils mediaPlayerUtils = new MediaPlayerUtils();
	private LoaderUtils loaderUtils = new LoaderUtils();

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		progressDialog = new MyDialogFragment();
		label = (TextView) findViewById(R.id.v_status_label);
		playButton = (Button) findViewById(R.id.v_play_button);
		playButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mediaPlayerUtils.isPlaying()) {
					statesUtils.setIdleState();

					mediaPlayerUtils.pausePlaying(mediaPlayer);
				} else {
					statesUtils.setPlayingState();

					if (mediaPlayer == null) {
						mediaPlayer = new MediaPlayer();
						mediaPlayerUtils.initializeAndStartPlayer(mediaPlayer);
					} else {
						mediaPlayerUtils.startPlaying(mediaPlayer);
					}
				}
				Log.d(ACTIVITY_SERVICE, "OnPlayButtonEvent. IsPlaying: "
						+ mediaPlayerUtils.isPlaying());
			}
		});

		loaderManager = getLoaderManager();
		Bundle bundle = new Bundle();
		bundle.putSerializable(FILE_URL_KEY, FILE_URL);
		loaderManager.initLoader(FILE_LOADER_ID, bundle, loaderUtils);

		if (downloadedMusicFile == null) {
			statesUtils.setDownloadingState();
		}

		if (getLastNonConfigurationInstance() != null) {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> map = (HashMap<String, Object>) getLastNonConfigurationInstance();
			mediaPlayer = (MediaPlayer) map.get(MEDIA_PLAYER_KEY);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		((FileLoader) loaderManager.<byte[]> getLoader(FILE_LOADER_ID))
				.addOnProgressListener(loaderUtils);
		Log.d(ACTIVITY_SERVICE,
				(downloadedMusicFile != null ? String.format("%,d bytes.",
						downloadedMusicFile.length) : "null"));
	}

	@Override
	protected void onStop() {
		super.onStop();

		((FileLoader) loaderManager.<byte[]> getLoader(FILE_LOADER_ID))
				.removeOnProgressListener(loaderUtils);
		Log.d(ACTIVITY_SERVICE,
				(downloadedMusicFile != null ? String.format("%,d bytes.",
						downloadedMusicFile.length) : "null"));
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
	@Deprecated
	public Object onRetainNonConfigurationInstance() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(MEDIA_PLAYER_KEY, mediaPlayer);

		Log.d(ACTIVITY_SERVICE, map
				+ " onRetainNonConfigurationInstance called.");
		return map;
	}

	private class MediaPlayerUtils {

		private void initializePlayer(MediaPlayer mediaPlayer, byte[] file) {
			File temp = null;
			try {
				temp = File.createTempFile("temp", "dat", getCacheDir());
				// temp.deleteOnExit();
			} catch (IOException e) {
				Log.e(ACTIVITY_SERVICE,
						"Exception while trying to create temp file.", e);
			}

			try {
				FileOutputStream out = new FileOutputStream(temp);
				out.write(file);
				out.close();
			} catch (FileNotFoundException e) {
				Log.e(ACTIVITY_SERVICE, "", e);
			} catch (IOException e) {
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

		private void initializeAndStartPlayer(MediaPlayer mediaPlayer) {
			AsyncTask<MediaPlayer, Integer, MediaPlayer> at = new AsyncTask<MediaPlayer, Integer, MediaPlayer>() {

				@Override
				protected MediaPlayer doInBackground(MediaPlayer... players) {
					MediaPlayer player = players[0];
					initializePlayer(player, downloadedMusicFile);
					return player;
				}

				@Override
				protected void onPostExecute(MediaPlayer result) {
					super.onPostExecute(result);
					startPlaying(result);
					Log.d(AUDIO_SERVICE, "Music started.");
				}
			};
			at.execute(mediaPlayer);
		}

		private void startPlaying(MediaPlayer mediaPlayer) {
			mediaPlayer.start();
		}

		private void pausePlaying(MediaPlayer mediaPlayer) {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
			}
		}

		private boolean isPlaying() {
			return mediaPlayer != null && mediaPlayer.isPlaying();
		}
	}

	private class StatesUtils {

		private void showProgressDialog() {
			progressDialog.show(getFragmentManager(),
					"progressDialog");
			// progressDialog = new MyDialogFragment();
			// FragmentManager fragmentManager = getFragmentManager();
			// FragmentTransaction fragmentTransaction = fragmentManager
			// .beginTransaction();
			// fragmentTransaction.add(progressDialog, null);
			// fragmentTransaction.commit();
		}

		private void hideProgressDialog() {
			progressDialog.dismiss();
			// FragmentManager fragmentManager = getFragmentManager();
			// FragmentTransaction fragmentTransaction = fragmentManager
			// .beginTransaction();
			// fragmentTransaction.remove(progressDialog);
			// fragmentTransaction.commit();
		}

		private void setDownloadingState() {
			playButton.setEnabled(false);
			label.setText(R.string.home_status_label_downloading);
		
			showProgressDialog();
		}

		private void setIdleState() {
			playButton.setEnabled(true);
			playButton.setText(R.string.home_play_button_play);
		
			label.setText(R.string.home_status_label_idle);
		
			if (progressDialog != null) {
				hideProgressDialog();
			}
		}

		private void setPlayingState() {
			playButton.setEnabled(true);
			playButton.setText(R.string.home_play_button_pause);
		
			label.setText(R.string.home_status_label_playing);
		}
	}

	private class LoaderUtils implements LoaderCallbacks<byte[]>,
			OnProgressListener {

		@Override
		public Loader<byte[]> onCreateLoader(int id, Bundle args) {

			switch (id) {
			case FILE_LOADER_ID:
				Log.d(ACTIVITY_SERVICE,
						"onCreateLoader. New loader has been created.");

				return new FileLoader(getApplicationContext(),
						(URL) args.getSerializable(FILE_URL_KEY));
			default:
				Log.e(ACTIVITY_SERVICE, String.format(
						"onCreateLoader. Unknown loader id: %d.", id));
				return null;
			}
		}

		@Override
		public void onLoadFinished(Loader<byte[]> loader, byte[] bytes) {
			Log.d(ACTIVITY_SERVICE,
					"onLoadFinished. File downloading was finished. Result: "
							+ (bytes != null ? String.format("%,d bytes.",
									bytes.length) : null));
			downloadedMusicFile = bytes;

			Handler handler = new Handler();
			handler.post(new Runnable() {

				@Override
				public void run() {
					statesUtils.setIdleState();
				}
			});
		}

		@Override
		public void onLoaderReset(Loader<byte[]> arg0) {
			Log.d(ACTIVITY_SERVICE, "onLoaderReset.");
		}

		@Override
		public void onProgress(int progress, int maxProgress) {
			ProgressDialog pd = (ProgressDialog) progressDialog.getDialog();

			pd.setMax(maxProgress);
			pd.setProgress(progress);
		}
	}
}
