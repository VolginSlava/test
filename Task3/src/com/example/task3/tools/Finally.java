package com.example.task3.tools;

import android.util.Log;

import java.io.Closeable;
import java.io.IOException;

public class Finally {
	private static final String TAG = "Finally";
	private static final String MESSAGE = "Exception occures while trying to close resource";

	public static void close(Closeable c) {
		try {
			if (c != null) {
				c.close();
			}
		} catch (IOException e) {
			Log.i(TAG, MESSAGE, e);
		}
	}
}
