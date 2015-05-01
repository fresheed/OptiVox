package org.fresheed.theremin;

import java.io.IOException;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.ImageView;

@SuppressWarnings("deprecation")
public class CameraCapture implements SurfaceHolder.Callback, Camera.PreviewCallback {
	private final static String t="CAMERACAPTURE";
	private final int preview_width, preview_height;
	
	private final ImageView preview;
	private final Bitmap bitmap;
	private final int[] pixel_array;
			private final int[] colors={Color.RED, Color.BLACK, Color.BLUE, Color.CYAN, Color.MAGENTA};
			private int counter=0;
	private byte[] data_array;
	private int image_format;
	
	private ImageProcessor processor;
	
	private final Handler handler=new Handler(Looper.getMainLooper());
	private Camera camera;
	
	private boolean now_processing=false;
	
	public CameraCapture (int prev_width, int prev_height,
			ImageView cam_image, Camera cam) {
		preview_width=prev_width;
		preview_height=prev_height;
		preview=cam_image;
		bitmap=Bitmap.createBitmap(prev_width, prev_height, Bitmap.Config.ARGB_8888);
//		bitmap=Bitmap.createBitmap(prev_width, prev_height, Bitmap.Config.ALPHA_8);
		pixel_array=new int[prev_height*prev_width];
		p("Creation: "+pixel_array.length);
		camera=cam;
		processor=new ImageProcessor(bitmap);
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
//		Log.d("gggg", "onprev");
	    if (image_format == ImageFormat.NV21){
	    	if ( !now_processing ){
	    	data_array = data;
	    	asyncProcessImage();
	     }
	   }
	}

	private void asyncProcessImage() {
		new Thread(){
			@Override
			public void run(){
				now_processing=true;
				try {
					if (false) Thread.sleep(1000);
					int sum=0;
					int count=0;
					long begin=System.currentTimeMillis();
					int left_offset=preview_height/4;
					int to_go=preview_height/2;
//					processor.processImg(data_array, left_offset, to_go, preview_width, pixel_array);
//					processor.processByGradient(data_array, left_offset, to_go, preview_width, pixel_array);
					processor.processByGradient(data_array, 0, preview_height, preview_width, pixel_array);
					long end=System.currentTimeMillis();
					p("Time: "+(end-begin));
					p("Average:"+sum/(float)count);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		        handler.post(updatePreview);
			}
		}.start();
	}
	
	private Runnable updatePreview=new Runnable(){
		@Override
		public void run(){
			bitmap.setPixels(pixel_array, 0, preview_width, 0,0 , preview_width, preview_height);
			preview.setImageBitmap(bitmap);
			p("bitmap set");
			now_processing=false;
		}
	};

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			camera.setPreviewDisplay(holder);
//			camera.setPreviewTexture(new SurfaceTexture(17));
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
//		try {
//			camera.setPreviewCallback(null);
////			camera.remove
//			camera.stopPreview();
//		} catch (Exception e){
//			e.printStackTrace();
//		}
	}
	
	public void cleanup(){
		camera.stopPreview();
	}
	
	public static void p(String m){
		Log.d("CAMCAPTURE", m);
	}
}

