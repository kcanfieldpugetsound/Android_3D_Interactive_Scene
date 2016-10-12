package cs315.kramercanfield.finalproject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

/**
 * 
 * @author joel
 * @version Oct 21, 2013
 */
public class Mesh
{
	public static final String TAG = "Mesh";

	public final int POSITION_DATA_SIZE = 3; //elements per pos
	public final int NORMAL_DATA_SIZE = 3; //elements per norm
	public final int TEXTURE_DATA_SIZE = 2; //elements per tex
	public final int BYTES_PER_FLOAT = 4;
	public final int BYTES_PER_SHORT = 2;

	private float[] vertexCoords;
	private float[] normalCoords;
	private short[] vertexIndices;
	private short[] normalIndices;
	private float[] packedBuffer;
	private float[] packedTexBuffer;
	private float[] textureCoords;
	private short[] textureIndices;

	public Mesh(String fileName, Context context)
	{
		loadFile(fileName, context); //loads a mesh from the given filename (found in assets)
	}

	private void loadFile(String fileName, Context context)
	{
		Log.i(TAG,"Loading "+fileName);

		ArrayList<Float> vertices = new ArrayList<Float>();
		ArrayList<Float> normals = new ArrayList<Float>();
		ArrayList<Short> vertexI = new ArrayList<Short>();
		ArrayList<Short> normalI = new ArrayList<Short>();
		ArrayList<Float> textures = new ArrayList<Float>();
		ArrayList<Short> textureI = new ArrayList<Short>();
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					context.getAssets().open(fileName)
					));

			String type;
			String[] split, comps;
			String line = reader.readLine();
			while (line != null) 
			{
				//				Log.i(TAG,"reading: "+line);

				split = line.split("( +)"); //supposedly StringTokenizer is faster, if speed is really a thing...
				if(split.length < 1) //make sure line wasn't empty
				{
					line = reader.readLine(); //grab next line
					continue;
				}

				type = split[0];
				if(type.equals("v"))
				{
					vertices.add(Float.parseFloat(split[1]));
					vertices.add(Float.parseFloat(split[2]));
					vertices.add(Float.parseFloat(split[3])); //assuming a vec3 -- check the model!
				}
				else if(type.equals("vn"))
				{
					normals.add(Float.parseFloat(split[1]));
					normals.add(Float.parseFloat(split[2]));
					normals.add(Float.parseFloat(split[3]));
				}
				else if(type.equals("vt"))
				{
					textures.add(Float.parseFloat(split[1]));
					textures.add(Float.parseFloat(split[2]));
				}
				
				/*
				 * texture handling would go here; similar process to above
				 */
				else if(type.equals("f"))
				{
					//tmp = sc.nextLine().trim(); //1 2 3 
					int slash = split[1].indexOf('/');
					for(int i=1; i<split.length; i++)
					{
						if(split[i].equals("")) //make sure we don't have an empty component
							continue;

						if(slash < 0) //single coord per face
						{
							vertexI.add(Short.parseShort(split[i]));
						}
						else //multiple coords per face
						{
							comps = split[i].split("/");
							vertexI.add((short)(Short.parseShort(comps[0])-1)); //face's index 1 is really 0
							if(!comps[1].equals(""));
								textureI.add((short)(Short.parseShort(comps[1])-1)); //add texture
							if(comps.length > 2)
								normalI.add((short)(Short.parseShort(comps[2])-1));
						}
					}
				}

				line = reader.readLine(); //grab next line
			}

			reader.close();
		} catch (IOException e) {
			Log.e(TAG, "Exception: "+Log.getStackTraceString(e));
		}

		//convert to arrays
		vertexCoords = new float[vertices.size()];
		normalCoords = new float[normals.size()];
		vertexIndices = new short[vertexI.size()];
		normalIndices = new short[normalI.size()];
		textureCoords = new float[textures.size()];
		textureIndices = new short[textureI.size()];
		for(int i=0; i<vertexCoords.length; i++)
			vertexCoords[i] = vertices.get(i);
		for(int i=0; i<normalCoords.length; i++)
			normalCoords[i] = normals.get(i);
		for(int i=0; i<vertexIndices.length; i++)
			vertexIndices[i] = vertexI.get(i);
		for(int i=0; i<normalIndices.length; i++)
			normalIndices[i] = normalI.get(i);
		for(int i=0; i<textureCoords.length; i++)
			textureCoords[i] = textures.get(i);
		for(int i=0; i<textureIndices.length; i++)
			textureIndices[i] = textureI.get(i);
		
		//		Log.i(TAG,Arrays.toString(vertexCoords));
		//		Log.i(TAG,Arrays.toString(normalCoords));
		//		Log.i(TAG,Arrays.toString(vertexIndices));
		//		Log.i(TAG,Arrays.toString(normalIndices));
		//		Log.i(TAG,Arrays.toString(textureCoords));
		//		Log.i(TAG,Arrays.toString(textureIndices));
		Log.i(TAG, "Finished loading "+fileName);
	}

	public float[] getPackedBuffer()
	{
		if(packedBuffer==null) //if already filled, just return
		{
			//go through the face indices, building up a non-indexed array of [pt, norm, pt, norm]
			packedBuffer = new float[vertexIndices.length*(POSITION_DATA_SIZE+NORMAL_DATA_SIZE)]; //size is numFaces*3*pos_size*norm_size
			int pCounter = 0;
			for(int i=0; i<vertexIndices.length; i++)
			{
				//add in the position
				packedBuffer[pCounter++] = vertexCoords[POSITION_DATA_SIZE*vertexIndices[i]];
				packedBuffer[pCounter++] = vertexCoords[POSITION_DATA_SIZE*vertexIndices[i]+1];
				packedBuffer[pCounter++] = vertexCoords[POSITION_DATA_SIZE*vertexIndices[i]+2];

				//add in the normal
				packedBuffer[pCounter++] = normalCoords[NORMAL_DATA_SIZE*normalIndices[i]];
				packedBuffer[pCounter++] = normalCoords[NORMAL_DATA_SIZE*normalIndices[i]+1];
				packedBuffer[pCounter++] = normalCoords[NORMAL_DATA_SIZE*normalIndices[i]+2];
			}
		}		
		return packedBuffer;
	}

	public float[] getPackedTexBuffer()
	{
		if(packedTexBuffer==null) //if already filled, just return
		{
			//go through the face indices, building up a non-indexed array of [pt, norm, pt, norm]
			packedTexBuffer = new float[vertexIndices.length*(POSITION_DATA_SIZE+NORMAL_DATA_SIZE*TEXTURE_DATA_SIZE)]; //size is numFaces*3*pos_size*norm_size
			int pCounter = 0;
			for(int i=0; i<vertexIndices.length; i++)
			{
				//add in the position
				packedTexBuffer[pCounter++] = vertexCoords[POSITION_DATA_SIZE*vertexIndices[i]];
				packedTexBuffer[pCounter++] = vertexCoords[POSITION_DATA_SIZE*vertexIndices[i]+1];
				packedTexBuffer[pCounter++] = vertexCoords[POSITION_DATA_SIZE*vertexIndices[i]+2];

				//add in the normal
				packedTexBuffer[pCounter++] = normalCoords[NORMAL_DATA_SIZE*normalIndices[i]];
				packedTexBuffer[pCounter++] = normalCoords[NORMAL_DATA_SIZE*normalIndices[i]+1];
				packedTexBuffer[pCounter++] = normalCoords[NORMAL_DATA_SIZE*normalIndices[i]+2];

				//add in the texture
				packedTexBuffer[pCounter++] = textureCoords[TEXTURE_DATA_SIZE*textureIndices[i]];
				packedTexBuffer[pCounter++] = textureCoords[TEXTURE_DATA_SIZE*textureIndices[i]+1];
			}
		}		
		return packedTexBuffer;
	}	
	
	public float[] getVertices()
	{
		return vertexCoords;
	}

	public float[] getNormals()
	{
		return normalCoords;
	}

	public short[] getVertexIndices()
	{
		return vertexIndices;
	}

	public short[] getNormalIndices()
	{
		return normalIndices;
	}

	public float[] getTextures()
	{
		return textureCoords;
	}
	
	public short[] getTextureIndices()
	{
		return textureIndices;
	}
	
	private FloatBuffer modelDataBuffer;
	private int modelDataBufferLength;
	private FloatBuffer vertexDataBuffer;
	private int vertexDataBufferLength;
	private FloatBuffer normalDataBuffer;
	private int normalDataBufferLength;
	private ShortBuffer indexDataBuffer;
	private int indexDataBufferLength;

	private FloatBuffer textureDataBuffer;
	private int textureDataBufferLength;
	
	public FloatBuffer getModelDataBuffer()
	{
		getPackedBuffer(); //generate if not
		if(modelDataBuffer == null)
		{
			modelDataBufferLength = packedBuffer.length/(POSITION_DATA_SIZE+NORMAL_DATA_SIZE);
			modelDataBuffer = ByteBuffer.allocateDirect(packedBuffer.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer(); //generate buffer
			modelDataBuffer.put(packedBuffer); //put the float[] into the buffer and set the position			
		}
		return modelDataBuffer;
	}

	public int getModelDataBufferLength()
	{
		return modelDataBufferLength;
	}

	public FloatBuffer getModelDataTexBuffer()
	{
		getPackedTexBuffer(); //generate if not
		if(textureDataBuffer == null)
		{
			textureDataBufferLength = packedTexBuffer.length/(POSITION_DATA_SIZE+NORMAL_DATA_SIZE+TEXTURE_DATA_SIZE);
			textureDataBuffer = ByteBuffer.allocateDirect(packedTexBuffer.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer(); //generate buffer
			textureDataBuffer.put(packedTexBuffer); //put the float[] into the buffer and set the position			
		}
		return textureDataBuffer;
	}

	public int getModelDataTexBufferLength()
	{
		return textureDataBufferLength;
	}
	
	public FloatBuffer getVertexDataBuffer()
	{
		if(vertexDataBuffer == null)
		{
			vertexDataBufferLength = vertexCoords.length/POSITION_DATA_SIZE;
			vertexDataBuffer = ByteBuffer.allocateDirect(vertexCoords.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer(); //generate buffer
			vertexDataBuffer.put(vertexCoords); //put the float[] into the buffer and set the position			
		}
		return vertexDataBuffer;
	}

	public int getVertexDataBufferLength()
	{
		return vertexDataBufferLength;
	}

	public FloatBuffer getNormalDataBuffer()
	{
		if(normalDataBuffer == null)
		{
			normalDataBufferLength = normalCoords.length/POSITION_DATA_SIZE;
			normalDataBuffer = ByteBuffer.allocateDirect(normalCoords.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer(); //generate buffer
			normalDataBuffer.put(normalCoords); //put the float[] into the buffer and set the position
		}
		return normalDataBuffer;

	}

	public int getNormalDataBufferLength()
	{
		return normalDataBufferLength;
	}

	public FloatBuffer getTextureDataBuffer()
	{
		if(textureDataBuffer == null)
		{
			textureDataBufferLength = textureCoords.length/TEXTURE_DATA_SIZE;
			textureDataBuffer = ByteBuffer.allocateDirect(textureCoords.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer(); //generate buffer
			textureDataBuffer.put(textureCoords); //put the float[] into the buffer and set the position
		}
		return textureDataBuffer;

	}

	public int getTextureDataBufferLength()
	{
		return textureDataBufferLength;
	}

	
	public ShortBuffer getIndexDataBuffer()
	{
		if(indexDataBuffer == null)
		{
			indexDataBufferLength = vertexIndices.length;
			indexDataBuffer = ByteBuffer.allocateDirect(vertexIndices.length * BYTES_PER_SHORT).order(ByteOrder.nativeOrder()).asShortBuffer(); //generate buffer
			indexDataBuffer.put(vertexIndices); //put the float[] into the buffer and set the position
		}

		return indexDataBuffer;
	}

	public int getIndexDataBufferLength()
	{
		return indexDataBufferLength;
	}



}
