package org.fresheed.theremin;

import android.graphics.Bitmap;
import android.graphics.Color;

public class ImageProcessor {
	final static String tag="Clusters";
	final Bitmap image;
	final int process_y_start, process_y_interval;
	final int WIDTH, HEIGHT;
	final int[][] bitmap_colors;
	
	final int[] temp;
	
	public ImageProcessor(Bitmap i){
		image=i;
		WIDTH=image.getWidth();
		HEIGHT=image.getHeight();
		bitmap_colors=new int[WIDTH][HEIGHT];
		temp=new int[WIDTH*HEIGHT];
		
		process_y_start=0;
		process_y_interval=HEIGHT;
	}
	
	public int processByGradient(byte[] source, int[] output, float bright_coeff, float bright_percent){
		int step=WIDTH;
		int y_offset=HEIGHT*WIDTH;
		
		int bright_threshold=(int) (bright_coeff*255);
		int brights_per_line=(int) (bright_percent*HEIGHT);
		
		for (int i=0; i<output.length; i++){
			output[i]=Color.WHITE;
		}
		
		int y1, y2, y3, y4;
		for(int i=0; i < y_offset; i+=2) {
	        y1 = source[i  ]&0xff;
	        y2 = source[i+1]&0xff;
	        y3 = source[WIDTH+i  ]&0xff;
	        y4 = source[WIDTH+i+1]&0xff;

	        temp[i  ] = y1;
	        temp[i+1] = y2;
	        temp[WIDTH+i  ] = y3;
	        temp[WIDTH+i+1] = y4;
	        
	        if (i!=0 && (i+2)%WIDTH==0)
	            i+=WIDTH;
	    }
		
		int l,r,t,b, cur,
			 lt,lb,rt,rb;
		int gx, gy;
		double res, rel_log, rel;
		final float MAX_DELTA=6308352;
		final double MAX_DELTA_LOG=(float) Math.log10(MAX_DELTA);
		int hand_pos=0;
		for (int i=1; i<WIDTH-2; i+=1){
			int brights=0;
			for (int j=process_y_start+1; j<process_y_start+process_y_interval-1; j+=1){
				cur=temp[j*step+i];
				l=(temp[j*step+i-1]);
				r=(temp[j*step+i+1]);
				t=(temp[(j-1)*step+i]);
				b=(temp[(j+1)*step+i]);
				lt=(temp[(j-1)*step+i-1]);
				lb=(temp[(j+1)*step+i-1]);
				rt=(temp[(j-1)*step+i+1]);
				rb=(temp[(j+1)*step+i+1]);
				
				gx=(lb+b+rb)-(lt+t+rt);
				gy=(rt+r+rb)-(lt+l+lb);
				//or another method
				
				res=gx*gx+gy*gy;
				
				rel_log=Math.log10(res);
				if (rel_log==Double.NaN || rel_log==Double.NEGATIVE_INFINITY
						||rel_log==Double.POSITIVE_INFINITY){
					bitmap_colors[i][j]=Color.RED;
					continue;
				}
				
				rel=rel_log/MAX_DELTA_LOG;
				if (rel < 0.5f) rel=0;
				int tmp=(int)Math.min((255*rel), 255);
				output[j*step+i]=(tmp<<24);
				if (tmp>bright_threshold) brights++;
			}
			if (brights>brights_per_line) hand_pos=i;
		}
		
		for (int j=process_y_start+1; j<process_y_start+process_y_interval-1; j+=1){
			output[j*step+hand_pos]=Color.RED;
		}
		
		return hand_pos;		
	}

}
