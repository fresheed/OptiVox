package org.fresheed.theremin;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;

public class ImageProcessor {
	final static String tag="Clusters";
	final Bitmap image;
	final int process_y_start, process_y_interval;
	final int WIDTH, HEIGHT;
	final int[][] bitmap_colors;
	
	
	final int[] temp;
	
	byte[][] owners;
	
	boolean f=true;
	
	List<Cluster> clusters;
	
	public ImageProcessor(Bitmap i){
		image=i;
		WIDTH=image.getWidth();
		HEIGHT=image.getHeight();
		owners=new byte[WIDTH][HEIGHT];
		bitmap_colors=new int[WIDTH][HEIGHT];
		temp=new int[WIDTH*HEIGHT];
		
		process_y_start=0;
		process_y_interval=HEIGHT;
		
		clusters=new ArrayList<Cluster>(4);
		
		clusters.add(new Cluster(WIDTH/2, HEIGHT/4, 0)); 
		clusters.add(new Cluster(WIDTH/3, HEIGHT/2, 1));
		clusters.add(new Cluster(WIDTH*2/3, HEIGHT/2, 2)); 
		clusters.add(new Cluster(WIDTH/2, HEIGHT*3/4, 3)); 
	}
	
	public void processImg(byte[] source, int offset, int interval, int step, int[] output){
		for (int i=0; i<WIDTH; i++){
			for (int j=offset; j<offset+interval; j++){
				bitmap_colors[i][j]=( (source[j*step+i]^0xFF)<<24); //fill bitmap
			}
		}
		
		for (Cluster c:clusters) c.updateColor();

		float min, comp;
		byte min_cluster;
		int current_color;
		Log.d(tag, "begin");
		Cluster cur;
		int size=clusters.size();
		for (int i=0; i<WIDTH; i++){
			for (int j=offset; j<offset+interval; j++){
				min=Float.MAX_VALUE;
				min_cluster=-1;
				current_color=bitmap_colors[i][j];
				for (int cl=0; cl<size; cl++){
					cur=clusters.get(cl);
					comp=cur.calcByteDifference(i,j, current_color);
					if (comp<min){
						min=comp;
						min_cluster=cur.id;
					}
				}
//				owners[i][j]=min_cluster;
				clusters.get(min_cluster).setOwnerTo(i, j);
			}
		}
		Log.d(tag, "end");
		
		int[] colors={Color.RED, Color.BLUE, Color.GRAY,
					  Color.GREEN, Color.YELLOW, Color.CYAN,
					  Color.BLACK, Color.WHITE, Color.DKGRAY};
		for (int i=0; i<WIDTH; i++){
			for (int j=offset; j<offset+interval; j++){
				output[j*step+i]=colors[owners[i][j]];
			}
		}
		
//		for (Cluster cluster:clusters){
//			cluster.update(offset, offset+interval);
//		}
	}
	
	public void processImgH(byte[] source, int offset, int interval, int step, int[] output){
		for (int i=0; i<WIDTH; i++){
			for (int j=offset; j<offset+interval; j++){
				owners[i][j]=source[j*step+i];
			}
		}
		for (int i=0; i<WIDTH; i++){
			for (int j=offset; j<offset+interval; j++){
				output[j*step+i]=((owners[i][j]^0xFE))<<24;;
			}
		}
	}
	
	public void processByGradientH(byte[] source, int offset, int interval, int step, int[] output){
//		for (int i=0; i<WIDTH; i++){
//			for (int j=offset; j<offset+interval; j++){
//				bitmap_colors[i][j]=( (source[j*step+i]^0xFF)<<24); //fill bitmap
//			}
//		}
		
		for (Cluster c:clusters) c.updateColor();
		
		int cur, l, r, t, b, lt, lb, rt, rb,
				gx, gy, res;
//			short count=0;
		final float MAX_DELTA=6308352;
		final double MAX_DELTA_LOG=(float) Math.log10(MAX_DELTA);
		int white=Color.WHITE;
		int lg=Color.LTGRAY;
		int grey=Color.GRAY;
		int dgr=Color.DKGRAY;
		int black=Color.BLACK;
		double rel_log, rel;
		int uv_offset=step*HEIGHT;
		for (int i=1; i<WIDTH-2-1; i+=1){
			for (int j=offset+1; j<offset+interval-2; j+=1){
				byte y=source[j*step+i];
				byte u=source[uv_offset+(j/2)*(step/2)+i&~1+1];
				byte v=source[uv_offset+(j/2)*(step/2)+i&~1];
//				*r = yValue + (1.370705 * (vValue-128));
//			    *g = yValue - (0.698001 * (vValue-128)) - (0.337633 * (uValue-128));
//			    *b = yValue + (1.732446 * (uValue-128));
				byte red=(byte) (y+(1.370705 * (v-128)));
				byte green=(byte) (y-(0.698001 * (v-128)) - (0.337633 * (u-128)));
				byte blue=(byte) (y + (1.732446 * (u-128)));
				bitmap_colors[i][j]=0xFF000000 | (red<<16) | (green<<8) | (blue);
				
//				l=source[j*step+i-1];
//				r=source[j*step+i+1];
//				t=source[(j-1)*step+i];
//				b=source[(j+1)*step+i];
//				lt=source[(j-1)*step+i-1];
//				lb=source[(j+1)*step+i-1];
//				rt=source[(j-1)*step+i+1];
//				rb=source[(j+1)*step+i+1];
//				
//				gx=(lb+b+rb)-(lt+t+rt);
//				gy=(rt+r+rb)-(lt+l+lb);
//	//				gx=(3*lb+10*b+3*rb)-(3*lt+10*t+3*rt);
//	//				gy=(3*rt+10*r+3*rb)-(3*lt+10*l+3*lb);
////				gx=rb-cur;
////				gy=b-r;
//				
//				res=gx*gx+gy*gy;
//				
//				rel_log=Math.log10(res);
//				if (rel_log==Double.NaN || rel_log==Double.NEGATIVE_INFINITY
//						||rel_log==Double.POSITIVE_INFINITY){
////					Log.d("TAG", "extra: "+rel_log+", arg: "+res);
//					bitmap_colors[i][j]=Color.RED;
//					continue;
//				}
//				if (rel_log>5){
////					Log.d("TAG", "extra: "+rel_log+", arg: "+res);
//					bitmap_colors[i][j]=Color.BLUE;
//					continue;
//				}
//				
//				rel=rel_log/MAX_DELTA_LOG;
//				if (rel < 0.4f) rel=0;
//				int tmp=(int)Math.min((255*rel), 255);
////				Log.d("Tag", "rel: "+(res/MAX_DELTA));
////				bitmap_colors[i][j]=0xFF000000 | (tmp<<8) | (tmp<<16) | (tmp);
//				bitmap_colors[i][j]=tmp<<24;
////				bitmap_colors[i][j]=(tmp<<24);
////				Log.d("hEX", "Hex: "+Integer.toHexString(bitmap_colors[i][j]));
			}
		}
		
//		final int BRIGHT=100;
//		final int HAND_MIN=interval*1/15;
//		int hand_pos=0;
//		for (int i=1; i<WIDTH-2-1; i+=2){
//			int brights=0;
//			for (int j=offset+1; j<offset+interval-2-1; j+=2){
//				if ((bitmap_colors[i][j]>>24)>BRIGHT){
//					brights++;
//				}
//			}
//			if (brights>=HAND_MIN) hand_pos=i;
//		}
//		Log.d("HAND", "hand_pos: "+hand_pos);

		
//		for (int i=1; i<WIDTH-2; i+=2){
//			for (int j=offset+1; j<offset+interval-2; j+=2){
//				bitmap_colors[i+1][j]=bitmap_colors[i][j];			
//				bitmap_colors[i+1][j+1]=bitmap_colors[i][j];
//				bitmap_colors[i][j+1]=bitmap_colors[i][j];
//			}
//		}
		
		for (int i=0; i<WIDTH; i++){
			for (int j=offset; j<offset+interval; j++){
				output[j*step+i]=0;
			}
		}
		
		for (int i=0; i<WIDTH; i++){
			for (int j=offset; j<offset+interval; j++){
				output[j*step+i]=bitmap_colors[i][j];
			}
		}
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
//					Log.d("TAG", "extra: "+rel_log+", arg: "+res);
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
	
	static int rgbFromYuv(int y, int u, int v){
		int red = clampUnsignedByte(y + (int)1.402f*v);
		int green = clampUnsignedByte(y - (int)(0.344f*u +0.714f*v));
		int blue = clampUnsignedByte(y + (int)1.772f*u);
		return 0xFF000000 | (red<<16) | (green<<8) | (blue);
	}
	
	static int clampUnsignedByte(int arg){
		return arg > 255 ? 255 : (arg<0 ? 0 : arg&0xFF);
	}
	
	int indexByXY(int x, int y){
		return y*WIDTH+x;
	}
	
	class Cluster {
		final byte id;
		final static float COLOR_DIFF_COEFF=0.5F;
		final float MANH_MAX=(WIDTH)+(HEIGHT);
		final static float MAX_COLOR_DIFF=255F;
		
		int center_x, center_y;
		int center_color, frombyte_center_color;
		
		int size;
		int sum_x, sum_y;
		
		int min_x, max_x;
		int min_y, max_y;
		
		Cluster(int ix, int iy, int id){
			center_x=ix;
			center_y=iy;
			center_color=bitmap_colors[ix][iy];
			this.id=(byte)id;
			
			size=0;
			sum_x=0;
			sum_y=0;
		}
		
		public void update(int offset, int max) {
			Log.d("PROC", "Id/size: "+id+" "+size);
//			if (size==0) return;
			if (size==0) size=1;
			int appr_cx=sum_x/size;
			int appr_cy=sum_y/size;
			int min_md=Integer.MAX_VALUE;
			loop: for (int i=0; i<WIDTH; i++){
				for (int j=offset; j<max; j++){
					if (owners[i][j]==this.id){
						int manh_dist=Math.abs(appr_cx-i)+Math.abs(appr_cy-j);
						if (manh_dist<min_md) {
							min_md=manh_dist;
							center_x=i;
							center_y=j;
							if (manh_dist<10) break loop;
						}
					}
				}
			}
			
			size=0;
			sum_x=0;
			sum_y=0;
		}

		public void setOwnerTo(int i, int j) {
			owners[i][j]=this.id;
			size++;
			sum_x+=i;
			sum_y+=j;
		}

		void updateColor(){
			center_color=bitmap_colors[center_x][center_y];
		}
		
		void recalc(int new_cx, int new_cy){
			center_x=new_cx;
			center_y=new_cy;
			center_color=bitmap_colors[center_x][center_y];
		}

		void calcStats(){
			int count=0;
			min_x=WIDTH;
			max_x=0;
			min_y=HEIGHT;
			max_y=0;
				
			for (int i=0; i<WIDTH; i++){
				for (int j=0; j<HEIGHT; j++){
					if (owners[i][j] == this.id){
						count++;
						if (i>max_x) max_x=i;
						else if (i<min_x) min_x=i;
						if (j>max_y) max_y=j;
						else if (j<min_y) min_y=j;
					}
				}
			}
			size=count;
		}
		
		String getDescription(){
			return "Size:"+size+"left-right:"+min_x+" "+max_x+
					"top-bottom: "+min_y+" "+max_y;
		}
		
		public float calcByteDifference(int x, int y, int current_color) {
//			int manh_dist=Math.abs((center_x-x)+(center_y-y));
			int manh_dist=Math.abs(center_x-x)+Math.abs(center_y-y);
			
			int color_diff=Math.abs(center_color-current_color);
			return (COLOR_DIFF_COEFF*color_diff/MAX_COLOR_DIFF+(1-COLOR_DIFF_COEFF)*manh_dist/MANH_MAX);
		}
		
	}
	
}
