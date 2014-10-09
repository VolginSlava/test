package com.example.task3;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

public class HomeActivity extends Activity {

	private Button playButton;
	private TextView label;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		playButton = (Button) findViewById(R.id.v_play_button);
		label = (TextView) findViewById(R.id.v_status_label);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void setDownloadingState() {
		playButton.setEnabled(false);
		label.setText(R.string.home_status_label_downloading);
	}

	private void setIdleState() {
		playButton.setEnabled(true);
		playButton.setText(R.string.home_play_button_play);
	
		label.setText(R.string.home_status_label_idle);
	}

	private void setPlayingState() {
		playButton.setEnabled(true);
		playButton.setText(R.string.home_play_button_pause);
	
		label.setText(R.string.home_status_label_playing);
	}
}
