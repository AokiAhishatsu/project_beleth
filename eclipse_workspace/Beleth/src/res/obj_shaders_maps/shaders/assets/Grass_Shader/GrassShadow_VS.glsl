#version 330

layout (location = 0) in vec3 position0;
layout (location = 2) in vec2 texCoord0;

out int instanceID_GS;
out vec2 texCoord_GS;

void main()
{
	gl_Position = vec4(position0, 1);
	texCoord_GS = texCoord0;
	instanceID_GS = gl_InstanceID;
}




