package com.example.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;

import java.util.concurrent.TimeUnit;

public class SplashScreenActivity extends Activity {
	private static final long SPLASH_TIMEOUT = TimeUnit.SECONDS.toMillis(2);

	private Handler handler;
	private Runnable runnable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		long timeout = getTimeout();
		if (timeout == 0) {
			startHomeActivity();
		} else {
			handler = new Handler();
			runnable = new Runnable() {

				@Override
				public void run() {
					startHomeActivity();
				}
			};

			handler.postDelayed(runnable, timeout);
		}
	}

	@Override
	protected void onPause() {
		super.onDestroy();
		handler.removeCallbacks(runnable);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		startHomeActivity();
		return true;
	}

	private long getTimeout() {
		return SPLASH_TIMEOUT;
	}

	private void startHomeActivity() {
		Intent intent = new Intent(SplashScreenActivity.this,
				HomeActivity.class);
		SplashScreenActivity.this.startActivity(intent);
		SplashScreenActivity.this.finish();
	}
}
