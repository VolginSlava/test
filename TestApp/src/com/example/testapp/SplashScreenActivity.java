package com.example.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;

import java.util.concurrent.TimeUnit;

public class SplashScreenActivity extends Activity {
	private static final long SPLASH_TIMEOUT = TimeUnit.SECONDS.toMillis(2);

	private boolean isStarted = false;
	private long splashScreenStartTime = System.currentTimeMillis();

	@Override
	protected void onStart() {
		super.onStart();
		isStarted = true;
	}

	@Override
	protected void onStop() {
		super.onStop();
		isStarted = false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		long timeout = getTimeout();
		if (timeout == 0) {
			startHomeActivity();
		} else {
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					startHomeActivity();
				}
			}, timeout);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		startHomeActivity();
		return true;
	}

	private long getTimeout() {
		long res = System.currentTimeMillis() - splashScreenStartTime;
		res = SPLASH_TIMEOUT - res;
		return res >= 0 ? res : 0;
	}

	private void startHomeActivity() {
		if (isStarted && !HomeActivity.isStarted()) {
			Intent intent = new Intent(SplashScreenActivity.this,
					HomeActivity.class);
			SplashScreenActivity.this.startActivity(intent);
			SplashScreenActivity.this.finish();
		}
	}
}
