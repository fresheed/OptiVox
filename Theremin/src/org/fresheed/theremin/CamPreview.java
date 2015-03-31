package org.fresheed.theremin;

import java.io.IOException;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.Image;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

@SuppressWarnings("deprecation")
public class CamPreview extends SurfaceView implements SurfaceHolder.Callback {
	private Camera camera;
	private SurfaceHolder init_holder;
	
	public CamPreview(Context cont, Camera cam) {
		super(cont);
		camera=cam;
		
		init_holder=getHolder();
		init_holder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
		init_holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceCreated(SurfaceHolder cur_holder) {
		try {
            camera.setPreviewDisplay(cur_holder);
//            Parameters params=camera.getParameters();
//            params.setPreviewFormat(ImageFormat)
//            camera.startPreview();
            Log.d("CREATED", "no exceptions");
        } catch (IOException e) {
            Log.d("IOCamError", "Error setting camera preview: " + e.getMessage());
        } catch (Exception e) {
           Log.d("CamErrorAtStart", "Error starting camera preview: " + e.getMessage());
           e.printStackTrace();
       }
	}

	@Override
	public void surfaceChanged(SurfaceHolder cur_holder, int format, 
							   int width, int height) {
		if (cur_holder.getSurface() == null){
          return;
        }
        // stop preview before making changes
        try {
        	camera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            camera.setPreviewDisplay(cur_holder);
            camera.startPreview();
        } catch (Exception e){
            Log.d("Cam error", "Error starting camera preview: ");
            e.printStackTrace();
        }
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
//		try {
//			Log.d("", "in destroying");
//        	camera.stopPreview();
//        } catch (Exception e){
//        	e.printStackTrace();
//        }
//		init_holder.removeCallback(this);
	}

}
