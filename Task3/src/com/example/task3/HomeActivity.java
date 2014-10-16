package com.example.task3;

import android.app.Activity;
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

import com.example.task3.MusicService.MusicBinder;
import com.example.task3.ProgressDialogFragment.CancelListener;
import com.example.task3.loader.NewFileLoader;
import com.example.task3.loader.Result;
import com.example.task3.tools.Logging;

public class HomeActivity extends Activity {

	private static final String DOWNLOAD_COMPLETE_KEY = "downloadComplete";
	private static final int NOTIFICATION_ID = 2;
	private static final String FILE_URL_KEY = "url";
	private static final String FILE_URL_STRING = "http://www.directlinkupload.com/uploads/46.20.72.162/Jingle-Punks-Arriba-Mami.mp3";

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		Logging.logEntrance(ACTIVITY_SERVICE);

		if (savedInstanceState != null) {
			loaderUtils.downloadComplete = savedInstanceState.getBoolean(DOWNLOAD_COMPLETE_KEY, false);
		}

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
				Logging.logEntrance(ACTIVITY_SERVICE, "IsPlaying: " + mediaPlayerUtils.isPlaying());
			}
		});

		if (savedInstanceState != null) { // TODO assume on first time the activity is started we receive null in savedInstanceState
			mediaPlayerUtils.bind();
		} else {
			mediaPlayerUtils.startMusicService();
		}

		if (!loaderUtils.isFileDownloaded()) {
			Bundle bundle = new Bundle();
			bundle.putSerializable(FILE_URL_KEY, FILE_URL);
			getLoaderManager().initLoader(LoaderUtils.FILE_LOADER_ID, bundle, loaderUtils);
			dialogUtils.showProgressDialog();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Logging.logEntrance(ACTIVITY_SERVICE, "Download complete: " + loaderUtils.downloadComplete);

		outState.putBoolean(DOWNLOAD_COMPLETE_KEY, loaderUtils.downloadComplete);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Logging.logEntrance(ACTIVITY_SERVICE);

		notificationsUtils.hide(NOTIFICATION_ID);

		if (!loaderUtils.isFileDownloaded()) {
			Bundle bundle = new Bundle();
			bundle.putSerializable(FILE_URL_KEY, FILE_URL);
			getLoaderManager().restartLoader(LoaderUtils.FILE_LOADER_ID, bundle, loaderUtils);

			dialogUtils.showProgressDialog();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		Logging.logEntrance(ACTIVITY_SERVICE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Logging.logEntrance(ACTIVITY_SERVICE);

		statesUtils.onResumeUpdateState();
		dialogUtils.addCancelListener();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Logging.logEntrance(ACTIVITY_SERVICE);

		dialogUtils.removeCancelListener();
	}

	@Override
	protected void onStop() {
		super.onStop();
		Logging.logEntrance(ACTIVITY_SERVICE);

		Notification notification = notificationsUtils.create(NOTIFICATION_ID);
		notificationsUtils.show(NOTIFICATION_ID, notification);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Logging.logEntrance(ACTIVITY_SERVICE);

		if (mediaPlayerUtils.serviceBound) {
			mediaPlayerUtils.unbind();
		}
		notificationsUtils.hide(NOTIFICATION_ID);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Logging.logEntrance(ACTIVITY_SERVICE);

		if (mediaPlayerUtils.isPlaying()) {
			mediaPlayerUtils.pausePlaying();
		}
	}

	private boolean isPauseButtonPressed() {
		return mediaPlayerUtils.isPlaying();
	}

	private boolean isPlayButtonPressed() {
		return !isPauseButtonPressed();
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
			PendingIntent pendingIntent = PendingIntent.getActivity(HomeActivity.this, REQUEST_CODE, intent, 0);

			Notification.Builder builder = new Notification.Builder(HomeActivity.this);

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
			Logging.logEntrance(ACTIVITY_SERVICE);

			playButton.setEnabled(false);
			label.setText(R.string.home_status_label_downloading);
		}

		private void setIdleState() {
			Logging.logEntrance(ACTIVITY_SERVICE);

			playButton.setEnabled(true);
			playButton.setText(R.string.home_play_button_play);

			label.setText(R.string.home_status_label_idle);
		}

		private void setPlayingState() {
			Logging.logEntrance(ACTIVITY_SERVICE);

			playButton.setEnabled(true);
			playButton.setText(R.string.home_play_button_pause);

			label.setText(R.string.home_status_label_playing);
		}

		private void onResumeUpdateState() {
			Logging.logEntrance(ACTIVITY_SERVICE);

			if (!loaderUtils.isFileDownloaded()) {
				setDownloadingState();
			} else if (mediaPlayerUtils.isPlaying()) {
				setPlayingState();
			} else {
				setIdleState();
			}
		}

		private void onServiceConnectedUpdateState() {
			Logging.logEntrance(ACTIVITY_SERVICE);

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

		private static final String PROGRESS_DIALOG_FRAGMENT_TAG = "progressDialogFragment";

		private ProgressDialogFragment progressDialogFragment;


		private ProgressDialogFragment getFragment() {
			Logging.logEntrance(ACTIVITY_SERVICE);
			ProgressDialogFragment fragment = (ProgressDialogFragment) getFragmentManager().findFragmentByTag(PROGRESS_DIALOG_FRAGMENT_TAG);
			if (fragment == null) {
				if (progressDialogFragment == null) {
					progressDialogFragment = new ProgressDialogFragment();
				}
				fragment = progressDialogFragment;
			}
			return fragment;
		}

		private void showProgressDialog() {
			boolean res = getFragmentManager().executePendingTransactions();
			Logging.logEntrance(ACTIVITY_SERVICE, "executePendingTransactions: " + res);

			if (!getFragment().isAdded()) {
				// getFragment().show(getFragmentManager(), "progressDialog");
				getFragmentManager()
						.beginTransaction()
						.add(getFragment(), PROGRESS_DIALOG_FRAGMENT_TAG)
						.commit();
			}
		}

		private void hideProgressDialog() {
			boolean res = getFragmentManager().executePendingTransactions();
			Logging.logEntrance(ACTIVITY_SERVICE, "executePendingTransactions: " + res);

			if (getFragment().isAdded()) {
				// getFragment().dismiss();
				getFragmentManager()
						.beginTransaction()
						.remove(getFragment())
						.commit();
			}
		}

		private void addCancelListener() {
			Logging.logEntrance(ACTIVITY_SERVICE);
			getFragment().addCancelListener(DialogUtils.this);
		}

		private void removeCancelListener() {
			Logging.logEntrance(ACTIVITY_SERVICE);
			getFragment().removeCancelListener(DialogUtils.this);
		}

		@Override
		public void onCancel() {
			Logging.logEntrance(ACTIVITY_SERVICE);
			finish();
		}
	}

	private class LoaderUtils implements LoaderCallbacks<Result> {

		private static final int FILE_LOADER_ID = 1;

		private boolean downloadComplete = false;


		private boolean isFileDownloaded() {
			Logging.logEntrance(ACTIVITY_SERVICE, "(" + downloadComplete + ")");
			return downloadComplete;
		}

		@Override
		public Loader<Result> onCreateLoader(int id, Bundle args) {
			switch (id) {
			case FILE_LOADER_ID:
				Logging.logEntranceExtra("New loader has been created.");
				return new NewFileLoader(HomeActivity.this, (URL) args.getSerializable(FILE_URL_KEY));
			default:
				Logging.logEntranceExtra("Unknown loader id: " + id);
				return null;
			}
		}

		@Override
		public void onLoaderReset(Loader<Result> arg0) {
			Logging.logEntrance(ACTIVITY_SERVICE);
		}

		@Override
		public void onLoadFinished(Loader<Result> loader, final Result result) {
			Logging.logEntrance(ACTIVITY_SERVICE);

			switch (result.state) {
			case IN_PROGRESS:
				onProgress(result.progress, result.maxProgress);
				break;
			case FINISHED:
				onFinish(result.bytes);
				break;
			case EXCEPTION:
				onException(result.exception);
				break;
			case CANCELED:
				onCancel();
				break;
			}
		}

		private void onProgress(final int progress, final int maxProgress) {
			Logging.logEntrance(ACTIVITY_SERVICE);
			ProgressDialog pd = (ProgressDialog) dialogUtils.getFragment().getDialog();

			if (pd != null) {
				pd.setMax(maxProgress);
				pd.setProgress(progress);
			}
		}

		private void onFinish(final byte[] bytes) {
			Logging.logEntrance(ACTIVITY_SERVICE, "File downloading was finished. Result: " + (bytes != null ? String.format("%,d bytes.", bytes.length) : null));
			
			downloadComplete = true;
			mediaPlayerUtils.musicService.setMusic(bytes);
			
			Handler handler = new Handler();
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					statesUtils.setIdleState();
					dialogUtils.hideProgressDialog();
				}
			});
		}

		private void onException(Throwable exception) { // TODO maybe some useful code should be placed here
			Log.e(ACTIVITY_SERVICE, "", exception);
		}

		private void onCancel() { // TODO maybe some useful code should be placed here
			Logging.logEntrance(ACTIVITY_SERVICE, "Downloading was canceled");
		}
	}

	private class MediaPlayerUtils {
		private MusicService musicService;
		private Intent playIntent;
		private boolean serviceBound = false;

		private ServiceConnection musicConnection = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Logging.logEntrance("ServiceConnection");
				MusicBinder binder = (MusicBinder) service;

				musicService = binder.getService();
				serviceBound = true;

				statesUtils.onServiceConnectedUpdateState();

				if (loaderUtils.isFileDownloaded()) {
					Logging.logEntrance(ACTIVITY_SERVICE, "Downloaded file != null. Service bound: " + serviceBound);
				}
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Logging.logEntrance("ServiceConnection");
				serviceBound = false;
			}
		};

		private void startMusicService() {
			Logging.logEntrance(ACTIVITY_SERVICE);
			if (playIntent == null) {
				playIntent = new Intent(HomeActivity.this, MusicService.class);
				bind();
				startService(playIntent);
			}
		}

		private void stopMusicService() {
			Logging.logEntrance(ACTIVITY_SERVICE);
			unbind();
			stopService(playIntent);
			musicService = null;
		}

		private void bind() {
			if (playIntent == null) {
				playIntent = new Intent(HomeActivity.this, MusicService.class);
			}
			boolean bind = bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);

			Logging.logEntrance(ACTIVITY_SERVICE, " (" + bind + ")");
		}

		private void unbind() {
			Logging.logEntrance(ACTIVITY_SERVICE);
			unbindService(musicConnection);
		}

		private void startPlaying() {
			Logging.logEntrance(ACTIVITY_SERVICE);
			musicService.playMusic();
		}

		private void pausePlaying() {
			Logging.logEntrance(ACTIVITY_SERVICE);
			musicService.pauseMusic();
		}

		private boolean isPlaying() {
			Logging.logEntrance(ACTIVITY_SERVICE);
			return musicService != null && musicService.isPlaying();
		}
	}
}
