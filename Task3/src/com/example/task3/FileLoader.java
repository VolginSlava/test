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

	public static interface OnProgressListener {
		void onProgress(int progress, int maxProgress);
	}

	private static final String FILE_LOADER = "FileLoader";
	private static final int BUFFER_SIZE = 2 * 1024;
	private static final int MAX_PROGRESS = 100;

	private final URL url;
	private byte[] downloadedData;

	private HashSet<OnProgressListener> progressListeners = new HashSet<OnProgressListener>();


	public FileLoader(Context context, URL url) {
		super(context);
		this.url = url;
	}

	@Override
	public byte[] loadInBackground() {
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
		while ((readBytes = in.read(b)) != -1) {
			out.write(b, 0, readBytes);
			downloaded += readBytes;

			publishProgress(MAX_PROGRESS * downloaded / size, MAX_PROGRESS);
			// Log.d(ACTIVITY_SERVICE, "" + downloaded);
		}
	}

	private void publishProgress(int progress, int maxProgress) {
		for (OnProgressListener listener : progressListeners) {
			listener.onProgress(progress, maxProgress);
		}
	}

	@Override
	public void deliverResult(byte[] data) {
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
		if (downloadedData != null) {
			deliverResult(downloadedData);
		}

		if (takeContentChanged() || downloadedData == null) {
			forceLoad();
		}
	}

	@Override
	protected void onStopLoading() {
		cancelLoad();
	}

	@Override
	protected void onReset() {
		onStopLoading();

		if (downloadedData != null) {
			downloadedData = null;
		}
	}

	// @Override
	// public void onCanceled(byte[] data) {
	// super.onCanceled(data);
	//
	// }
	//
	// private void releaseResources(byte[] data) {
	//
	// }

	public void addOnProgressListener(OnProgressListener listener) {
		progressListeners.add(listener);
	}

	public void removeOnProgressListener(OnProgressListener listener) {
		progressListeners.remove(listener);
	}

}
