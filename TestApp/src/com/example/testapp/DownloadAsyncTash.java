package com.example.testapp;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

class DownloadAsyncTash extends AsyncTask<URL, Integer, byte[]> {
	private static final String ASYNC_TASK = "DownloadAsyncTash";
	private static final int BUFFER_SIZE = 2 * 1024;
	private static final int MAX_PROGRESS = 100;
	private Activity activity;

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Log.d(ASYNC_TASK, "DownloadAsyncTash: start");
	}

	@Override
	protected byte[] doInBackground(URL... urls) {
		try {
			return download(urls[0]);
		} catch (IOException e) {
			Log.d(ASYNC_TASK, "", e);
			return fakeDownload(urls[0]);
		}
	}

	private byte[] fakeDownload(URL url) {
		byte[] result = new byte[1024 * 1024];
		for (int i = 0; i <= MAX_PROGRESS; i += MAX_PROGRESS / 100) {
			try {
				Thread.sleep(25);
			} catch (InterruptedException e) {
			}
			publishProgress(i);

			Log.d(ASYNC_TASK, "" + i);
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
		while ((readBytes = in.read(b)) != -1) {
			out.write(b, 0, readBytes);
			downloaded += readBytes;
			publishProgress(MAX_PROGRESS * downloaded / size);

			// Log.d(ACTIVITY_SERVICE, "" + downloaded);
		}
	}

	@Override
	protected void onPostExecute(byte[] result) {
		super.onPostExecute(result);

		Log.d(ASYNC_TASK, "DownloadAsyncTash: end. File size: "
				+ result.length);

		Toast.makeText(activity, "End", Toast.LENGTH_SHORT).show();

		TextView label = (TextView) activity.findViewById(R.id.v_status_label);
		label.setText(R.string.home_status_label_idle);

		Button play = (Button) activity.findViewById(R.id.v_play_button);
		play.setEnabled(true);
	}

	public void setNewActivity(Activity activity) {
		this.activity = activity;

		if (getStatus() != Status.FINISHED) {
			Button play = (Button) activity.findViewById(R.id.v_play_button);
			play.setEnabled(false);

			TextView label = (TextView) activity
					.findViewById(R.id.v_status_label);
			label.setText(R.string.home_status_label_downloading);
		}
	}
}