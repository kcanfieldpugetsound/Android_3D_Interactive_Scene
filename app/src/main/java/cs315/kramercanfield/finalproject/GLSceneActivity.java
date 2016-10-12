package cs315.kramercanfield.finalproject;

//import cs315.jross.hwk1.R;
//import cs315.yourname.hwk4.GLDancingRobotActivity.GLBasicView;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
//import android.view.View;

/**
 * A basic activity for displaying a simple OpenGL rendering. This uses a slightly different structure than
 * with a regular Canvas.
 * 
 * @author Joel
 * @version Fall 2013
 */
public class GLSceneActivity extends Activity
{
//	private static final String TAG = "GLScene"; //for logging/debugging

	private GLSurfaceView _GLView; //the view that we're actually drawing

	/**
	 * Called when the activity is started
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		_GLView = (GLSurfaceView)this.findViewById(R.id.gl_view);
	}

	protected void onPause() {
		super.onPause();
		_GLView.onPause(); //tell the view to pause
	}

	protected void onResume() {
		super.onResume();
		_GLView.onResume(); //tell the view to resume
	}


	/**
	 * The actual view itself, includes as an inner class. Note that this also controls interaction (but not rendering)
	 * We put the OpenGL rendering in a separate class
	 */
	public static class MyGLSurfaceView extends GLSurfaceView
	{
		private static SceneRenderer renderer;
		
		float previousX;//the previous coordinates of where the dragging starts
		float previousY;
		
		
		public MyGLSurfaceView(Context context) 
		{
			this(context,null);
		
//			previousX = 384;//initialize as middle of the screen because that's where the hexagon starts, changed from 0,0
//			previousY = 640;
//			
//			
			setEGLContextClientVersion(2); //specify OpenGL ES 2.0
			super.setEGLConfigChooser(8, 8, 8, 8, 16, 0); //may be needed for some targets; specifies 24bit color

			renderer = new SceneRenderer(context);
			setRenderer(renderer); //set the renderer

			/* 
			 * Render the view only when there is a change in the drawing data.
			 * We comment this out when we don't have UI (just animation)
			 */
			
			setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
			//the android tutorial on responding to touch events linked from the homework description told me to make sure I had this method call to increase efficiency in terms of the frame refresh rates.	
		}
		
		

		public static void walk(boolean isForward)
		{
			((SceneRenderer) renderer).walk(isForward);
		}



		public static void controlAnimation()
		{
			((SceneRenderer) renderer).toggleAnimation();
			
		}
		
		public static void toggleDayNight()
		{
//			((SceneRenderer) renderer).toggleDayNightAnimation();
		}

		public MyGLSurfaceView(Context context, AttributeSet attrs)
		{
			super(context, attrs);

			setEGLContextClientVersion(2); //specify OpenGL ES 2.0
			super.setEGLConfigChooser(8, 8, 8, 8, 16, 0); //may be needed for some targets; specifies 24bit color

			renderer = new SceneRenderer(context);
			setRenderer(renderer); //set the renderer

			//render continuously (like for animation). Set to WHEN_DIRTY to manually control redraws (via GLSurfaceView.requestRender())
			setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);//WHEN_DIRTY);
		}
		/**
		 * This method is for responding to touch events. This is like the ActionListener or MouseListener in the Java.awt.event package. Structure of the method from the tutorial linked from the homework.
		 * @param e The MotionEvent object that is triggering the method.
		 */
		public boolean onTouchEvent(MotionEvent e)
		{
		    float x = e.getX();//the coordinates of the location of the touch event
		    float y = e.getY();

		    if(e.getAction()==MotionEvent.ACTION_DOWN)//if the user touches down, then save the soon-to-be-previous touch locations
		    {
		    	previousX = x;
		    	previousY = y;
		    }
		    if(e.getAction()==MotionEvent.ACTION_MOVE)
		    {
		    	//the distances between the click and the previous locations
		    	float dx = x - previousX;
		    	float dy = previousY - y;//the vertical distance should be the first location minus the event location in order to translate to OpenGL coordinates (inverted y axis in OpenGL)

		    	renderer.changeCamera(dx, dy);//pass the event distances to the renderer and change the camera
		    	// * (1.0f / 175), dy * (1.0f / 175)); //TOUCH_SCALE_FACTOR  (180.0f / 320) specified in the tutorial comments linked from assignment description at http://developer.android.com/training/graphics/opengl/touch.html
//		    	INSTEAD, we want to make our own TOUCH_SCALE_FACTOR so that the screen follows the dragging motion that we want: following the "mouse" with approx. a 1:1 ratio of number of screen-pixels the "mouse" moves to the number of screen-pixels the camera moves
		    	requestRender();
		    	
		    	previousX = x;//reset the previous locations for the next movement across the screen
		    	previousY = y;
		    }


		    return true;//assuming success, return true
		}
	}
}
