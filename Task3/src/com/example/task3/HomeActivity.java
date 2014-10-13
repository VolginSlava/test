package com.example.task3;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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

import com.example.task3.FileLoader.ProgressListener;
import com.example.task3.MusicService.MusicBinder;
import com.example.task3.MyDialogFragment.CancelListener;

public class HomeActivity extends Activity {

	private static final String MEDIA_PLAYER_KEY = "mediaPlayer";
	private static final String FILE_URL_KEY = "url";
	// private static final String FILE_URL_STRING =
	// "http://www.audiocheck.net/download.php?filename=Audio/audiocheck.net_white_88k_-3dBFS.wav";
	private static final String FILE_URL_STRING = "http://www.directlinkupload.com/uploads/46.20.72.162/Jingle-Punks-Arriba-Mami.mp3";

	private static final URL FILE_URL;
	private static final int FILE_LOADER_ID = 1;
	private static final String DOWNLOADED_FILE_KEY = "downloadedMusicFile";
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
	private DialogUtils dialogUtils = new DialogUtils();
	private LoaderUtils loaderUtils = new LoaderUtils();
	private MediaPlayerUtils mediaPlayerUtils = new MediaPlayerUtils();

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
				if (isPauseButtonPressed()) {
					statesUtils.setIdleState();

					mediaPlayerUtils.pausePlaying();
				} else if (isPlayButtonPressed()) {
					statesUtils.setPlayingState();

					mediaPlayerUtils.startPlaying();
					// if (mediaPlayer == null) {
					// mediaPlayer = new MediaPlayer();
					// mediaPlayerUtils.initializeAndStartPlayer(mediaPlayer,
					// downloadedMusicFile);
					// } else {
					// mediaPlayerUtils.startPlaying(mediaPlayer);
					// }
				}
				Log.d(ACTIVITY_SERVICE,
						"HomeActivity # OnPlayButtonEvent. IsPlaying: "
								+ mediaPlayerUtils.isPlaying());
			}
		});

		if (getLastNonConfigurationInstance() != null) {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> map = (HashMap<String, Object>) getLastNonConfigurationInstance();
			downloadedMusicFile = (byte[]) map.get(DOWNLOADED_FILE_KEY);
			mediaPlayer = (MediaPlayer) map.get(MEDIA_PLAYER_KEY);
		}

		loaderManager = getLoaderManager();

		if (downloadedMusicFile == null) {
			Bundle bundle = new Bundle();
			bundle.putSerializable(FILE_URL_KEY, FILE_URL);
			loaderManager.initLoader(FILE_LOADER_ID, bundle, loaderUtils);
		}

		if (!mediaPlayerUtils.serviceBound) {
			mediaPlayerUtils.startMusicService();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mediaPlayerUtils.serviceBound) {
			mediaPlayerUtils.stopMusicService();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		Log.d(ACTIVITY_SERVICE, "HomeActivity # onResume");

		if (downloadedMusicFile == null) {
			statesUtils.setDownloadingState();
		} else if (isPauseButtonPressed()) {
			statesUtils.setPlayingState();
		} else {
			statesUtils.setIdleState();
		}

		FileLoader fileLoader = (FileLoader) loaderManager
				.<byte[]> getLoader(FILE_LOADER_ID);
		if (fileLoader != null) {
			fileLoader.addProgressListener(loaderUtils);
		}
		progressDialog.addCancelListener(dialogUtils);
	}

	@Override
	protected void onPause() {
		super.onPause();

		Log.d(ACTIVITY_SERVICE, "HomeActivity # onPause()");

		FileLoader fileLoader = (FileLoader) loaderManager
				.<byte[]> getLoader(FILE_LOADER_ID);
		if (fileLoader != null) {
			fileLoader.removeProgressListener(loaderUtils);
		}
		progressDialog.removeCancelListener(dialogUtils);

		dialogUtils.hideProgressDialog();
	}

	// @Override
	// protected void onStart() {
	// super.onStart();
	//
	// Log.d(ACTIVITY_SERVICE, "HomeActivity # onStart");
	// Log.d(ACTIVITY_SERVICE,
	// "Downloaded file: "
	// + (downloadedMusicFile != null ? String.format(
	// "%,d bytes.", downloadedMusicFile.length)
	// : "null"));
	// Log.d(ACTIVITY_SERVICE,
	// "Loader: " + loaderManager.getLoader(FILE_LOADER_ID));
	// }
	//
	// @Override
	// protected void onStop() {
	// super.onStop();
	//
	// Log.d(ACTIVITY_SERVICE, "HomeActivity # onStop");
	// Log.d(ACTIVITY_SERVICE,
	// "Downloaded file: "
	// + (downloadedMusicFile != null ? String.format(
	// "%,d bytes.", downloadedMusicFile.length)
	// : "null"));
	// }
	//
	// @Override
	// protected void onDestroy() {
	// super.onDestroy();
	//
	// Log.d(ACTIVITY_SERVICE, "HomeActivity # onDestroy");
	// }

	@Override
	@Deprecated
	public Object onRetainNonConfigurationInstance() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(DOWNLOADED_FILE_KEY, downloadedMusicFile);
		map.put(MEDIA_PLAYER_KEY, mediaPlayer);
	
		Log.d(ACTIVITY_SERVICE, map
				+ " onRetainNonConfigurationInstance called.");
		return map;
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

	private boolean isPauseButtonPressed() {
		return mediaPlayerUtils.isPlaying();
	}

	private boolean isPlayButtonPressed() {
		return !isPauseButtonPressed();
	}

	private class StatesUtils {
	
		private void setDownloadingState() {
			playButton.setEnabled(false);
			label.setText(R.string.home_status_label_downloading);
		
			dialogUtils.showProgressDialog();
		}
	
		private void setIdleState() {
			playButton.setEnabled(true);
			playButton.setText(R.string.home_play_button_play);
		
			label.setText(R.string.home_status_label_idle);
		
			dialogUtils.hideProgressDialog();
		}
	
		private void setPlayingState() {
			playButton.setEnabled(true);
			playButton.setText(R.string.home_play_button_pause);
		
			label.setText(R.string.home_status_label_playing);
		}
	}

	private class DialogUtils implements CancelListener {

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

		@Override
		public void onCancel() {
			finish();
		}
	}
	private class LoaderUtils implements LoaderCallbacks<byte[]>,
			ProgressListener {

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

					mediaPlayerUtils.musicService.setMusic(downloadedMusicFile);
				}
			});
		}

		@Override
		public void onLoaderReset(Loader<byte[]> arg0) {
			Log.d(ACTIVITY_SERVICE, "HomeActivity # onLoaderReset");
		}

		@Override
		public void onProgress(int progress, int maxProgress) {
			ProgressDialog pd = (ProgressDialog) progressDialog.getDialog();

			if (pd != null) {
				pd.setMax(maxProgress);
				pd.setProgress(progress);
			}
		}
	}
	
	
	private class MediaPlayerUtils {
		private MusicService musicService;
		private Intent playIntent;
		private boolean serviceBound = false;

		private ServiceConnection musicConnection = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				MusicBinder binder = (MusicBinder) service;

				musicService = binder.getService();
				serviceBound = true;

				Log.d("ServiceConnection",
						"MediaPlayerUtils # onServiceConnected");
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				serviceBound = false;

				Log.d("ServiceConnection",
						"MediaPlayerUtils # onServiceDisconnected");
			}
		};

		private void startMusicService() {
			if (playIntent == null) {
				playIntent = new Intent(HomeActivity.this, MusicService.class);
				bind();
				startService(playIntent);
			}
		}

		private void bind() {
			boolean bind = bindService(playIntent, musicConnection,
					Context.BIND_AUTO_CREATE);
			Log.d(ACTIVITY_SERVICE, "Bind: " + bind);
		}

		private void unbind() {
			unbindService(musicConnection);
		}

		private void stopMusicService() {
			unbind();
			stopService(playIntent);
			musicService = null;
		}

		private void startPlaying() {
			musicService.playMusic();
		}

		private void pausePlaying() {
			musicService.pauseMusic();
		}

		private boolean isPlaying() {
			return musicService != null && musicService.isPlaying();
		}
	}
}
