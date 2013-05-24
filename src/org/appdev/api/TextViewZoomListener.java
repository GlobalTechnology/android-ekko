package org.appdev.api;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.FloatMath;  
import android.view.MotionEvent;  
import android.view.View;  
import android.view.View.OnTouchListener;  
import android.widget.TextView;  

/**
 * Used to zoom in/out the text view
 *
 */
public class TextViewZoomListener implements OnTouchListener {  

	private int mode = 0;  
	float oldDist;  
	float textSize = 0;  
	Matrix savedMatrix = new Matrix();
	Matrix matrix = new Matrix();
	PointF start = new PointF();
	PointF mid = new PointF();

	TextView textView = null;  

	/**
	 * mode: 0 for NONE; 1 for Drag; 2 for ZOOM
	 */
	@Override  
	public boolean onTouch(View v, MotionEvent event) {  
		textView = (TextView) v;  
		if (textSize == 0) {  
			textSize = textView.getTextSize();  
		}  
		switch (event.getAction() & MotionEvent.ACTION_MASK) {  
		case MotionEvent.ACTION_DOWN:  
			savedMatrix.set(matrix);
			start.set(event.getX(), event.getY());
			mode = 1;  
			break;  
		case MotionEvent.ACTION_UP:  	
		case MotionEvent.ACTION_POINTER_UP:  
			mode = 0;  
			break;  
		case MotionEvent.ACTION_POINTER_DOWN:  
			oldDist = spacing(event);  
			if(oldDist>10f){
				savedMatrix.set(matrix);				
				mode = 2;
			}
			break;  

		case MotionEvent.ACTION_MOVE:  
			if (mode == 1) {  
				matrix.set(savedMatrix);
				
			}else if(mode == 2){
				float newDist = spacing(event);  
				if (newDist > oldDist +10) {  
					matrix.set(savedMatrix);
					float scale = newDist/oldDist;
					
					zoom(scale);				
					oldDist = newDist;  
				}  
				if (newDist < oldDist - 10) {  
					zoom(newDist / oldDist);  
					oldDist = newDist;  
				}  
			}  
			break;  
		}  
		
		return true;  
	}  

	private void zoom(float f) {  
		textView.setTextSize(textSize *= f);  
	}  

	private float spacing(MotionEvent event) {  
		float x = event.getX(0) - event.getX(1);  
		float y = event.getY(0) - event.getY(1);  
		return FloatMath.sqrt(x * x + y * y);  
	}
	

}  

