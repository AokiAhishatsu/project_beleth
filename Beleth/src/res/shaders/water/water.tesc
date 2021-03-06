#version 430

layout(vertices = 16) out;

layout (location = 0) in vec2 inUV[];

layout (location = 0) out vec2 outUV[];

layout(set = 0, binding = 0, std140, row_major) uniform Camera {
	vec3 eyePosition;
	mat4 m_View;
	mat4 m_ViewProjection;
	vec4 frustumPlanes[6];
};

layout (push_constant, std430, row_major) uniform Constants{
	mat4 m_World;
	vec2 windDirection;
	float tessSlope;
	float tessShift;
	int tessFactor;
	int uvScale;
	float displacementScale;
	float choppiness;
	int highDetailRange;
	float kReflection;
	float kRefraction;
	int windowWidth;
	int windowHeight;
	float emission;
	float specular;
} constants;

const int CD = 0;
const int AC = 1;
const int AB = 2;
const int BD = 3;

float LODfactor(float dist)
{
	float tessLevel = max(0.0,constants.tessFactor/(pow(dist, constants.tessSlope)) - constants.tessShift);
	return tessLevel;
}

		
void main(){

	if(gl_InvocationID == 0)
	{
			// D -- B			15	11	7	3
			// |	|			14	10	6	2
			// C -- A			13	9	5	1
			//					12	8	4	0
			
			
			vec3 abMid = vec3((gl_in[0].gl_Position.x + gl_in[3].gl_Position.x)/2,
							  (gl_in[0].gl_Position.y + gl_in[3].gl_Position.y)/2,
							  (gl_in[0].gl_Position.z + gl_in[3].gl_Position.z)/2);
							  
			vec3 bdMid = vec3((gl_in[3].gl_Position.x + gl_in[15].gl_Position.x)/2,
							  (gl_in[3].gl_Position.y + gl_in[15].gl_Position.y)/2,
							  (gl_in[3].gl_Position.z + gl_in[15].gl_Position.z)/2);
							  
			vec3 cdMid = vec3((gl_in[15].gl_Position.x + gl_in[12].gl_Position.x)/2,
							  (gl_in[15].gl_Position.y + gl_in[12].gl_Position.y)/2,
							  (gl_in[15].gl_Position.z + gl_in[12].gl_Position.z)/2);
							  
			vec3 acMid = vec3((gl_in[12].gl_Position.x + gl_in[0].gl_Position.x)/2,
							  (gl_in[12].gl_Position.y + gl_in[0].gl_Position.y)/2,
							  (gl_in[12].gl_Position.z + gl_in[0].gl_Position.z)/2);
		
	
			float distanceAB = distance(abMid, eyePosition);
			float distanceBD = distance(bdMid, eyePosition);
			float distanceCD = distance(cdMid, eyePosition);
			float distanceAC = distance(acMid, eyePosition);
			
			gl_TessLevelOuter [AB] = mix(1, gl_MaxTessGenLevel, LODfactor(distanceAB));
			gl_TessLevelOuter [BD] = mix(1, gl_MaxTessGenLevel, LODfactor(distanceBD));
			gl_TessLevelOuter [CD] = mix(1, gl_MaxTessGenLevel, LODfactor(distanceCD));
			gl_TessLevelOuter [AC] = mix(1, gl_MaxTessGenLevel, LODfactor(distanceAC));
	
			gl_TessLevelInner [0] = (max(gl_TessLevelOuter [BD], gl_TessLevelOuter [AC]))/2;
			gl_TessLevelInner [1] = (max(gl_TessLevelOuter [AB], gl_TessLevelOuter [CD]))/2;
	}
	
	gl_out[ gl_InvocationID ].gl_Position = gl_in[ gl_InvocationID ].gl_Position;
	
	outUV[ gl_InvocationID ] = inUV[ gl_InvocationID ];
}
