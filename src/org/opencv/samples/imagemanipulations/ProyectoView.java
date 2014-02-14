package org.opencv.samples.imagemanipulations;

import java.util.LinkedList;

import org.opencv.core.Point;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
//import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

public class ProyectoView extends View   {
	LinkedList <Point>points= new LinkedList<ProyectoView.Point>();
	 private Paint p;
	 
	 private Bitmap frame;
	public ProyectoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		p=new Paint();  
		if(frame!=null)
		 canvas.drawBitmap(frame, 0, 0, p);
		   
		/*p=new Paint();    
	    Path path = new Path();
	    boolean first = true;
	    for(Point point : points){
	        if(first){
	            first = false;
	            path.moveTo(point.x, point.y);
	        }
	        else{
	            path.lineTo(point.x, point.y);
	        }
	    }
	    canvas.drawPath(path, p);*/
	}
	
	public boolean onTouch(View view, MotionEvent event) {
	    if(event.getAction() != MotionEvent.ACTION_UP){
	        Point point = new Point();
	        point.x = event.getX();
	        point.y = event.getY();
	      //  points.add(point);
	        invalidate();
	      //  Log.d(TAG, "point: " + point);
	        return true;
	    }
	    return super.onTouchEvent(event);
	}
	public void update(Bitmap frame){
		this.frame=frame;
		Log.d("HOLAA", "point: ");
	}
	
	class Point {
	    float x, y;
	    float dx, dy;

	    @Override
	    public String toString() {
	        return x + ", " + y;
	    }
	}
}
