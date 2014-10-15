package com.example.task3.loader;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.example.task3.Finally;
import com.example.task3.defaultloader.DefaultLoader;

public class NewFileLoader extends DefaultLoader<Result> {

	private static final String FILE_LOADER = "FileLoader";
	private static final int BUFFER_SIZE = 2 * 1024;
	private static final int MAX_PROGRESS = 100;

	private URL url;

	public NewFileLoader(Context context, URL url) {
		super(context);
		this.url = url;

	}

	@Override
	public Result loadInBackground() {
		try {
			byte[] b = download(url);
			return Result.finished(b);
		} catch (IOException e) {
			Log.e(FILE_LOADER, "Can't load file from URL: " + url + " | " + e, e);
			return Result.exception(e);
		} catch (InterruptedException e) {
			return Result.canceled();
		}
	}

	public byte[] download(URL url) throws IOException, InterruptedException {
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

	private void download(InputStream in, OutputStream out, int size) throws IOException, InterruptedException {
		byte[] b = new byte[BUFFER_SIZE];
		int readBytes;
		int downloaded = 0;
		while ((readBytes = in.read(b)) != -1) {
			out.write(b, 0, readBytes);
			downloaded += readBytes;

			publishProgress(MAX_PROGRESS * downloaded / size, MAX_PROGRESS);
			if (isCanceled()) {
				throw new InterruptedException("Downloading was canceled");
			}
		}
	}

	private int prevProgress = -1;
	private int prevMaxProgress = -1;

	private void publishProgress(int progress, int maxProgress) {
		if (prevProgress != progress || prevMaxProgress != maxProgress) {
			prevProgress = progress;
			prevMaxProgress = maxProgress;

			deliverResult(Result.inProgress(progress, maxProgress));
			Log.d(FILE_LOADER, String.format("%d / %d", progress, maxProgress));
		}
	}

	@Override
	protected void releaseResources(Result data) {
		// TODO Auto-generated method stub
	}
}
