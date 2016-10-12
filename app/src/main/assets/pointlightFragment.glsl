//CODE ADAPTED FROM Shreiner et al 2013

varying vec4 vColor; 		//color for the fragment; this was output from the vertexShader
varying vec3 vNormal;		//normal for this fragment
varying vec4 vPosition;		//get our position
varying vec2 vTexCoord;			//interpolate the tex coords	


uniform float shininess;
uniform vec3 lightDir;//the direction or position of the sun
uniform vec4 AmbientColor;
uniform sampler2D uTexture; //the texture buffer (data)
uniform mat4 uMVMatrix; //MVMatrix
uniform int textureStyle;//1 if drawing an image on a model (such as the grass), 0 if drawing objects with ambient, diffuse, and specular lighting, 2 if using bump mapping, 3 for snowflakes
uniform int day;//1 if it is daytime, 0 at night, used to be sure about lighting at day vs. night (for blocking specular) 
//I tried to use this field to lower the diffuse light (in addition to specular light) at night, but that created more problems.

const vec4 Ld = vec4(1.0);
const vec3 LightColor = vec3(1.0);
const vec3 EyeDirection = normalize(vec3(0.0,0.0,1.0)); //direction of the eye
const float cnstAtten = 1;
const float lineAtten = 0;
const float quadAtten = 0;

//extra constant values not used
//const vec4 La = vec4(0.33, 0.22, 0.03, 1.0);
//const float Kd = 1.0;
//const vec4 Ls = vec4(0.99, 0.91, 0.81, 1.0);
//const float Ks = 1.0;
//const float shininess = 27.8;
//const float shininess = 100*4;

void main() 
{
	
	vec3 norm;//make the variable outside of the conditionals so the file loads and compiles without error

	if(textureStyle==2)//if we're using bump mapping
	{
		norm = vec3(texture2D(uTexture, vTexCoord));
		norm = normalize(vec3(uMVMatrix * vec4(norm, 0.0))); //transform the normals based on model/view
	}
	else
	{
		norm = normalize(vNormal);
	}

	//determine light directions
	vec3 lightDirection = lightDir - vPosition.xyz;
	float lightDistance = length(lightDirection);
	lightDirection = lightDirection/lightDistance; //normalize without recomputing length
	
	float attenuation = 1.0 / (cnstAtten + lineAtten*lightDistance + quadAtten*lightDistance*lightDistance);
	
	vec3 halfVec = normalize(lightDirection + EyeDirection);
	
	float diffuse = max(dot(norm, lightDirection), 0.0);
	float specular = max(dot(norm, halfVec), 0.0);
	
	if(day == 0)//if it's night, don't add any specular
		specular = 0.0;
	else
		specular = pow(specular, shininess);

	vec3 scatteredLight = AmbientColor.rgb + (0.8)*vColor.rgb*diffuse*attenuation;
	vec3 reflectedLight = LightColor.rgb*specular*attenuation;
	vec3 rgb = min(vColor.rgb*scatteredLight+reflectedLight, vec3(1.0));

	//gl_FragColor = vec4(rgb, vColor.a);
	
	
	if(textureStyle==1)
	{
		gl_FragColor = vec4( (0.9)*(texture2D(uTexture, vTexCoord).rgb) + (0.1)*(LightColor.rgb*AmbientColor.rgb), 1.0);   // + (vColor.rgb*Ld.rgb*diffuse)) + (0.1)*(rgb), 1.0);//vec4(rgb, vColor.a).rgb);

					vec4 fogGrey = vec4(0.64, 0.64, 0.64, 1.0);
        			float fogPercent = min(22, sqrt(vPosition.x*vPosition.x + vPosition.z*vPosition.z)) / 30;
        			gl_FragColor = (1-fogPercent)*gl_FragColor + (fogPercent)*(fogGrey);
		
	}
	else
	{
		gl_FragColor = vec4( (vColor.rgb*AmbientColor.rgb) + rgb, 1.0);    //vec4(rgb, vColor.a).rgb;//(vColor.rgb*Ld.rgb*diffuse) +
		//NOTE: rgb includes specular and diffuse, so all three lighting types in the Phong model are included
	}
		if(textureStyle != 3)
		{
			vec4 fogGrey = vec4(0.64, 0.64, 0.64, 1.0);
			float fogPercent = min(22, sqrt(vPosition.x*vPosition.x + vPosition.z*vPosition.z)) / 30;
			gl_FragColor = (1-fogPercent)*gl_FragColor + (fogPercent)*(fogGrey);
		}
	
}
