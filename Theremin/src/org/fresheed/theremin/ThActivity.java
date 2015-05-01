package org.fresheed.theremin;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ThActivity extends Activity implements android.view.View.OnClickListener {
	  private CameraCapture cam_capture;
	  private ImageView cam_preview;
	  private LinearLayout mainLayout;
	  private int preview_size_width = 320;
	  private int previw_size_height= 240;
	  private Camera camera;
	   
	  @Override
	  public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.main_layout);
	    
	    cam_preview=new ImageView(this);
	 
	    SurfaceView camView = new SurfaceView(this);
	    SurfaceHolder camHolder = camView.getHolder();
	    try {
	    	camera=Camera.open(1);
	    } catch (Exception e){
	    	e.printStackTrace();
	    } finally {
	    	Log.d("----", camera.toString());
	    }
	    cam_capture = new CameraCapture(preview_size_width, previw_size_height, cam_preview, camera);
	         
	    camHolder.addCallback(cam_capture);
	    camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	         
	    mainLayout = (LinearLayout) findViewById(R.id.linear);
	    mainLayout.addView(cam_preview, new LayoutParams(preview_size_width, previw_size_height));
//	    try {
//			InputStream is=getAssets().open("hand.png");
//	    	final Bitmap bitmap=BitmapFactory.decodeStream(is);
//			ImageProcessor proc=new ImageProcessor(bitmap);
//			Bitmap res=proc.getProcessedImage();
//			cam_preview.setImageBitmap(res);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	    
	    mainLayout.addView(camView, new LayoutParams(preview_size_width, previw_size_height));
	    
	    Button click=(Button)findViewById(R.id.clicker);
	    click.setOnClickListener(this);
	  }
	  
	  protected void onPause(){
		super.onPause();
		try {
			cam_capture.cleanup();
			camera.stopPreview();
			camera.setPreviewCallback(null);
			camera.release();
			camera=null;
		} catch (Exception e){
			e.printStackTrace();
		}
	 }
	  
	  @Override
	  public void onClick(View v) {
	    switch (v.getId()) {
	    case R.id.clicker:
	    	Log.d("CAMCAPTURE", "brn clicked");
	      	Toast.makeText(this, "Parallel", Toast.LENGTH_SHORT).show();
	      break;
	    }
	  }
	  
	  public static void p(String m){
//			Log.d("CAMCAPTURE", m);
		}
}
