package org.fresheed.theremin;

import java.io.IOException;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

@SuppressWarnings("deprecation")
public class CameraCapture implements SurfaceHolder.Callback, Camera.PreviewCallback {
	private final static String t="CAMERACAPTURE";
	private final int preview_width, preview_height;
	
	private final Bitmap bitmap;
	private final int[] pixel_array;
			private final int[] colors={Color.RED, Color.BLACK, Color.BLUE, Color.CYAN, Color.MAGENTA};
	private byte[] data_array;
	private int image_format;
	
	private ImageProcessor processor;
	private final float DEFAULT_THRESHOLD=0.5F,
						DEFAULT_PER_LINE=0.1F;
	private float threshold, per_line;
	
	private Handler handler;
	private ImageProcessCallback process_callback;
	
	private Camera camera;
	
	private final Object proc_lock=new Object();
	private boolean now_processing=false;
	
	public CameraCapture (int prev_width, int prev_height, Camera cam, Handler h, ImageProcessCallback cb) {
		preview_width=prev_width;
		preview_height=prev_height;
		bitmap=Bitmap.createBitmap(prev_width, prev_height, Bitmap.Config.ARGB_8888);
		pixel_array=new int[prev_height*prev_width];
		p("Creation: "+pixel_array.length);
		
		camera=cam;
		processor=new ImageProcessor(bitmap);
		handler=h;
		process_callback=cb;
		
		threshold=DEFAULT_THRESHOLD;
		per_line=DEFAULT_PER_LINE;
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
	    if (image_format == ImageFormat.NV21){
	    	synchronized (proc_lock) {
	    		if ( !now_processing ){
	    			now_processing=true;
	    			data_array = data;
	    			asyncProcessImage();
	    		}
			}
	   }
	}
	
	public void setProcessingValues(float t, float p){
		threshold=t;
		per_line=p;
	}

	private void asyncProcessImage() {
		new Thread(){
			@Override
			public void run(){
				int frequency=processor.processByGradient(data_array, pixel_array, threshold, per_line);
		        handler.post(updatePreview);
		        process_callback.setFrequency(frequency);
			}
		}.start();
	}
	
	private Runnable updatePreview=new Runnable(){
		@Override
		public void run(){
			synchronized (proc_lock) {
				now_processing=false;
			}
			bitmap.setPixels(pixel_array, 0, preview_width, 0,0 , preview_width, preview_height);
			process_callback.setProcessedImage(bitmap);
		}
	};

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Parameters params=camera.getParameters();
		try {
			camera.stopPreview();
		} catch (Exception e){
			
		}
		List<Size> sizes=params.getSupportedPictureSizes();
		for (Size s: sizes) p(s.width+"x"+s.height);
		params.setPreviewSize(width, height);
		image_format=params.getPreviewFormat();
		camera.setParameters(params);

		camera.setPreviewCallback(this);
		camera.startPreview();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d("AppLog", "surface destroyed");
	}
	
	public void cleanup(){
		camera.stopPreview();
	}
	
	public static void p(String m){
		Log.d("CAMCAPTURE", m);
	}
}

