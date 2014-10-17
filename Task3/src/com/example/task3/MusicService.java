package com.example.task3;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.example.task3.tools.Finally;
import com.example.task3.tools.Logging;

public class MusicService extends Service implements OnPreparedListener,
		OnCompletionListener, OnErrorListener {

	private static final String MUSIC_SERVICE = "MusicService";

	public class MusicBinder extends Binder {

		public MusicService getService() {
			Logging.logEntrance(MUSIC_SERVICE);
			return MusicService.this;
		}
	}

	private final IBinder musicBind = new MusicBinder();
	private byte[] fileBytes;
	private MediaPlayer player;
	private boolean prepared = false;

	@Override
	public void onCreate() {
		super.onCreate();
		Logging.logEntrance(MUSIC_SERVICE);

		player = new MediaPlayer();
		addPlayerListeners();
	}

	private void addPlayerListeners() {
		Logging.logEntrance(MUSIC_SERVICE);
		player.setOnPreparedListener(this);
		player.setOnErrorListener(this);
		player.setOnCompletionListener(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int res = super.onStartCommand(intent, flags, startId);
		Logging.logEntrance(MUSIC_SERVICE);

		// new Handler(getMainLooper()).post(new Runnable() {
		//
		// @Override
		// public void run() {
		// // TODO Auto-generated method stub
		//
		// playMusic();
		// }
		// });
		return START_STICKY;// res;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Logging.logEntrance(MUSIC_SERVICE);
		player.release();
		player = null;
	}

	public void setMusic(byte[] bytes) {
		Logging.logEntrance(MUSIC_SERVICE);

		fileBytes = bytes;
		prepared = false;
		player.release();
		player = new MediaPlayer();
		addPlayerListeners();

		File temp = null;
		try {
			temp = saveAsTempFile("music", "dat", fileBytes);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			Log.e(MUSIC_SERVICE, "Can't write data to temp file.");
			throw new RuntimeException(e);
		}

		FileInputStream in = null;
		try {
			in = new FileInputStream(temp);
			player.setLooping(true);
			player.setDataSource(in.getFD());
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		} catch (IllegalArgumentException e) {
			Log.e(MUSIC_SERVICE, "", e);
		} catch (IllegalStateException e) {
			Log.e(MUSIC_SERVICE, "Exception while trying to set source data to mediaPlayer in incorrect state.", e);
		} catch (IOException e) {
			Log.e(MUSIC_SERVICE, "Can't read data from temp file.");
			throw new RuntimeException(e);
		} finally {
			Finally.close(in);
		}
	}

	private File saveAsTempFile(String prefix, String suffix, byte[] bytes) throws IOException, FileNotFoundException {
		Logging.logEntrance(MUSIC_SERVICE);
		File temp = File.createTempFile(prefix, suffix, getCacheDir());
		FileOutputStream out = new FileOutputStream(temp);
		try {
			out.write(bytes);
		} finally {
			Finally.close(out);
		}
		return temp;
	}

	public void playMusic() {
		Logging.logEntrance(MUSIC_SERVICE);
		if (!prepared) {
			player.prepareAsync();
		} else {
			resumeMusic();
		}
	}

	public void pauseMusic() {
		Logging.logEntrance(MUSIC_SERVICE);
		player.pause();
	}

	private void resumeMusic() {
		Logging.logEntrance(MUSIC_SERVICE);
		player.start();
	}

	public boolean isPlaying() {
		boolean result = false;
		try{
			result = prepared && player.isPlaying();
		} catch (IllegalStateException e) {
			Log.e(MUSIC_SERVICE, "MusicService # isPlaying", e);
		}
		Logging.logEntrance(MUSIC_SERVICE, "" + result);
		return result;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Logging.logEntrance(MUSIC_SERVICE);
		return musicBind;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Logging.logEntrance(MUSIC_SERVICE);
		// if (isPlaying()) {
		// player.stop();
		// }
		// player.release();
		return false;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Logging.logEntrance(MUSIC_SERVICE, String.format("Error(what: %d, extra: %d)", what, extra));
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer player) {
		Logging.logEntrance(MUSIC_SERVICE);
	}

	@Override
	public void onPrepared(MediaPlayer player) {
		Logging.logEntrance(MUSIC_SERVICE);
		prepared = true;
		resumeMusic();
	}
}
