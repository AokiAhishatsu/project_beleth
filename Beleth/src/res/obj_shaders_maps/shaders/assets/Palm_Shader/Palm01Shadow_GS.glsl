#version 430

layout(triangles, invocations = 6) in;

layout(triangle_strip, max_vertices = 3) out;

in int instanceID_GS[];

layout (std140, row_major) uniform Camera{
	vec3 eyePosition;
	mat4 m_View;
	mat4 viewProjectionMatrix;
	vec4 frustumPlanes[6];
};

layout (std140, row_major) uniform worldMatrices{
	mat4 m_World[100];
};

layout (std140, row_major) uniform LightViewProjections{
	mat4 m_lightViewProjection[6];
};

uniform vec4 clipplane;
uniform int matrixIndices[100];

void main()
{	
		for (int i = 0; i < gl_in.length(); ++i)
		{
			gl_Layer = gl_InvocationID;
			vec4 worldPos = m_World[matrixIndices[instanceID_GS[i]]] * gl_in[i].gl_Position;
			gl_Position = m_lightViewProjection[ gl_InvocationID ] * worldPos;
			// gl_ClipDistance[0] = dot(gl_Position,frustumPlanes[0]);
			// gl_ClipDistance[1] = dot(gl_Position,frustumPlanes[1]);
			// gl_ClipDistance[2] = dot(gl_Position,frustumPlanes[2]);
			// gl_ClipDistance[3] = dot(gl_Position,frustumPlanes[3]);
			// gl_ClipDistance[4] = dot(gl_Position,frustumPlanes[4]);
			// gl_ClipDistance[5] = dot(gl_Position,frustumPlanes[5]);
			gl_ClipDistance[6] = dot(worldPos,clipplane);
			EmitVertex();
		}	
		EndPrimitive();
}
