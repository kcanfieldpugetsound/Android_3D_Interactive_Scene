package cs315.kramercanfield.finalproject;

import cs315.kramercanfield.finalproject.GLSceneActivity.MyGLSurfaceView;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	/**
	 * This is the method that responds to button presses. (Like the ActionListener in Java)
	 * @param view The button that was pressed.
	 */
	public void buttonPress(View view)
	{
		if(view==this.findViewById(R.id.button))
		{
			MyGLSurfaceView.walk(true);//forward button, so walk in the direction the camera is facing
		}
		if(view==this.findViewById(R.id.button1))
		{
			MyGLSurfaceView.walk(false);//backward button, so walk in the direction opposite the camera is facing
		}
		
	
	}

	
}
