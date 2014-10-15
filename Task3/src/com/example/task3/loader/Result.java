package com.example.task3.loader;

public class Result {

	public static enum LoaderState {
		IN_PROGRESS,
		FINISHED,
		EXCEPTION,
		CANCELED
	}

	public static Result finished(byte[] loadedFile) {
		return new Result(loadedFile);
	}

	public static Result inProgress(int progress, int maxProgress) {
		return new Result(progress, maxProgress);
	}

	public static Result exception(Throwable exception) {
		return new Result(exception);
	}

	public static Result canceled() {
		return new Result(LoaderState.CANCELED, null, -1, -1, null);
	}

	public final LoaderState state;

	public final byte[] bytes;
	public final int progress;
	public final int maxProgress;
	public final Throwable exception;

	private Result(byte[] loadedFile) {
		this(LoaderState.FINISHED, loadedFile, -1, -1, null);
	}

	private Result(int progress, int maxProgress) {
		this(LoaderState.IN_PROGRESS, null, progress, maxProgress, null);
	}

	private Result(Throwable exception) {
		this(LoaderState.EXCEPTION, null, -1, -1, exception);
	}

	private Result(LoaderState state, byte[] loadedFile, int progress, int maxProgress, Throwable exception) {
		this.state = state;
		this.bytes = loadedFile;
		this.progress = progress;
		this.maxProgress = maxProgress;
		this.exception = exception;
	}
}
