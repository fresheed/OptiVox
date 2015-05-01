package org.fresheed.theremin;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

public class ImageProcessor {
	final static String tag="Clusters";
	final Bitmap image;
	final int WIDTH, HEIGHT;
	final int[][] bitmap_colors;
	
	byte[][] owners;
	
	boolean f=true;
	
	List<Cluster> clusters;
	
	public ImageProcessor(Bitmap i){
		image=i;
		WIDTH=image.getWidth();
		HEIGHT=image.getHeight();
		owners=new byte[WIDTH][HEIGHT];
		bitmap_colors=new int[WIDTH][HEIGHT];
		
		clusters=new ArrayList<Cluster>(4);
//		clusters.add(new Cluster(WIDTH/4, HEIGHT/2, 2)); //left middle
//		clusters.add(new Cluster(WIDTH/2, HEIGHT/2, 3)); //mid middle
//		clusters.add(new Cluster(WIDTH*3/4, HEIGHT/2, 4)); //right middle
		
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
	
	public void processByGradient(byte[] source, int offset, int interval, int step, int[] output){
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
		for (int i=1; i<WIDTH-2-1; i+=2){
			for (int j=offset+1; j<offset+interval-2-1; j+=2){
				cur=source[j*step+i];
				l=source[j*step+i-1];
				r=source[j*step+i+1];
				t=source[(j-1)*step+i];
				b=source[(j+1)*step+i];
				lt=source[(j-1)*step+i-1];
				lb=source[(j+1)*step+i-1];
				rt=source[(j-1)*step+i+1];
				rb=source[(j+1)*step+i+1];
				
//				gx=(lb+b+rb)-(lt+t+rt);
//				gy=(rt+r+rb)-(lt+l+lb);
	//				gx=(3*lb+10*b+3*rb)-(3*lt+10*t+3*rt);
	//				gy=(3*rt+10*r+3*rb)-(3*lt+10*l+3*lb);
				gx=rb-cur;
				gy=b-r;
				
				res=gx*gx+gy*gy;
				
//				int tmp=(int)(0x7FFFFF*res/MAX_DELTA);
				rel_log=Math.log10(res);
				rel=rel_log/MAX_DELTA_LOG;
//				if (rel < 0.5f) rel=0;
				int tmp=(int)Math.min((255*rel), 255);
//				Log.d("Tag", "rel: "+(res/MAX_DELTA));
//				bitmap_colors[i][j]=0xFF000000 | (tmp<<8) | (tmp<<16) | (tmp);
				bitmap_colors[i][j]=tmp<<24;
//				bitmap_colors[i][j]=0xFF000000 | (tmp>>8);
//				bitmap_colors[i][j]=(tmp<<24);
//				Log.d("hEX", "Hex: "+Integer.toHexString(bitmap_colors[i][j]));
			}
		}
		
		

		
		for (int i=1; i<WIDTH-2; i+=2){
			for (int j=offset+1; j<offset+interval-2; j+=2){
				bitmap_colors[i+1][j]=bitmap_colors[i][j];			
				bitmap_colors[i+1][j+1]=bitmap_colors[i][j];
				bitmap_colors[i][j+1]=bitmap_colors[i][j];
			}
		}
		
		for (int i=0; i<WIDTH; i++){
			for (int j=offset; j<offset+interval; j++){
				output[j*step+i]=0;
			}
		}
		
//		for (int i=1; i<WIDTH-2; i+=2){
//			for (int j=offset+1; j<offset+interval-2; j+=2){
//				output[j*step+i]=bitmap_colors[i][j];
//			}
//		}
		
//		for (int i=0; i<WIDTH; i++){
//			for (int j=offset; j<offset+interval; j++){
//				int res=0;
//				if (i%2==1 && j%2==1) res=0xFFFFFFFF;
//				output[j*step+i]=res;
//			}
//		}
		
//		for (int i=0; i<output.length; i++){
//			if (i%2==0) output[i]=0;
//			else output[i]=0xFFFFFFFF;
//		}
		
		for (int i=0; i<WIDTH; i++){
			for (int j=offset; j<offset+interval; j++){
				output[j*step+i]=bitmap_colors[i][j];
			}
		}
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
