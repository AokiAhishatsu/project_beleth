#version 430

in vec2 texCoordF;
in vec4 viewSpacePos;
in vec3 position;
in vec3 tangent;

layout(location = 0) out vec4 albedo_out;
layout(location = 1) out vec4 worldPosition_out;
layout(location = 2) out vec4 normal_out;
layout(location = 3) out vec4 specularEmission_out;
layout(location = 4) out vec4 lightScattering_out;

struct Material
{
	sampler2D diffusemap;
	sampler2D normalmap;
	sampler2D heightmap;
	float heightScaling;
	float horizontalScaling;
};

layout (std140, row_major) uniform Camera{
	vec3 eyePosition;
	mat4 m_View;
	mat4 m_ViewProjection;
	vec4 frustumPlanes[6];
};

uniform sampler2D normalmap;
uniform sampler2D splatmap;
uniform float scaleY;
uniform float scaleXZ;
uniform Material materials[4];
uniform float sightRangeFactor;
uniform int largeDetailRange;
uniform int isReflection;
uniform int isRefraction;
uniform int isCameraUnderWater;
uniform vec4 clipplane;
uniform sampler2D dudvCaustics;
uniform sampler2D caustics;
uniform float distortionCaustics;

const float zfar = 10000;
const float znear = 0.1;
const vec3 fogColor = vec3(0.65,0.85,0.9);
const vec3 waterRefractionColor = vec3(0.1,0.125,0.19);

float distancePointPlane(vec3 point, vec4 plane){
	return abs(plane.x*point.x + plane.y*point.y + plane.z*point.z + plane.w) / 
		   abs(sqrt(plane.x * plane.x + plane.y * plane.y + plane.z * plane.z));
}

void main()
{		
	float dist = length(eyePosition - position);
	float height = position.y;
	
	// normalmap/occlusionmap/splatmap coords
	vec2 mapCoords = (position.xz + scaleXZ/2)/scaleXZ; 
	vec3 normal = texture(normalmap, mapCoords).rgb;
	normal = normalize(normal);
	
	vec4 v_splatmap = texture(splatmap, mapCoords).rgba;
	float[4] blendValues = float[](v_splatmap.r,v_splatmap.g,v_splatmap.b,v_splatmap.a);
	
	if (dist < largeDetailRange-50)
	{
		float attenuation = clamp(-dist/(largeDetailRange-50) + 1,0.0,1.0);
		
		vec3 bitangent = normalize(cross(tangent, normal));
		mat3 TBN = mat3(tangent,bitangent,normal);
		
		vec3 bumpNormal;
		for (int i=0; i<4; i++){
			
			bumpNormal += (2*(texture(materials[i].normalmap, texCoordF/materials[i].horizontalScaling).rgb) - 1) * blendValues[i];
		}
		
		bumpNormal = normalize(bumpNormal);
		
		bumpNormal.xy *= attenuation;
		
		normal = normalize(TBN * bumpNormal);
	}
	
	vec3 fragColor = vec3(0,0,0);
	
	for (int i=0; i<4; i++){
		fragColor +=  texture(materials[i].diffusemap, texCoordF/materials[i].horizontalScaling).rgb
					* blendValues[i];
	}
	
	// caustics
	if (isCameraUnderWater == 1 && isRefraction == 0){
		vec2 causticsTexCoord = position.xz / 100;
		vec2 causticDistortion = texture(dudvCaustics, causticsTexCoord*0.2 + distortionCaustics*0.6).rb * 0.18;
		vec3 causticsColor = texture(caustics, causticsTexCoord + causticDistortion).rbg;
		
		fragColor += (causticsColor/5);
	}
	
	// underwater distance blue blur
	if (isCameraUnderWater == 0 && isRefraction == 1){
		
		float distToWaterSurace = distancePointPlane(position,clipplane);
		float refractionFactor = clamp(0.025 * distToWaterSurace,0,1);
		
		fragColor = mix(fragColor, waterRefractionColor, refractionFactor); 
	}
	
	albedo_out = vec4(fragColor,1);
	worldPosition_out = vec4(position,1);
	normal_out = vec4(normal,1);
	specularEmission_out = vec4(1,0,0,1);
	lightScattering_out = vec4(0,0,0,1);
}