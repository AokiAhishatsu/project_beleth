package core.wrapper.shader;

import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_COMPUTE_BIT;

import org.lwjgl.vulkan.VkDevice;
import core.pipeline.ShaderModule;

public class ComputeShader extends ShaderModule{

	public ComputeShader(VkDevice device, String filePath) {
		super(device, filePath, VK_SHADER_STAGE_COMPUTE_BIT);
	}

}
