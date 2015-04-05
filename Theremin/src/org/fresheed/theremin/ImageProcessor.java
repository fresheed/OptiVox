package org.fresheed.theremin;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.Log;

public class ImageProcessor {
	final static String tag="Clusters";
	final Bitmap image;
	final int WIDTH, HEIGHT;
	
	byte[][] owners;
	
	public ImageProcessor(Bitmap i){
		image=i;
		WIDTH=image.getWidth();
		HEIGHT=image.getHeight();
		owners=new byte[WIDTH][HEIGHT];
	}
	
	Bitmap getProcessedImage(){
//	void getProcessedImage(){
		List<Cluster> clusters=new ArrayList<Cluster>(5);
		
		clusters.add(new Cluster(WIDTH/5, HEIGHT/2, 0)); //left
		clusters.add(new Cluster(WIDTH*4/5, HEIGHT/2, 1)); //right
		clusters.add(new Cluster(WIDTH/2, HEIGHT/6, 2)); //top middle
		clusters.add(new Cluster(WIDTH/2, HEIGHT*3/6, 3)); //mid middle
		clusters.add(new Cluster(WIDTH/2, HEIGHT*5/6, 4)); //bottom middle
//	clusters.add(new Cluster(WIDTH/10, HEIGHT/10, 0)); 
//	clusters.add(new Cluster(WIDTH/9, HEIGHT*9/10, 1)); 
//	clusters.add(new Cluster(WIDTH*3/4, HEIGHT*2/3, 2)); 
//	clusters.add(new Cluster(WIDTH/6, HEIGHT*8/10, 3)); 
//		clusters.add(new Cluster(WIDTH/6, HEIGHT/6, 0)); 
//		clusters.add(new Cluster(WIDTH*3/6, HEIGHT/6, 1)); 
//		clusters.add(new Cluster(WIDTH*5/6, HEIGHT/6, 2)); 
//		clusters.add(new Cluster(WIDTH/6, HEIGHT*3/6, 3)); 
//		clusters.add(new Cluster(WIDTH*3/6, HEIGHT*3/6, 4)); 
//		clusters.add(new Cluster(WIDTH*5/6, HEIGHT*3/6, 5)); 
//		clusters.add(new Cluster(WIDTH/6, HEIGHT*5/6, 6)); 
//		clusters.add(new Cluster(WIDTH*3/6, HEIGHT*5/6, 7)); 
//		clusters.add(new Cluster(WIDTH*5/6, HEIGHT*5/6, 8));
		
		float min, comp;
		byte min_cluster, index;
		int current_color;
		for (int i=0; i<WIDTH; i++){
			for (int j=0; j<HEIGHT; j++){
				min=Float.MAX_VALUE;
				min_cluster=-1;
				current_color=image.getPixel(i, j);
				for (Cluster cur: clusters){
					comp=cur.calcDifference(i, j, current_color);
					if (comp<min){
						min=comp;
						min_cluster=cur.id;
					}
				}
				owners[i][j]=min_cluster;
			}
		}
		
		
		Bitmap b=Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
		Canvas c=new Canvas(b);
		Paint p=new Paint();
		p.setColor(Color.MAGENTA);
		p.setStrokeWidth(3);
		p.setStyle(Style.STROKE);
		p.setAlpha(100);
		
//		c.drawBitmap(image, 0, 0, null);
//		int count=0;
//		for (Cluster cur: clusters){
//			cur.calcStats();
//			c.drawRect(cur.min_x, cur.min_y, 
//					cur.max_x, cur.max_y, p);
//			Log.d(tag, "Cluster "+(count++)+": "+cur.getDescription());
//		}
//		int[] colors={Color.RED, Color.BLUE, Color.GRAY,
//                      Color.GREEN, Color.YELLOW};
		int[] colors={Color.RED, Color.BLUE, Color.GRAY,
					  Color.GREEN, Color.YELLOW, Color.CYAN,
					  Color.BLACK, Color.WHITE, Color.DKGRAY};
		for (int i=0; i<WIDTH; i++){
			for (int j=0; j<HEIGHT; j++){
				b.setPixel(i, j, colors[owners[i][j]]);
			}
		}
		c.drawBitmap(image, 0, 0, p);
		
		return b;
	}
	
	void splitToClusters(){
		
	}
	
	private class Cluster {
		final byte id;
		final static float COLOR_DIFF_COEFF=0.6F;
		final float MAX_DIST2_DIFF=(WIDTH/2)*(WIDTH/2)+(HEIGHT/2)*(HEIGHT/2);
		final float MAX_COLOR_DIFF=255F;
		
		int center_x, center_y;
		int center_color;
		
		int size;
		int min_x, max_x;
		int min_y, max_y;
		
		boolean god=false;
		
		Cluster(int ix, int iy, int id){
			center_x=ix;
			center_y=iy;
			center_color=image.getPixel(ix, iy);
			this.id=(byte)id;
			if (id==5) god=true;
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
		
		float calcDifference(int x, int y, int color){
			int dist2=(center_x-x)*(center_x-x)+(center_y-y)*(center_y-y);
			int color_diff=colorDiff(color);
//			if (god) Log.d(tag, "dist2/color: "+(COLOR_DIFF_COEFF*color_diff/MAX_COLOR_DIFF)+" :: "+((1-COLOR_DIFF_COEFF)*dist2/MAX_DIST2_DIFF));
//			return color_diff/MAX_COLOR_DIFF;
//			return dist2/MAX_DIST2_DIFF;
			return (COLOR_DIFF_COEFF*color_diff/MAX_COLOR_DIFF+(1-COLOR_DIFF_COEFF)*dist2/MAX_DIST2_DIFF);
		}
		
		int colorDiff(int pt_color){
			int dr=Math.abs(Color.red(pt_color)-Color.red(center_color));
			int dg=Math.abs(Color.green(pt_color)-Color.green(center_color));
			int db=Math.abs(Color.blue(pt_color)-Color.blue(center_color));
			return (dr+dg+db)/3;
		}
	}
	
}
