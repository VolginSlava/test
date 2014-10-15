package com.example.task3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import java.util.concurrent.TimeUnit;

public class SplashScreenActivity extends Activity {
	private static final long SPLASH_TIMEOUT = TimeUnit.SECONDS.toMillis(2);
	private static final String CREATION_TIME_KEY = "creationTime";

	private long creationTime;

	private Handler handler;
	private Runnable homeActivityStarter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);

		creationTime = getCreationTime(savedInstanceState);

		handler = new Handler();

		View splashMainLayout = findViewById(R.id.v_splash_main_layout);
		splashMainLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startHomeActivity();
			}
		});
	}

	private long getCreationTime(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			return savedInstanceState.getLong(CREATION_TIME_KEY);
		} else {
			return System.currentTimeMillis();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(CREATION_TIME_KEY, creationTime);
	}

	@Override
	protected void onResume() {
		super.onResume();
		homeActivityStarter = new Runnable() {

			@Override
			public void run() {
				startHomeActivity();
			}
		};
		handler.postDelayed(homeActivityStarter, getTimeout());
	}

	@Override
	protected void onPause() {
		super.onPause();
		handler.removeCallbacks(homeActivityStarter);
		Log.d(ACTIVITY_SERVICE, "onPause removing thread");
	}

	private long getTimeout() {
		long timeout = SPLASH_TIMEOUT - (System.currentTimeMillis() - creationTime);
		return timeout >= 0 ? timeout : 0;
	}

	private void startHomeActivity() {
		Log.d(ACTIVITY_SERVICE, "Starting new home activity.");

		Intent intent = new Intent(SplashScreenActivity.this, HomeActivity.class);
		startActivity(intent);
		finish();
	}
}