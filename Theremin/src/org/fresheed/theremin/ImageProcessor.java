package org.fresheed.theremin;

import static org.fresheed.theremin.ThActivity.p;

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
		
//		if (!f){
//			int[][] xy=new int[clusters.size()][3];
//			for (int cl=0; cl<clusters.size(); cl++){
//				xy[cl][2]=0;
//			}
//			for (int i=0; i<WIDTH; i++){
//				for (int j=offset; j<offset+interval; j++){
////					bitmap_colors[i][j]=( (source[j*step+i]^0xFF)<<24); //fill bitmap
//					xy[owners[i][j]][0]+=i;
//					xy[owners[i][j]][1]+=j;
//					xy[owners[i][j]][2]++;
//				}
//			}
//			
//			for (int cl=0; cl<clusters.size(); cl++){
//				Cluster cur=clusters.get(cl);
//				int cx=xy[cl][0]/xy[cl][2];
//				int cy=xy[cl][1]/xy[cl][2];
//				Log.d("Processor", "Data for "+cl+" cluster: cx="+cx+", cy="+cy+", size="+xy[cl][2]);
//				cur.recalc(cx, cy);
//			}
//			
//		}
//		f=false;
		float min, comp;
		byte min_cluster;
		int current_color;
		Log.d(tag, "begin");
		Cluster cur;
		int size=clusters.size();
		Cluster[] to_access=new Cluster[size];
		clusters.toArray(to_access);
//		int vert_offset=0;
//		for (int i=offset; i<offset+interval; i++){
//			for (int j=0; j<HEIGHT; j++){
		for (int i=0; i<WIDTH; i++){
			for (int j=offset; j<offset+interval; j++){
				min=Float.MAX_VALUE;
				min_cluster=-1;
				current_color=bitmap_colors[i][j];
				for (int cl=0; cl<size; cl++){
					cur=to_access[cl];
					comp=cur.calcByteDifference(i,j, current_color);
					if (comp<min){
						min=comp;
						min_cluster=cur.id;
					}
				}
				owners[i][j]=min_cluster;
			}
//			vert_offset++;
		}
		Log.d(tag, "end");
		
		int[] colors={Color.RED, Color.BLUE, Color.GRAY,
					  Color.GREEN, Color.YELLOW, Color.CYAN,
					  Color.BLACK, Color.WHITE, Color.DKGRAY};
//		for (int i=offset; i<offset+interval; i++){
//			for (int j=0; j<HEIGHT; j++){
		for (int i=0; i<WIDTH; i++){
			for (int j=offset; j<offset+interval; j++){
				output[j*step+i]=colors[owners[i][j]];
			}
		}
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
		int min_x, max_x;
		int min_y, max_y;
		
		Cluster(int ix, int iy, int id){
			center_x=ix;
			center_y=iy;
			center_color=bitmap_colors[ix][iy];
			this.id=(byte)id;
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
			int manh_dist=Math.abs((center_x-x)+(center_y-y));
			int color_diff=Math.abs(center_color-current_color);
			return (COLOR_DIFF_COEFF*color_diff/MAX_COLOR_DIFF+(1-COLOR_DIFF_COEFF)*manh_dist/MANH_MAX);
		}
		
	}
	
}
