package core.wrapper.pipeline;

import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_PATCH_LIST;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDevice;
import core.pipeline.ShaderPipeline;
import core.pipeline.VkPipeline;
import core.pipeline.VkVertexInput;

public class GraphicsTessellationPipeline extends VkPipeline{

	public GraphicsTessellationPipeline(VkDevice device, ShaderPipeline shaderPipeline,
			VkVertexInput vertexInput, LongBuffer layout, int width, int height,
			long renderPass, int colorAttachmentCount, int samples,
			int pushConstantRange, int pushConstantStageFlags,
			int patchControlPoints) {
		
		super(device);
		
		setVertexInput(vertexInput);
		setPushConstantsRange(pushConstantStageFlags, pushConstantRange);
		setInputAssembly(VK_PRIMITIVE_TOPOLOGY_PATCH_LIST);
		setViewportAndScissor(width, height);
		setRasterizer();
		setMultisamplingState(samples);
		for (int i=0; i<colorAttachmentCount; i++){
			addColorBlendAttachment();
		}
		setColorBlendState();
		setDepthAndStencilTest(true);
		setDynamicState();
		setLayout(layout);
		setTessellationState(patchControlPoints);
		createGraphicsPipeline(shaderPipeline, renderPass);
	}
	
	public GraphicsTessellationPipeline(VkDevice device, ShaderPipeline shaderPipeline,
			VkVertexInput vertexInput, LongBuffer layout, int width, int height,
			long renderPass, int colorAttachmentCount,
			int samples, int patchControlPoints) {
		
		super(device);
		
		setVertexInput(vertexInput);
		setInputAssembly(VK_PRIMITIVE_TOPOLOGY_PATCH_LIST);
		setViewportAndScissor(width, height);
		setRasterizer();
		setMultisamplingState(samples);
		for (int i=0; i<colorAttachmentCount; i++){
			addColorBlendAttachment();
		}
		setColorBlendState();
		setDepthAndStencilTest(true);
		setDynamicState();
		setLayout(layout);
		setTessellationState(patchControlPoints);
		createGraphicsPipeline(shaderPipeline, renderPass);
	}

}
