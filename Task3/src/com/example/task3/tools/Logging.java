package com.example.task3.tools;

import android.util.Log;

public class Logging {

	private static final String DEFAULT_TAG = "log";

	public static void logEntrance() {
		logEntrance(getStackTraceElement(), null, null);
	}

	public static void logEntrance(String tag) {
		logEntrance(getStackTraceElement(), tag, null);
	}

	public static void logEntranceExtra(String extra) {
		logEntrance(getStackTraceElement(), null, extra);
	}

	public static void logEntrance(String tag, String extra) {
		logEntrance(getStackTraceElement(), tag, extra);
	}

	private static StackTraceElement getStackTraceElement() {
		return Thread.currentThread().getStackTrace()[4];
	}

	private static void logEntrance(StackTraceElement element, String tag, String extra) {
		String msg = String.format("%s # %s", element.getClassName(), element.getMethodName());
		if (extra != null && !extra.isEmpty()) {
			msg += ". " + extra;
		}
		Log.d(tag != null ? tag : DEFAULT_TAG, msg);
	}
}
