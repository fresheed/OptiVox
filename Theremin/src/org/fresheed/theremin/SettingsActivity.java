package org.fresheed.theremin;

import android.app.Activity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SettingsActivity extends Activity {
	
	SeekBar filter, sens;
	TextView res;
	
	int filter_value, sens_value;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_layout);
		
		filter=(SeekBar) findViewById(R.id.filter_level);
		sens=(SeekBar) findViewById(R.id.sens_level);
		res=(TextView)findViewById(R.id.results_label);
		
		OnSeekBarChangeListener listener=new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (seekBar==filter) filter_value=progress;
				else if (seekBar==sens) sens_value=progress;
				
				res.setText("Filter: "+filter_value+", sens: "+sens_value);				
			}
		};
		filter.setOnSeekBarChangeListener(listener);
		sens.setOnSeekBarChangeListener(listener);
	}

}
