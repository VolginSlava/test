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

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MusicService extends Service implements OnPreparedListener,
		OnCompletionListener, OnErrorListener {

	private static final String MUSIC_SERVICE = "MusicService";

	public class MusicBinder extends Binder {

		public MusicService getService() {
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

		player = new MediaPlayer();
		addPlayerListeners();
	}

	private void addPlayerListeners() {
		player.setOnPreparedListener(this);
		player.setOnErrorListener(this);
		player.setOnCompletionListener(this);
	}

	public void setMusic(byte[] bytes) {
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
			player.setDataSource(in.getFD());
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		} catch (IllegalArgumentException e) {
			Log.e(MUSIC_SERVICE, "", e);
		} catch (IllegalStateException e) {
			Log.e(MUSIC_SERVICE,
					"Exception while trying to set source data to mediaPlayer in incorrect state.",
					e);
		} catch (IOException e) {
			Log.e(MUSIC_SERVICE, "Can't read data from temp file.");
			throw new RuntimeException(e);
		} finally {
			closeFinally(in);
		}
	}

	private File saveAsTempFile(String prefix, String suffix, byte[] bytes)
			throws IOException, FileNotFoundException {
		File temp = File.createTempFile(prefix, suffix, getCacheDir());
		FileOutputStream out = new FileOutputStream(temp);
		try {
			out.write(bytes);
		} finally {
			closeFinally(out);
		}
		return temp;
	}

	public void playMusic() {
		if (!prepared) {
			player.prepareAsync();
		} else {
			resumeMusic();
		}
	}

	public void pauseMusic() {
		player.pause();
	}

	private void resumeMusic() {
		player.start();
	}

	public boolean isPlaying() {
		boolean result = false;
		try{
			result = prepared && player.isPlaying();
		} catch (IllegalStateException e) {
			Log.e(MUSIC_SERVICE, "MusicService # isPlaying", e);
		}
		return result;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return musicBind;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		if (isPlaying()) {
			player.stop();
		}
		player.release();
		return false;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer player) {
		player.start();
	}

	@Override
	public void onPrepared(MediaPlayer player) {
		prepared = true;
		resumeMusic();
	}

	private void closeFinally(Closeable c) {
		try {
			if (c != null) {
				c.close();
			}
		} catch (IOException e) {
			Log.i(MUSIC_SERVICE,
					"Exception occures while trying to close resource", e);
		}
	}
}
