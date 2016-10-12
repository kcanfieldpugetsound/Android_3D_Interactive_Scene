package cs315.kramercanfield.finalproject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

/**
 * A class to hold some utility functions for working with OpenGL (refactoring for readability)
 * @author joel
 */
public class GLUtilities
{
	public static final String TAG = "GLUtilities";


	/**
	 * Prints the given 4x4 matrix to LogCat (info priority).
	 * @param array
	 */
	public static void printMatrix(float[] array)
	{
		Log.i("MATRIX","[ " + array[0] + ", "+array[4] + ", "+array[8] + ", "+array[12]);
		Log.i("MATRIX","  " + array[1] + ", "+array[5] + ", "+array[9] + ", "+array[13]);
		Log.i("MATRIX","  " + array[2] + ", "+array[6] + ", "+array[10] + ", "+array[14]);
		Log.i("MATRIX","  " + array[3] + ", "+array[7] + ", "+array[11] + ", "+array[15]+ " ]");
	}	

	/**
	 * Loads a shader from the assets folder
	 * @param shaderType
	 * @param fileName
	 * @param context
	 * @return
	 */
	public static int loadShader(int shaderType, String fileName, Context context)
	{
		String shaderSource = "";
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					context.getAssets().open(fileName)
					));

			String line = reader.readLine();
			while (line != null) {
				shaderSource += line+"\n";
				line = reader.readLine(); 
			}

			reader.close();
		} catch (IOException e) {
			Log.e(TAG, "Exception: "+Log.getStackTraceString(e));
		}

		return compileShader(shaderType, shaderSource); //call helper method
	}

	/**
	 * Loads a texture from the assets folder
	 * @param filename
	 * @param context
	 * @return
	 */
	public static int loadTexture(String filename, Context context)
	{
		//code adapted from http://www.learnopengles.com/android-lesson-four-introducing-basic-texturing/
		final int[] textureHandle = new int[1];

		GLES20.glGenTextures(1, textureHandle, 0);

		if (textureHandle[0] != 0)
		{
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false;        // No pre-scaling

			Bitmap bitmap = null;
			try {
				bitmap = BitmapFactory.decodeStream(context.getAssets().open(filename)); //load the asset
			} catch (IOException e) {
				Log.e(TAG, "Exception: "+Log.getStackTraceString(e));
			}

			// Bind to the texture in OpenGL
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
			//GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

			// Set filtering levels
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

			// Load the bitmap into the bound texture.
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

			// Recycle the bitmap, since its data has been loaded into OpenGL.
			bitmap.recycle();                                                
		}

		if (textureHandle[0] == 0)
		{
			throw new RuntimeException("Error loading texture.");
		}

		return textureHandle[0];	
	}

	public static int loadTextureCube(String filename, Context context)
	{
		//code adapted from http://www.learnopengles.com/android-lesson-four-introducing-basic-texturing/
		final int[] textureHandle = new int[1];

		GLES20.glGenTextures(1, textureHandle, 0);

		if (textureHandle[0] != 0)
		{
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false;        // No pre-scaling

			Bitmap bitmap = null;
			try {
				bitmap = BitmapFactory.decodeStream(context.getAssets().open(filename)); //load the asset
			} catch (IOException e) {
				Log.e(TAG, "Exception: "+Log.getStackTraceString(e));
			}

			// Bind to the texture in OpenGL
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

			// Set filtering levels
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

			// Load the bitmap into the bound texture.
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

			// Recycle the bitmap, since its data has been loaded into OpenGL.
			bitmap.recycle();                                                
		}

		if (textureHandle[0] == 0)
		{
			throw new RuntimeException("Error loading texture.");
		}

		return textureHandle[0];	
	}

	//FROM: https://code.google.com/p/opengles-book-samples/source/browse/trunk/Android/Ch9_Simple_TextureCubemap/src/com/openglesbook/simpletexturecubemap/SimpleTextureCubemapRenderer.java?r=36
	public static int createSimpleTextureCubemap()
	{
		int[] textureId = new int[1];

		byte[] red = { 127, 0, 0 }; 
		byte[] green = { 0, 127, 0 }; 
		byte[] blue = { 0, 0, 127 };
		byte[] cyan = { 0, 127, 127 };
		byte[] magenta = { 127, 0, 127 };
		byte[] yellow = { 127, 127, 0 };

		ByteBuffer cubePixels = ByteBuffer.allocateDirect(3);

		// Generate a texture object
		GLES20.glGenTextures ( 1, textureId, 0 );

		// Bind the texture object
		GLES20.glBindTexture ( GLES20.GL_TEXTURE_CUBE_MAP, textureId[0] );

		// Load the cube face - Positive X
		cubePixels.put(red).position(0);
		GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GLES20.GL_RGB, 1, 1, 0, 
				GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, cubePixels );

		// Load the cube face - Negative X
		cubePixels.put(green).position(0);
		GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GLES20.GL_RGB, 1, 1, 0, 
				GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, cubePixels );

		// Load the cube face - Positive Y
		cubePixels.put(blue).position(0);        
		GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GLES20.GL_RGB, 1, 1, 0, 
				GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, cubePixels );

		// Load the cube face - Negative Y
		cubePixels.put(yellow).position(0);
		GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GLES20.GL_RGB, 1, 1, 0, 
				GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, cubePixels );

		// Load the cube face - Positive Z
		cubePixels.put(magenta).position(0);        
		GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GLES20.GL_RGB, 1, 1, 0, 
				GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, cubePixels );

		// Load the cube face - Negative Z
		cubePixels.put(cyan).position(0);
		GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GLES20.GL_RGB, 1, 1, 0, 
				GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, cubePixels );

		// Set the filtering mode
		GLES20.glTexParameteri ( GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST );
		GLES20.glTexParameteri ( GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST );

		return textureId[0];
	}

	public static int createTextureCubemap(String filename, Context context)
	{
		//grab the image
		//grab the pixels of the image
		//throw them where they need to go!
		
		int[] textureId = new int[1];
		GLES20.glGenTextures ( 1, textureId, 0 );
		GLES20.glBindTexture ( GLES20.GL_TEXTURE_CUBE_MAP, textureId[0] );
		
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;        // No pre-scaling

		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeStream(context.getAssets().open(filename)); //load the asset
		} catch (IOException e) {
			Log.e(TAG, "Exception: "+Log.getStackTraceString(e));
		}

		// Set the filtering mode
		GLES20.glTexParameteri ( GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST );
		GLES20.glTexParameteri ( GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST );
//		GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		
//		GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP, 0, bitmap, 0);
//		bitmap.recycle();
		
		//brute force our way into having separate buffers
		int subWidth = bitmap.getWidth()/4;
		int subHeight = bitmap.getHeight()/3;
		
		if((subWidth & (subWidth - 1)) != 0) //some error checking...
			throw new RuntimeException("Error: cubemap section width is not a power of 2: "+subWidth);
		if((subHeight & (subHeight - 1)) != 0)
			throw new RuntimeException("Error: cubemap section height is not a power of 2: "+subHeight);
		
		Bitmap top = Bitmap.createBitmap(bitmap, subWidth, 0, subWidth, subHeight); //top
		Bitmap left = Bitmap.createBitmap(bitmap, 0, subHeight, subWidth, subHeight); //left
		Bitmap front = Bitmap.createBitmap(bitmap, subWidth, subHeight, subWidth, subHeight); //back
		Bitmap right = Bitmap.createBitmap(bitmap, 2*subWidth, subHeight, subWidth, subHeight); //right
		Bitmap back = Bitmap.createBitmap(bitmap, 3*subWidth, subHeight, subWidth, subHeight); //front
		Bitmap bottom = Bitmap.createBitmap(bitmap, subWidth, 2*subHeight, subWidth, subHeight); //bottom
		bitmap.recycle(); //throw out old bitmap

		ByteBuffer pixelBuffer = ByteBuffer.allocateDirect(subWidth*subHeight*4);

		//manually copy in each bitmap to the texture map
		pixelBuffer.position(0);
		right.copyPixelsToBuffer(pixelBuffer);
		pixelBuffer.position(0);
		GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GLES20.GL_RGBA, subWidth, subHeight, 0, 
				GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
		right.recycle(); //throw it out
		
		pixelBuffer.position(0);
		left.copyPixelsToBuffer(pixelBuffer);
		pixelBuffer.position(0);
		GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GLES20.GL_RGBA, subWidth, subHeight, 0, 
				GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
		left.recycle(); //throw it out

		pixelBuffer.position(0);
		top.copyPixelsToBuffer(pixelBuffer);
		pixelBuffer.position(0);
		GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GLES20.GL_RGBA, subWidth, subHeight, 0, 
				GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
		top.recycle(); //throw it out

		pixelBuffer.position(0);
		bottom.copyPixelsToBuffer(pixelBuffer);
		pixelBuffer.position(0);
		GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GLES20.GL_RGBA, subWidth, subHeight, 0, 
				GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
		bottom.recycle(); //throw it out

		pixelBuffer.position(0);
		front.copyPixelsToBuffer(pixelBuffer);
		pixelBuffer.position(0);
		GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GLES20.GL_RGBA, subWidth, subHeight, 0, 
				GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
		front.recycle(); //throw it out

		pixelBuffer.position(0);
		back.copyPixelsToBuffer(pixelBuffer);
		pixelBuffer.position(0);
		GLES20.glTexImage2D (GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GLES20.GL_RGBA, subWidth, subHeight, 0, 
				GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
		back.recycle(); //throw it out

		return textureId[0];
	}
	
	
	/** 
	 * Helper function to compile a shader. Reports syntax errors in shader language
	 * 
	 * @param shaderType The shader type.
	 * @param shaderSource The shader source code.
	 * @return An OpenGL handle to the shader.
	 */
	public static int compileShader(int shaderType, String shaderSource) 
	{
		int shaderHandle = GLES20.glCreateShader(shaderType);

		if (shaderHandle != 0) 
		{
			// Pass in the shader source.
			GLES20.glShaderSource(shaderHandle, shaderSource);

			// Compile the shader.
			GLES20.glCompileShader(shaderHandle);

			// Get the compilation status.
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

			// If the compilation failed, delete the shader.
			if (compileStatus[0] == 0) 
			{
				Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shaderHandle));
				GLES20.glDeleteShader(shaderHandle);
				shaderHandle = 0;
			}
		}

		if (shaderHandle == 0)
		{			
			throw new RuntimeException("Error creating shader.");
		}

		return shaderHandle;
	}	

	/**
	 * Launder for supporting old code; passes in no attributes to bind.
	 */
	public static int createAndLinkProgram(final int vertexShaderHandle, final int fragmentShaderHandle)
	{
		return createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, null);
	}

	/**
	 * Helper function to compile and link a program.
	 * 
	 * @param vertexShaderHandle An OpenGL handle to an already-compiled vertex shader.
	 * @param fragmentShaderHandle An OpenGL handle to an already-compiled fragment shader.
	 * @param attributes Attributes that need to be bound to the program.
	 * @return An OpenGL handle to the program.
	 */
	public static int createAndLinkProgram(final int vertexShaderHandle, final int fragmentShaderHandle, final String[] bindAttribs) 
	{
		int programHandle = GLES20.glCreateProgram();

		if (programHandle != 0) 
		{
			// Bind the vertex shader to the program.
			GLES20.glAttachShader(programHandle, vertexShaderHandle);			

			// Bind the fragment shader to the program.
			GLES20.glAttachShader(programHandle, fragmentShaderHandle);

			// Bind attributes
			if (bindAttribs != null)
			{
				final int len = bindAttribs.length;
				for (int i=0; i < len; i++)
				{
					GLES20.glBindAttribLocation(programHandle, i, bindAttribs[i]);
				}                                                
			}

			// Link the two shaders together into a program.
			GLES20.glLinkProgram(programHandle);

			// Get the link status.
			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

			// If the link failed, delete the program.
			if (linkStatus[0] == 0) 
			{				
				Log.e(TAG, "Error compiling program: " + GLES20.glGetProgramInfoLog(programHandle));
				GLES20.glDeleteProgram(programHandle);
				programHandle = 0;
			}
		}

		if (programHandle == 0)
		{
			throw new RuntimeException("Error creating program.");
		}

		return programHandle;
	}

	/**
	 * Helper method for debugging OpenGL
	 * From Google
	 */
	public static void checkGlError(String op) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(TAG, op + ": glError " + error);
			throw new RuntimeException(op + ": glError " + error);
		}
	}





}
