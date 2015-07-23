package org.fresheed.theremin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class PlayActivity extends Activity implements android.view.View.OnClickListener {
	  private CameraCapture cam_capture;
	  private ImageView cam_preview;
	  private TextView freq;
	  private LinearLayout mainLayout;
	  SoundPlayer player;
	  private int preview_size_width = 320;
	  private int previw_size_height= 240;
	  private Camera camera;
	  
	  private final Handler handler=new Handler(Looper.getMainLooper());
	   
	  @Override
	  public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.play_layout);
	    
	    cam_preview=new ImageView(this);
	    freq=(TextView)findViewById(R.id.freq_view);

	    player=new SoundPlayer();
	    player.playInBackground();
	    
	    
	    SurfaceView camView = new SurfaceView(this);
	    SurfaceHolder camHolder = camView.getHolder();
	    try {
	    	camera=Camera.open(1);
	    } catch (Exception e){
	    	e.printStackTrace();
	    } finally {
	    	Log.d("----", camera.toString());
	    }
	    cam_capture = new CameraCapture(preview_size_width, previw_size_height,
	    								camera, handler, this.getProcessCallback());
	         
	    camHolder.addCallback(cam_capture);
	    camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	         
	    mainLayout = (LinearLayout) findViewById(R.id.linear);
	    mainLayout.addView(cam_preview, 0, new LayoutParams(preview_size_width, previw_size_height));
	    mainLayout.addView(camView, 0, new LayoutParams(preview_size_width, previw_size_height));
	    
	    useSettings();
	    
	    Log.d("TAG", "end onCreate");
	  }
	  
	  @Override
	  protected void onPause(){
		super.onPause();
		try {
			cam_capture.cleanup();
			camera.stopPreview();
			camera.setPreviewCallback(null);
			camera.release();
			camera=null;
			
			player.stop();
		} catch (Exception e){
			e.printStackTrace();
		}
	 }
	  
	 @Override
	 public void onDestroy(){
		 super.onDestroy();
	 }
	  
	  @Override
	  public void onClick(View v) {
	    switch (v.getId()) {
//	    case R.id.goto_settings:
//	    	Intent i=new Intent(this, SettingsActivity.class);
//	    	startActivity(i);
//	      break;
	    }
	  }
	  
	    
		SeekBar filter, sens;
		float filter_value, sens_value;
		void useSettings(){
			filter=(SeekBar) findViewById(R.id.filter_level);
			sens=(SeekBar) findViewById(R.id.sens_level);
			
			OnSeekBarChangeListener listener=new OnSeekBarChangeListener() {
				
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {}
				
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {}
				
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					if (seekBar==filter) filter_value=progress/100F;
					else if (seekBar==sens) sens_value=progress/100F;
					cam_capture.setProcessingValues(filter_value, sens_value);
				}
			};
			
			filter.setOnSeekBarChangeListener(listener);
			sens.setOnSeekBarChangeListener(listener);
		}
	  
	  ImageProcessCallback getProcessCallback(){
		  return new ImageProcessCallback() {
			
			@Override
			public void setProcessedImage(Bitmap b) {
				cam_preview.setImageBitmap(b);
			}
			
			@Override
			public void setFrequency(int f) {
				freq.setText("Frequence: "+f);
				player.setFrequency(f);
			}
		};
	  }
	  
}
