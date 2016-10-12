package cs315.kramercanfield.finalproject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
//import java.util.Arrays;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;




//import cs315.yourname.hwk4.ModelFactory;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

/**
 * This class represents a custom OpenGL renderer--all of our "drawing" logic goes here.
 * This is my final project scene. It is a snowy and foggy scene in a grassy courtyard with brick buildings.
 * 
 * @author Joel; code adapted from Google and LearnOpenGLES
 * @version Fall 2013
 * @author Kramer Canfield
 * @version 18 December 2013
 * 
 * Image, Sound, and Object Sources:
 * GRASS    http://www.deviantart.com/morelikethis/179345571
 * BRICK    http://opensim-creations.com/2013/01/24/sunny-seamless-brick/sunny-big-red-brick/
 * SNOW     http://www.418qe.com/my-snowy-life
 * WIND mp3 http://www.soundjay.com/wind-sound-effect.html
 * Tree .obj file:
	"TurboSquid Tree pack" http://www.turbosquid.com/FullPreview/Index.cfm/ID/506851
 *Spheres and Cubes: Joel Ross
 */
public class SceneRenderer implements GLSurfaceView.Renderer 
{
	private static final String TAG = "FINAL PROJECT"; //for logging/debugging
	private Context context; //for accessing assets
	
	//some constants given our model specifications
	public final int POSITION_DATA_SIZE = 3;	
	public final int NORMAL_DATA_SIZE = 3;
	public final int COLOR_DATA_SIZE = 4; //in case we may want it!
	public final int BYTES_PER_FLOAT = 4;
	public final int BYTES_PER_SHORT = 2;
	public final int TEXTURE_DATA_SIZE = 2; //for texturing

	//Matrix storage
	private float[] mModelMatrix = new float[16]; //to store current model matrix
	private float[] mViewMatrix = new float[16];
	private float[] mProjectionMatrix = new float[16];
	private float[] mMVMatrix = new float[16]; //to store the current modelview matrix
	private float[] mMVPMatrix = new float[16]; //combined MVP matrix
	
	//Buffer for model data -- from hwk_4
	private final FloatBuffer mCubeData;
	private final int mCubeVertexCount; //vertex count for the buffer
	private final FloatBuffer mCubeTextureBuffer;//so we can texture a cube
	private final FloatBuffer mSphereData;
	private final int mSphereVertexCount; //vertex count for the buffer
	

	//Color storage
//	private final float[] mColorRed;
//	private final float[] mColorBlue;
	private final float[] mColorGrey;
	//custom colors added
	private final float[] mColorGreen;//the background color of the grass that shows through the image
	private final float[] mColorTreeGreen;//for coloring the tree
	private final float[] mColorWhite;
	private final float[] mColorBrown;//the cow's color
	private final float[] mColorCandyPink;//for the lollipop trees
	private final float[] mColorCandyBlue;
//	private final float[] mColorWaterBlue;//for the small pond
//	private final float[] mColorSkyBlue;

	//axis points (for debugging)
	private final FloatBuffer mAxisBuffer;
	private final int mAxisCount;
	private final float[] axislightNormal = {0,0,3};

	//OpenGL Handles
	private int mPerVertexProgramHandle; //our "program" (OpenGL state) for drawing (uses some lighting!)
	private int mMVMatrixHandle; //the combined ModelView matrix
	private int mMVPMatrixHandle; //the combined ModelViewProjection matrix
	private int mPositionHandle; //the position of a vertex
	private int mNormalHandle; //the position of a vertex
	private int mColorHandle; //the color to paint the model
	
	//for textures
	private int mTextureBufferHandle; //memory location of texture buffer (data)
	private int mTextureHandle; //pointer into shader
	private int mTextureCoordHandle; //pointer into shader
	
//	private int mBumpTextureBufferHandle; //memory location of texture buffer (data)
	private int mBrickTextureBufferHandle; //memory location of texture buffer (data)
	private int mSnowTextureBufferHandle; //memory location of texture buffer (data)
	
	private int mTextureStyleHandle;
	private int mShininessTextureHandle;
	
	//the 3D models
	private final Mesh treeModel;
	//private final Mesh cowModel;
	
	private int lightDirection;//the direction/position of the sunlight
	private float[] sunData;
	private int ambientLight;//the amount of ambient light
	private int daytimeSwitch;//a pointer to an integer in the shader (values of 1 or 0) to ensure a lack of certain kinds of light at night (specular)
	private float[] ambientData;
	private long _time;//time as defined by the system clock
	private boolean isAnimating;//whether or not the scene is animating, true if animations are playing, false otherwise 
	
	private int screenWidth;
	private int screenHeight;
	
	//camera position and the direction it's looking
	private float cameraXPosition;
	private float cameraYPosition;
	private float cameraZPosition;
	
	private float cameraXDirection;
	private float cameraYDirection;
	private float cameraZDirection;
	
	MediaPlayer player;
	
	
	
	
	/**
	 * Constructor should initialize any data we need, such as model data
	 */
	public SceneRenderer(Context context)
	{	
		
		this.context = context; //so we can fetch from assets

		treeModel = new Mesh("tree1a_lod0.obj",context);
		//cowModel = new Mesh("cow.obj",context);
		
		player = new MediaPlayer();//initialize the media player
		
		player.reset();
		//set up the data source of the player (which media to play), then prepare the player to play, then start playback
		//these different methods need to be called because Android's Media Player manages playback as a state machine and certain methods can only be called in the correct state
		//most of the setup must be done with try-catch
		try
		{
			//I got this solution from StackOverflow
			Log.d(TAG, "file: "+context.getAssets().openFd("wind-howl-01.mp3").getLength());
			//player.setDataSource(context.getAssets().openFd("wind-howl-01.mp3")//.getFileDescriptor());
			AssetFileDescriptor afd = context.getAssets().openFd("wind-howl-01.mp3");
			player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (IllegalStateException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		
		
		try 
		{
			player.prepare();
		}
		catch (IllegalStateException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		player.setVolume(100f, 100f);//make sure volume is all the way up
		player.setLooping(true);//make the sounds play over and over
		player.start();//start playing the audio
		

		//Log.d(TAG, "player isPlaying: "+player.isPlaying());
		
		isAnimating = true;//start off animating
		
		//set up some example colors. Can add more as needed!
//		mColorRed = new float[] {0.8f, 0.1f, 0.1f, 1.0f};
//		mColorBlue = new float[] {0.1f, 0.1f, 0.8f, 1.0f};
		mColorGrey = new float[] {0.8f, 0.8f, 0.8f, 1.0f};
		mColorGreen = new float[] {0.0f, 0.2f, 0.0f, 1.0f};
		//set up added colors
		mColorTreeGreen = new float[] {0.2f, 0.69f, 0.2f, 1.0f};
		mColorWhite = new float[] {1.0f, 1.0f, 1.0f, 0.5f};
		mColorBrown = new float[] {0.607f, 0.404f, 0.18f, 1.0f};
		mColorCandyPink = new float[] {1.0f, 0.0f, 0.749f};
		mColorCandyBlue = new float[] {0.0f, 0.667f, 1.0f};
//		mColorWaterBlue = new float[] {0.0f, 0.4196f, 0.784f, 0.1f};
//		mColorSkyBlue = new float[] {128/255f, 154/255f, 208/255f};//{0.1f, 0.1f, 0.8f, 1.0f};
		ModelFactory models = new ModelFactory();

		//axis
		float[] axisData = models.getCoordinateAxis();
		mAxisCount = axisData.length/POSITION_DATA_SIZE;
		mAxisBuffer = ByteBuffer.allocateDirect(axisData.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer(); //generate buffer
		mAxisBuffer.put(axisData); //put the float[] into the buffer and set the position

		//CUBE
		float[] cubeData = models.getCubeData();
		mCubeVertexCount = cubeData.length/(POSITION_DATA_SIZE+NORMAL_DATA_SIZE);
		mCubeData = ByteBuffer.allocateDirect(cubeData.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer(); //generate buffer
		mCubeData.put(cubeData); //put the float[] into the buffer and set the position
		float[] cubeTexData = models.getCubeTextureData();
		mCubeTextureBuffer = ByteBuffer.allocateDirect(cubeTexData.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer(); //generate buffer
		mCubeTextureBuffer.put(cubeTexData); //put the float[] into the buffer and set the position
		
		
		//SPHERE
		float[] sphereData = models.getSphereData(ModelFactory.SMOOTH_SPHERE);
		mSphereVertexCount = sphereData.length/(POSITION_DATA_SIZE+NORMAL_DATA_SIZE);
		mSphereData = ByteBuffer.allocateDirect(sphereData.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer(); //generate buffer
		mSphereData.put(sphereData); //put the float[] into the buffer and set the position

		//SUN POSITION
	   sunData = new float[] {0.0f,0.0f,-3.0f};

	   // initialize the values of how much ambient light to use
	   ambientData = new float[] {0.0f, 0.0f, 0.0f, 1.0f};
		
		
	}

	/**
	 * This method is called when the rendering surface is first created; more initializing stuff goes here.
	 * Initialize OpenGL program components here
	 */
	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) 
	{
//		GLES20.glEnable(GLES20.GL_BLEND);
//		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
		//flags to enable depth work
		GLES20.glEnable(GLES20.GL_CULL_FACE); //remove back faces
		GLES20.glEnable(GLES20.GL_DEPTH_TEST); //enable depth testing
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);

		// Set the background clear color
//		GLES20.glClearColor(0.2f, 0.2f, 0.2f, 1.0f); //Currently a dark grey so we can make sure things are working
		GLES20.glClearColor(0.1f, 0.1f, 0.8f, 1.0f); //make it blue
		
		
		//This is a good place to compile the shaders from Strings into actual executables. We use a helper method for that
		//int vertexShaderHandle = GLUtilities.compileShader(GLES20.GL_VERTEX_SHADER, perVertexShaderCode); //get pointers to the executables		
		int vertexShaderHandle = GLUtilities.loadShader(GLES20.GL_VERTEX_SHADER, "vertexPass.glsl", context);//want to define which shader files to work with
		int fragmentShaderHandle = GLUtilities.loadShader(GLES20.GL_FRAGMENT_SHADER, "pointlightFragment.glsl", context);
		Log.d(TAG,vertexShaderHandle+" "+fragmentShaderHandle); 
		mPerVertexProgramHandle = GLUtilities.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle); //and then we throw them into a program

		//Get pointers to the shader's variables (for use elsewhere)
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mPerVertexProgramHandle, "uMVPMatrix");
		mMVMatrixHandle = GLES20.glGetUniformLocation(mPerVertexProgramHandle, "uMVMatrix");
		mPositionHandle = GLES20.glGetAttribLocation(mPerVertexProgramHandle, "aPosition");
		mNormalHandle = GLES20.glGetAttribLocation(mPerVertexProgramHandle, "aNormal");
		mColorHandle = GLES20.glGetAttribLocation(mPerVertexProgramHandle, "aColor");
		lightDirection = GLES20.glGetUniformLocation(mPerVertexProgramHandle, "lightDir");
		ambientLight = GLES20.glGetUniformLocation(mPerVertexProgramHandle, "AmbientColor");
		daytimeSwitch = GLES20.glGetUniformLocation(mPerVertexProgramHandle, "day");
		

		//the handles for texturing
		mTextureBufferHandle = GLUtilities.loadTexture("free_top_down_grass_texture_2_by_mtprower-d2yrzxf.png", context);//reference the image to use
		mTextureHandle = GLES20.glGetUniformLocation(mPerVertexProgramHandle, "uTexture");
		mTextureCoordHandle = GLES20.glGetAttribLocation(mPerVertexProgramHandle, "aTexCoord");
		
		//mBumpTextureBufferHandle = GLUtilities.loadTexture("WATER.jpg", context);//reference the image to use for bump mapping, this is so we can easily switch between images
		mBrickTextureBufferHandle = GLUtilities.loadTexture("SUNNY-Big-Red-Brick.bmp", context);//reference the image to use
		mSnowTextureBufferHandle = GLUtilities.loadTexture("sf_single.png", context);//reference the image to use
		
		mTextureStyleHandle = GLES20.glGetUniformLocation(mPerVertexProgramHandle, "textureStyle");//make a handle so we can easily switch what "kind" of drawing we're doing, textured or "Phong" shading
		mShininessTextureHandle = GLES20.glGetUniformLocation(mPerVertexProgramHandle, "shininess");//this way we can change the shininess of different models

	}

	/**
	 * Called whenever the surface changes (i.e., size due to rotation). Put viewing initialization stuff 
	 * (that depends on the size) here!
	 */
	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) 
	{
		GLES20.glViewport(0, 0, width, height); // Set the OpenGL viewport (basically the canvas) to the same size as the surface.

		screenWidth = width;
		screenHeight = height;
		
		cameraXPosition = 0f;
		cameraYPosition = 1f;
		cameraZPosition = 0f;
		
		cameraXDirection = 0f;
		cameraYDirection = 0f;
		cameraZDirection = -1f;//start by looking down the negative z axis
		
		//Set View Matrix
		Matrix.setLookAtM(mViewMatrix, 0, 
				0.0f, 0.0f, 1.0f, //eye's location
				0.0f, 0.0f, 0.0f, //point we're looking at
				0.0f, 1.0f, 0.0f //direction that is "up" from our head
				);

		//tweak the camera
		boolean isOverview = false;
		if(isOverview)
		{
			Matrix.translateM(mViewMatrix, 0, 0, 0, -30);//overview
			Matrix.rotateM(mViewMatrix,0, 90, 1f, 0, 0);//overview
		}
		else
		{
			Matrix.translateM(mViewMatrix, 0, cameraXPosition, cameraYPosition, cameraZPosition);//default
			Matrix.rotateM(mViewMatrix,0, 4, 1f, 0, 0);//default
		}
	
		
		//Set Projection Matrix.
		final float ratio = (float) width / height; //aspect ratio
		//final float left = -ratio;	final float right = ratio;
		//final float bottom = -1; final float top = 1;
		final float near = 1.0f; final float far = 50.0f;
		Matrix.perspectiveM(mProjectionMatrix, 0, 90f, ratio, near, far);
	}

	/**
	 * This method is for changing whether or not the scene is animating.
	 */
	public void toggleAnimation()
	{
		isAnimating = !isAnimating;//switch whether or not the robot is dancing
	}
	
	/**
	 * This method changes the camera by rotating around it's local y axis as a response to interaction with the screen/controls.
	 * @param deltaX How far on the screen the user dragged in the horizontal direction.
	 * @param deltaY How far on the screen the user dragged in the vertical direction.
	 */
	public void changeCamera(float deltaX, float deltaY)
	{
		float angleDeltaX = deltaX/screenWidth * 180;//use how far the user dragged across the screen to calculate rotation angles
		float angleDeltaY = deltaY/screenHeight* 90;//use different angles to achieve different effects
		
		/*
		 * Rather than transforming the viewModel, treat the camera as a ray with an origin at the location of the camera, and a direction to the "look-at" point.
		 */
	
		float[] m = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};//set up a new identity matrix and multiply it by the previous direction the camera    
													  //was facing in order to get to the new direction where the camera should be facing
		Matrix.setIdentityM(m, 0);
		
		Matrix.rotateM(m, 0, angleDeltaX, 0, 1.0f, 0);
		Matrix.rotateM(m, 0, angleDeltaY, 1.0f, 0, 0);//now we have a rotation matrix
		
		float[] rhsVec = {cameraXDirection, cameraYDirection, cameraZDirection, 1.0f};
		float[] newDir = {0,0,0,0};
		
		Matrix.multiplyMV(newDir, 0, m, 0, rhsVec, 0);//the new direction to look is the result of multiplying the rotation matrix by the camera's previous direction
		
	
		cameraXDirection = newDir[0];//update the camera directions
		cameraYDirection = newDir[1];
		cameraZDirection = newDir[2];
		//the point to look at is the ray's origin plus the direction (a+d if t==1)
		//Using this technique, we reassign the look-at matrix every time the camera spins
		Matrix.setLookAtM(mViewMatrix, 0, 
				cameraXPosition, cameraYPosition, cameraZPosition, //eye's location
				cameraXPosition + newDir[0], cameraYPosition + newDir[1], cameraZPosition + newDir[2], //point we're looking at
				0.0f, 1.0f, 0.0f //direction that is "up" from our head
				);	
	}
	
	/**
	 * This method tells the camera to move (like walking) either North or South.
	 * @param isForward True if walking forward (same direction as camera is looking), false if going the opposite direction
	 */
	public void walk(boolean isForward)
	{
		double length = Math.sqrt((cameraXDirection*cameraXDirection)+(cameraZDirection*cameraZDirection));
		float[] walk = {(float) (cameraXDirection/length), (float) (cameraZDirection/length)};
		if(isForward)
		{
			cameraXPosition = cameraXPosition + walk[0];//update bo
			cameraZPosition = cameraZPosition + walk[1];
			Matrix.translateM(mViewMatrix, 0, -walk[0], 0, -walk[1]);
		}
		else
		{
			cameraXPosition = cameraXPosition - walk[0];
			cameraZPosition = cameraZPosition - walk[1];
			Matrix.translateM(mViewMatrix, 0, walk[0], 0, walk[1]);
		}
	}
	
	
	/**
	 * This is like our "onDraw" method; it says what to do each frame
	 */
	@Override
	public void onDrawFrame(GL10 unused) 
	{
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT); //start by clearing the screen for each frame

		GLES20.glUseProgram(mPerVertexProgramHandle); //tell OpenGL to use the shader program we've compiled

		// Do a complete rotation every 20 seconds.
		if(isAnimating)
			_time = SystemClock.uptimeMillis() % 20000L;        
		float angleInDegrees = (360.0f / 20000.0f) * ((int) _time);


		GLES20.glUniform1i(daytimeSwitch, 1);//tell the shader it's day time
		
		GLES20.glClearColor(0.64f, 0.64f, 0.64f, 1.0f); //light blue sky in the day
		
		//make the sun move and pass it as a parameter to the shader
		float radius = 2.2f;
		float x = 0;
		float y = 0;
	
		//convert to polar coordinates	
		x = (float) (radius*Math.cos((angleInDegrees*Math.PI)/180));
		y = (float) (radius*Math.sin((angleInDegrees*Math.PI)/180));
		//Log.d(TAG, "x: "+x+"   y: "+y);		
		
		sunData[0] = x;
		sunData[2] = y;
		//use slots in the array 0 and 2 to get the sun to rotate around the desired axis
		
		GLES20.glUniform3fv(lightDirection, 1, sunData, 0);//pass the position to the shader
		
		
		
		/*
		 * ambient lighting
		 */
			float percentOfAmbient = (float) Math.sin(angleInDegrees*Math.PI/180);//use a sine function to get the ambient light to fade in then out over the course of the day
			ambientData[0] = percentOfAmbient;
			ambientData[1] = percentOfAmbient;
			ambientData[2] = percentOfAmbient;
		
		GLES20.glUniform4fv(ambientLight, 1, ambientData, 0);//pass the ambient light to the shader
		
	    drawGrass();
	   
	    drawBuildings();
		
		//set the ambient light back to zero for night time
		ambientData[0] = 0.0f;
		ambientData[1] = 0.0f;
		ambientData[2] = 0.0f;
		GLES20.glUniform4fv(ambientLight, 1, ambientData, 0);
		
		//drawPond();
			
		drawSnow(_time/2000);
		drawTree();
		//drawAxis(); //so we have guides on coordinate axes, for debugging

	}				
	
	/**
	 * Draw the pond that uses "bump mapping."
	 * I got the water normal-map image from http://www.filterforge.com/filters/4141-normal.html
	 */
//	public void drawPond()
//	{
//		//TODO
//		
//		//pass in textures to OpenGL
//	    GLES20.glActiveTexture(GLES20.GL_TEXTURE0); //specify which texture we're going to be using (basically select what will be the "current" texture)
//	    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBumpTextureBufferHandle); //bind the texture to the "current" texture
//		
//		GLES20.glUniform1i(mTextureStyleHandle, 2);//drawing with bump rendering
//		GLES20.glUniform1f(mShininessTextureHandle, 100);//make the pond shinier than the cow, but not as shiny as the lollipop trees
//		
//		Matrix.setIdentityM(mModelMatrix, 0);
////		Matrix.translateM(mModelMatrix, 0, 16.0f, 5.0f, 0.0f);
////		Matrix.scaleM(mModelMatrix, 0, 20.0f, 20f, 1.0f);
//		
//		Matrix.translateM(mModelMatrix, 0, 2.0f, -7f, -4.0f);
//		Matrix.scaleM(mModelMatrix, 0, 1.5f, 6f, 1.5f);
//		Matrix.rotateM(mModelMatrix, 0, -90, 1, 0, 0);
//		drawPackedTriangleBuffer(mCubeData, mCubeVertexCount, mModelMatrix, mColorSkyBlue, false); 
//	}	
	

	/**
	 * This helper method will draw the grass. The grass is a long, wide, flat cube that is covered by the grass texture image.
	 */
	public void drawGrass()
	{
		//pass in textures to OpenGL
	    GLES20.glActiveTexture(GLES20.GL_TEXTURE0); //specify which texture we're going to be using (basically select what will be the "current" texture)
	    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureBufferHandle); //bind the texture to the "current" texture
	    
	    GLES20.glUniform1i(mTextureHandle, 0); //pass the texture into the shader
	  
		//per-model texture passing -- CUBE [works]
		mCubeTextureBuffer.position(0); //reset buffer start to 0 (where data starts)
		GLES20.glVertexAttribPointer(mTextureCoordHandle, TEXTURE_DATA_SIZE, GLES20.GL_FLOAT, false, 0, mCubeTextureBuffer);
		GLES20.glEnableVertexAttribArray(mTextureCoordHandle); //doing this explicitly/separately
		
		GLES20.glUniform1i(mTextureStyleHandle, 1);//tell the shader to use the texture 
		//drawing the grass
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, 0.0f, -2.0f, 0.0f);
		Matrix.scaleM(mModelMatrix, 0, 20.0f, 1.0f, 26.6f);
		drawPackedTriangleBuffer(mCubeData, mCubeVertexCount, mModelMatrix, mColorGreen, false);
	}
	public void drawTree()
	{
		GLES20.glUniform1i(mTextureStyleHandle, 0);//tell the shader not to use an image
		GLES20.glUniform1f(mShininessTextureHandle, 300);//turn the shininess back down
		
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, 5.0f, -0.7f, 0.0f);
		Matrix.scaleM(mModelMatrix, 0, 0.5f, 0.83f, 0.5f);
		drawIndexedTriangleBuffer(treeModel.getVertexDataBuffer(), treeModel.getNormalDataBuffer(),  //draw as an indexed array
				treeModel.getIndexDataBuffer(), treeModel.getIndexDataBufferLength(), 
				mModelMatrix, mColorTreeGreen);
	}
	

	/**
	* This helper method draws all of the buildings in the scene.
	*/
	public void drawBuildings()
	{
		//pass in textures to OpenGL
	    GLES20.glActiveTexture(GLES20.GL_TEXTURE0); //specify which texture we're going to be using (basically select what will be the "current" texture)
	    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBrickTextureBufferHandle); //bind the texture to the "current" texture
	    
	    GLES20.glUniform1i(mTextureHandle, 0); //pass the texture into the shader
	  
		//per-model texture passing -- CUBE [works]
		mCubeTextureBuffer.position(0); //reset buffer start to 0 (where data starts)
		GLES20.glVertexAttribPointer(mTextureCoordHandle, TEXTURE_DATA_SIZE, GLES20.GL_FLOAT, false, 0, mCubeTextureBuffer);
		GLES20.glEnableVertexAttribArray(mTextureCoordHandle); //doing this explicitly/separately
		
		GLES20.glUniform1i(mTextureStyleHandle, 1);//tell the shader to use the texture 
		
		float yPositionAdjust = 5.0f;
		float yScale = 6f;
		float buildingWidth = 1.5f;
		
		
		//drawing the buildings themselves
		
		/*
		 * The North and South buildings
		 */
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, 2.0f, yPositionAdjust, -25.0f);
		Matrix.scaleM(mModelMatrix, 0, 15.0f, yScale, buildingWidth);
		drawPackedTriangleBuffer(mCubeData, mCubeVertexCount, mModelMatrix, mColorBrown, false);
		
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, 2.0f, yPositionAdjust, 25.0f);
		Matrix.scaleM(mModelMatrix, 0, 15.0f, yScale, buildingWidth);
		drawPackedTriangleBuffer(mCubeData, mCubeVertexCount, mModelMatrix, mColorBrown, false);
		
		/*
		 * The West building
		 */
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, -16, yPositionAdjust, 0.0f);
 		Matrix.scaleM(mModelMatrix, 0, buildingWidth, yScale, 20.0f);
		drawPackedTriangleBuffer(mCubeData, mCubeVertexCount, mModelMatrix, mColorBrown, false);
	}
	
	
	
	public void drawSnow(float deltaY)
	{
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
		//flags to enable depth work
		GLES20.glDisable(GLES20.GL_CULL_FACE); //remove back faces
		GLES20.glDisable(GLES20.GL_DEPTH_TEST); //enable depth testing
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
		
		float scaleFactor = 0.12f;

		Random randomGenerator = new Random();
//		float randomYAdjust = randomGenerator.nextFloat();

		
		//pass in textures to OpenGL
	    GLES20.glActiveTexture(GLES20.GL_TEXTURE0); //specify which texture we're going to be using (basically select what will be the "current" texture)
	    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mSnowTextureBufferHandle); //bind the texture to the "current" texture
	    
	    GLES20.glUniform1i(mTextureHandle, 0); //pass the texture into the shader
	  
		//per-model texture passing -- CUBE [works]
		mCubeTextureBuffer.position(0); //reset buffer start to 0 (where data starts)
		GLES20.glVertexAttribPointer(mTextureCoordHandle, TEXTURE_DATA_SIZE, GLES20.GL_FLOAT, false, 0, mCubeTextureBuffer);
		GLES20.glEnableVertexAttribArray(mTextureCoordHandle); //doing this explicitly/separately
		
		GLES20.glUniform1i(mTextureStyleHandle, 1);//tell the shader to use the texture 
		
		float randomX;
		float randomY;
		float randomZ;
		for(int i=0; i<450; i++)
		{
			boolean xIsNegative = randomGenerator.nextBoolean();
			boolean zIsNegative = randomGenerator.nextBoolean();
			
			int xPosNegMultiplier = 1;
			int zPosNegMultiplier = 1;
			if(xIsNegative)
				xPosNegMultiplier = -1;
			if(zIsNegative)
				zPosNegMultiplier = -1;
			
			randomX = 20* xPosNegMultiplier * randomGenerator.nextFloat();
			randomY = 10* randomGenerator.nextFloat();
			randomZ = 20* zPosNegMultiplier * randomGenerator.nextFloat();
			
			Matrix.setIdentityM(mModelMatrix, 0);
			Matrix.translateM(mModelMatrix, 0, 0.0f, randomY, 0f);
			Matrix.translateM(mModelMatrix, 0, randomX, 2-deltaY, randomZ);
			Matrix.scaleM(mModelMatrix, 0, scaleFactor, scaleFactor, scaleFactor);
			drawPackedTriangleBuffer(mCubeData, mCubeVertexCount, mModelMatrix, mColorWhite, false);
		
		}	
	
		GLES20.glDisable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
		//flags to enable depth work
		GLES20.glEnable(GLES20.GL_CULL_FACE); //remove back faces
		GLES20.glEnable(GLES20.GL_DEPTH_TEST); //enable depth testing
	
	}
	
	/**
	 * This helper method draws the lollipop trees (in candy colors).
	 * * (Lollipop trees are large spheres on top of a single, white rectangular trunk)
	 */
	public void drawLollipopTrees()
	{
		GLES20.glUniform1i(mTextureStyleHandle, 0);//tell the shader not to use an image
		
		//trunk 1
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, 0.0f, 0.5f, -2.0f);
		Matrix.scaleM(mModelMatrix, 0, 0.1f, 3.0f, 0.1f);
		drawPackedTriangleBuffer(mCubeData, mCubeVertexCount, mModelMatrix, mColorWhite, false); 
		//trunk 2
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, 2.0f, 0.5f, -2.5f);
		Matrix.scaleM(mModelMatrix, 0, 0.1f, 3.0f, 0.1f);
		drawPackedTriangleBuffer(mCubeData, mCubeVertexCount, mModelMatrix, mColorWhite, false); 
		
		GLES20.glUniform1f(mShininessTextureHandle, 400);//want to make the treetops especially shiny
		
		//treetop 1
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, 0.0f, 3.5f, -2.0f);
		Matrix.scaleM(mModelMatrix, 0, 1.0f, 0.8f, 1.0f);
		drawPackedTriangleBuffer(mSphereData, mSphereVertexCount, mModelMatrix, mColorCandyBlue, false); 
		//treetop 2
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, 2.0f, 3.5f, -2.5f);
		Matrix.scaleM(mModelMatrix, 0, 1.0f, 0.8f, 1.0f);
		drawPackedTriangleBuffer(mSphereData, mSphereVertexCount, mModelMatrix, mColorCandyPink, false); 
		
		GLES20.glUniform1f(mShininessTextureHandle, 300);//turn the shininess back down

		
		
		
	}
	
//	public void drawCow()
//	{
//		GLES20.glUniform1i(mTextureStyleHandle, 0);//tell the shader to not use a texture
//		
//		float xCow = (_time/1000) - 4.0f;//cow x position update
//		GLES20.glUniform1f(mShininessTextureHandle, 150);//make the cow not as shiny as the lollipop trees
//		Matrix.setIdentityM(mModelMatrix, 0);
//		Matrix.translateM(mModelMatrix, 0, xCow, -0.4f, 2.0f);//move the cow to where it needs to be
//		Matrix.scaleM(mModelMatrix, 0, 2.0f, 2.0f, 2.0f);//scale here to adjust the cow model size so it shows up on the screen well
//		drawIndexedTriangleBuffer(cowModel.getVertexDataBuffer(), cowModel.getNormalDataBuffer(),  //draw as an indexed array
//				cowModel.getIndexDataBuffer(), cowModel.getIndexDataBufferLength(), 
//				mModelMatrix, mColorBrown);	
//	}
//	
	private void drawIndexedTriangleBuffer(FloatBuffer vertexBuffer, FloatBuffer normalBuffer, ShortBuffer indexBuffer, int indexCount,
			float[] modelMatrix, float[] color)
	{
		//Calculate MV and MVPMatrix. Note written as MVP, but really P*V*M
		Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, modelMatrix, 0);  //"M * V"
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0); //"MV * P"

		GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVMatrix, 0); //put combined matrixes in the shader variables
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

		vertexBuffer.position(0); //reset buffer start to 0 (where data starts)
		GLES20.glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, 0, vertexBuffer);
		GLES20.glEnableVertexAttribArray(mPositionHandle);

		normalBuffer.position(0); //reset buffer start to 0 (where data starts)
		GLES20.glVertexAttribPointer(mNormalHandle, NORMAL_DATA_SIZE, GLES20.GL_FLOAT, false, 0, normalBuffer);
		GLES20.glEnableVertexAttribArray(mNormalHandle);
		

		//color data
		GLES20.glVertexAttrib4fv(mColorHandle, color, 0);
		
		//setup the index buffer
		indexBuffer.position(0);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer); //draw by indices
		GLUtilities.checkGlError("glDrawElements");
	}


	/**
	 * Draws a triangle buffer with the given modelMatrix and single color. 
	 * Note the view matrix is defined per program.
	 */			
	private void drawPackedTriangleBuffer(FloatBuffer buffer, int vertexCount, float[] modelMatrix, float[] color, boolean hasTexture)
	{		
		//Calculate MV and MVPMatrix. Note written as MVP, but really P*V*M
		Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, modelMatrix, 0);  //"M * V"
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0); //"MV * P"

		GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVMatrix, 0); //put combined matrixes in the shader variables
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);


		int stride = (POSITION_DATA_SIZE + NORMAL_DATA_SIZE) * BYTES_PER_FLOAT; //how big of steps we take through the buffer
		if(hasTexture)
			stride += TEXTURE_DATA_SIZE * BYTES_PER_FLOAT;
		
		buffer.position(0); //reset buffer start to 0 (where the position data starts)
		GLES20.glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, stride, buffer); //note the stride lets us step over the normal data!
		GLES20.glEnableVertexAttribArray(mPositionHandle);

		buffer.position(POSITION_DATA_SIZE); //shift pointer to where the normal data starts
		GLES20.glVertexAttribPointer(mNormalHandle, NORMAL_DATA_SIZE, GLES20.GL_FLOAT, false, stride, buffer); //note the stride lets us step over the position data!
		GLES20.glEnableVertexAttribArray(mNormalHandle);

		if(hasTexture)
		{
			buffer.position(POSITION_DATA_SIZE+NORMAL_DATA_SIZE);
			GLES20.glVertexAttribPointer(mTextureCoordHandle, TEXTURE_DATA_SIZE, GLES20.GL_FLOAT, false, stride, buffer);
			GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
		}
		
		//put color data in the shader variable
		GLES20.glVertexAttrib4fv(mColorHandle, color, 0);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount); //draw the vertices
	}
	//draws the coordinate axis (for debugging)
	@SuppressWarnings("unused")
	private void drawAxis()
	{
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.multiplyMM(mMVMatrix, 0, mModelMatrix, 0, mViewMatrix, 0);  //M * V
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0); //P * MV 

		GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVMatrix, 0);
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

		// Pass in the position information
		mAxisBuffer.position(0); //reset buffer start to 0 (just in case)
		GLES20.glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, 0, mAxisBuffer); 
		GLES20.glEnableVertexAttribArray(mPositionHandle);
		GLES20.glDisableVertexAttribArray(mNormalHandle); //turn off the buffer version of normals
		GLES20.glVertexAttrib3fv(mNormalHandle, axislightNormal, 0); //pass particular normal (so points are bright)

		//GLES20.glDisableVertexAttribArray(mColorHandle); //just in case it was enabled earlier
		GLES20.glVertexAttrib4fv(mColorHandle, mColorGrey, 0); //put color in the shader variable

		GLES20.glDrawArrays(GLES20.GL_POINTS, 0, mAxisCount); //draw the axis (as points!)
	}

	public static void printMatrix(float[] array)
	{
		Log.i("MATRIX","[ " + array[0] + ", "+array[4] + ", "+array[8] + ", "+array[12]);
		Log.i("MATRIX","  " + array[1] + ", "+array[5] + ", "+array[9] + ", "+array[13]);
		Log.i("MATRIX","  " + array[2] + ", "+array[6] + ", "+array[10] + ", "+array[14]);
		Log.i("MATRIX","  " + array[3] + ", "+array[7] + ", "+array[11] + ", "+array[15]+ " ]");
	}	
	
}
