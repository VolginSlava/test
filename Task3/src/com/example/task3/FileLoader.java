package com.example.task3;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;

public class FileLoader extends AsyncTaskLoader<byte[]> {

	public static interface ProgressListener {
		void onProgress(int progress, int maxProgress);
	}

	private static final String FILE_LOADER = "FileLoader";
	private static final int BUFFER_SIZE = 2 * 1024;
	private static final int MAX_PROGRESS = 100;


	private HashSet<ProgressListener> progressListeners = new HashSet<ProgressListener>();

	private final URL url;
	private byte[] downloadedData;
	private boolean isCanceled = false;


	public FileLoader(Context context, URL url) {
		super(context);
		this.url = url;
	}

	@Override
	public byte[] loadInBackground() {
		Log.d(FILE_LOADER, "FileLoader # loadInBackground");

		if (downloadedData != null) {
			return downloadedData;
		}

		try {
			return download(url);
		} catch (IOException e) {
			Log.e(FILE_LOADER, "Can't load file from URL: " + url + " | " + e,
					e);
			return null;
		}
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
			Finally.close(out);
			Finally.close(in);
		}
		return file.toByteArray();
	}

	private void download(InputStream in, OutputStream out, int size)
			throws IOException {
		byte[] b = new byte[BUFFER_SIZE];
		int readBytes;
		int downloaded = 0;
		while ((readBytes = in.read(b)) != -1) {
			out.write(b, 0, readBytes);
			downloaded += readBytes;

			publishProgress(MAX_PROGRESS * downloaded / size, MAX_PROGRESS);
			if (isCanceled()) {
				break;
			}
		}
	}

	private int prevProgress = -1;
	private int prevMaxProgress = -1;

	private void publishProgress(int progress, int maxProgress) {
		synchronized (progressListeners) {
			for (ProgressListener listener : progressListeners) {
				listener.onProgress(progress, maxProgress);
			}
		}
		if (prevProgress != progress || prevMaxProgress != maxProgress) {
			prevProgress = progress;
			prevMaxProgress = maxProgress;
			Log.d(FILE_LOADER, String.format("%d / %d", progress, maxProgress));
		}
	}

	@Override
	public void deliverResult(byte[] data) {
		Log.d(FILE_LOADER, "FileLoader # deliverResult");

		if (isReset()) {
			return;
		}
		downloadedData = data;

		if (isStarted()) {
			super.deliverResult(data);
		}
	}

	@Override
	protected void onStartLoading() {
		Log.d(FILE_LOADER, "FileLoader # onStartLoading");

		if (downloadedData != null) {
			deliverResult(downloadedData);
		}

		if (takeContentChanged() || downloadedData == null) {
			forceLoad();
		}
	}

	@Override
	protected void onStopLoading() {
		Log.d(FILE_LOADER, "FileLoader # onStopLoading");

		if (cancelLoad()) {
			isCanceled = true;
		}
	}

	private boolean isCanceled() {
		if (isCanceled) {
			isCanceled = false;
			return true;
		}
		return isCanceled;
	}

	@Override
	protected void onReset() {
		Log.d(FILE_LOADER, "FileLoader # onReset");

		onStopLoading();

		if (downloadedData != null) {
			downloadedData = null;
		}
	}

	public void addProgressListener(ProgressListener listener) {
		Log.d(FILE_LOADER, "FileLoader # addProgressListener");

		progressListeners.add(listener);
	}

	public void removeProgressListener(ProgressListener listener) {
		Log.d(FILE_LOADER, "FileLoader # removeProgressListener");

		progressListeners.remove(listener);
	}
}
