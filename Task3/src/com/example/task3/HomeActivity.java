package com.example.task3;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
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

import com.example.task3.MusicService.MusicBinder;
import com.example.task3.ProgressDialogFragment.CancelListener;
import com.example.task3.loader.FileLoader;
import com.example.task3.loader.FileLoader.ProgressListener;

public class HomeActivity extends Activity {

	private static final int NOTIFICATION_ID = 2;
	private static final String FILE_URL_KEY = "url";
	private static final String FILE_URL_STRING = "http://www.directlinkupload.com/uploads/46.20.72.162/Jingle-Punks-Arriba-Mami.mp3";

	private static final String DOWNLOADED_FILE_KEY = "downloadedMusicFile";
	private static final URL FILE_URL;
	static {
		try {
			FILE_URL = new URL(FILE_URL_STRING);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}


	private Button playButton;
	private TextView label;

	private StatesUtils statesUtils = new StatesUtils();
	private DialogUtils dialogUtils = new DialogUtils();
	private LoaderUtils loaderUtils = new LoaderUtils();
	private MediaPlayerUtils mediaPlayerUtils = new MediaPlayerUtils();
	private NotificationsUtils notificationsUtils = new NotificationsUtils();


	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		dialogUtils.onCreate();

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
				}
				Log.d(ACTIVITY_SERVICE, "HomeActivity # OnPlayButtonEvent. IsPlaying: " + mediaPlayerUtils.isPlaying());
			}
		});

		if (getLastNonConfigurationInstance() != null) {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> map = (HashMap<String, Object>) getLastNonConfigurationInstance();
			loaderUtils.downloadedMusicFile = (byte[]) map
					.get(DOWNLOADED_FILE_KEY);

			mediaPlayerUtils.bind();
		} else {
			mediaPlayerUtils.startMusicService();
		}

		loaderUtils.onCreate();
		if (!loaderUtils.isFileDownloaded()) {
			Bundle bundle = new Bundle();
			bundle.putSerializable(FILE_URL_KEY, FILE_URL);
			loaderUtils.initLoaderManager(bundle);
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d(ACTIVITY_SERVICE, "HomeActivity # onRestart");

		notificationsUtils.hide(NOTIFICATION_ID);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(ACTIVITY_SERVICE, "HomeActivity # onStart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(ACTIVITY_SERVICE, "HomeActivity # onResume");

		statesUtils.onResumeUpdateState();
		loaderUtils.addProgressListener();
		dialogUtils.addCancelListener();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(ACTIVITY_SERVICE, "HomeActivity # onPause");

		loaderUtils.removeProgressListener();
		dialogUtils.removeCancelListener();
		dialogUtils.hideProgressDialog();
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(ACTIVITY_SERVICE, "HomeActivity # onStop");

		Notification notification = notificationsUtils.create(NOTIFICATION_ID);
		notificationsUtils.show(NOTIFICATION_ID, notification);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(ACTIVITY_SERVICE, "HomeActivity # onDestroy");

		if (mediaPlayerUtils.serviceBound) {
			mediaPlayerUtils.unbind();
		}
		notificationsUtils.hide(NOTIFICATION_ID);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (mediaPlayerUtils.isPlaying()) {
			mediaPlayerUtils.pausePlaying();
		}
		Log.d(ACTIVITY_SERVICE, "HomeActivity # onBackPressed");
	}

	private boolean isPauseButtonPressed() {
		return mediaPlayerUtils.isPlaying();
	}

	private boolean isPlayButtonPressed() {
		return !isPauseButtonPressed();
	}

	@Override
	@Deprecated
	public Object onRetainNonConfigurationInstance() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(DOWNLOADED_FILE_KEY, loaderUtils.downloadedMusicFile);

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

	private class NotificationsUtils {

		private static final int REQUEST_CODE = 3;

		@SuppressWarnings("deprecation")
		private Notification create(int notificationId) {
			Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(
					HomeActivity.this, REQUEST_CODE, intent, 0);

			Notification.Builder builder = new Notification.Builder(
					HomeActivity.this);

			builder.setContentTitle("Title")
					.setContentText("text")
					.setContentInfo("info")
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentIntent(pendingIntent);

			return builder.getNotification();
		}

		private void show(int notificationId, Notification notification) {
			getNotificationManager().notify(notificationId, notification);
		}

		private void hide(int notificationId) {
			getNotificationManager().cancel(notificationId);
		}

		private NotificationManager getNotificationManager() {
			return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		}
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

		private void onResumeUpdateState() {
			if (!loaderUtils.isFileDownloaded()) {
				setDownloadingState();
			} else if (mediaPlayerUtils.isPlaying()) {
				setPlayingState();
			} else {
				setIdleState();
			}
		}

		private void onServiceConnectedUpdateState() {
			if (loaderUtils.isFileDownloaded()) {
				if (mediaPlayerUtils.isPlaying()) {
					statesUtils.setPlayingState();
				} else {
					statesUtils.setIdleState();
				}
			}
		}
	}

	private class DialogUtils implements CancelListener {

		private ProgressDialogFragment progressDialogFragment;

		private void onCreate() {
			progressDialogFragment = new ProgressDialogFragment();
		}

		private void showProgressDialog() {
			progressDialogFragment.show(getFragmentManager(), "progressDialog");
			// progressDialog = new MyDialogFragment();
			// FragmentManager fragmentManager = getFragmentManager();
			// FragmentTransaction fragmentTransaction = fragmentManager
			// .beginTransaction();
			// fragmentTransaction.add(progressDialog, null);
			// fragmentTransaction.commit();
		}

		private void hideProgressDialog() {
			progressDialogFragment.dismiss();
			// FragmentManager fragmentManager = getFragmentManager();
			// FragmentTransaction fragmentTransaction = fragmentManager
			// .beginTransaction();
			// fragmentTransaction.remove(progressDialog);
			// fragmentTransaction.commit();
		}

		private void addCancelListener() {
			progressDialogFragment.addCancelListener(DialogUtils.this);
		}

		private void removeCancelListener() {
			progressDialogFragment.removeCancelListener(DialogUtils.this);
		}

		@Override
		public void onCancel() {
			finish();
		}
	}

	private class LoaderUtils implements LoaderCallbacks<byte[]>,
			ProgressListener {

		private static final int FILE_LOADER_ID = 1;

		private LoaderManager loaderManager;

		private byte[] downloadedMusicFile;

		private void onCreate() {
			loaderUtils.loaderManager = getLoaderManager();
		}

		private void initLoaderManager(Bundle bundle) {
			loaderManager.initLoader(FILE_LOADER_ID, bundle, this);
		}

		private void addProgressListener() {
			FileLoader fileLoader = getFileLoader(FILE_LOADER_ID);
			if (fileLoader != null) {
				fileLoader.addProgressListener(this);
			}
		}

		private void removeProgressListener() {
			FileLoader fileLoader = getFileLoader(FILE_LOADER_ID);
			if (fileLoader != null) {
				fileLoader.removeProgressListener(this);
			}
		}

		private FileLoader getFileLoader(int fileLoaderId) {
			return (FileLoader) loaderManager
					.<byte[]> getLoader(fileLoaderId);
		}

		private boolean isFileDownloaded() {
			return downloadedMusicFile != null;
		}

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
			ProgressDialog pd = (ProgressDialog) dialogUtils.progressDialogFragment
					.getDialog();

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

				statesUtils.onServiceConnectedUpdateState();

				if (loaderUtils.isFileDownloaded()) {
					Log.d(ACTIVITY_SERVICE,
							"ServiceConnection # onServiceConnected, downloadedFile != null. Service bound: "
									+ serviceBound);
				}

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

		private void stopMusicService() {
			unbind();
			stopService(playIntent);
			musicService = null;
		}

		private void bind() {
			if (playIntent == null) {
				playIntent = new Intent(HomeActivity.this, MusicService.class);
			}
			boolean bind = bindService(playIntent, musicConnection,
					Context.BIND_AUTO_CREATE);

			Log.d(ACTIVITY_SERVICE, "Bind: " + bind);
		}

		private void unbind() {
			unbindService(musicConnection);
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
