package core.engine;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R16G16B16A16_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R16_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_GENERAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_STORAGE_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_COMPUTE_BIT;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import core.context.BaseContext;
import core.command.CommandBuffer;
import core.descriptor.DescriptorPool;
import core.descriptor.DescriptorSet;
import core.descriptor.DescriptorSetLayout;
import core.device.VkDeviceBundle;
import core.image.VkImage;
import core.image.VkImageView;
import core.pipeline.ShaderModule;
import core.pipeline.VkPipeline;
import core.util.VkUtil;
import core.wrapper.image.Image2DDeviceLocal;
import core.wrapper.shader.ComputeShader;

public class SampleCoverage {
	
	private VkImage sampleCoverageImage;
	private VkImageView sampleCoverageImageView;
	private VkImage lightScatteringImage;
	private VkImageView lightScatteringImageView;
	private VkPipeline computePipeline;
	private DescriptorSet descriptorSet;
	private DescriptorSetLayout descriptorSetLayout;
	
	private ByteBuffer pushConstants;
	private List<DescriptorSet> descriptorSets;
	private int width;
	private int height;
	
	private final float discontinuitiestThreshold = 4f;

	public SampleCoverage(VkDeviceBundle deviceBundle,
			int width, int height, VkImageView worldPositionImageView,
			VkImageView lightScatteringMask) {
		
		VkDevice device = deviceBundle.getLogicalDevice().getHandle();
		VkPhysicalDeviceMemoryProperties memoryProperties = deviceBundle.getPhysicalDevice().getMemoryProperties();
		DescriptorPool descriptorPool = deviceBundle.getLogicalDevice().getDescriptorPool(Thread.currentThread().getId());
		this.width = width;
		this.height = height;
		
		sampleCoverageImage = new Image2DDeviceLocal(device, memoryProperties, 
				width, height, VK_FORMAT_R16_SFLOAT, VK_IMAGE_USAGE_STORAGE_BIT
				| VK_IMAGE_USAGE_SAMPLED_BIT);
		sampleCoverageImageView = new VkImageView(device,
				VK_FORMAT_R16_SFLOAT, sampleCoverageImage.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);
		
		lightScatteringImage = new Image2DDeviceLocal(device, memoryProperties, 
				width, height, VK_FORMAT_R16G16B16A16_SFLOAT, VK_IMAGE_USAGE_STORAGE_BIT
				| VK_IMAGE_USAGE_SAMPLED_BIT);
		lightScatteringImageView = new VkImageView(device,
				VK_FORMAT_R16G16B16A16_SFLOAT, lightScatteringImage.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);
		
		descriptorSetLayout = new DescriptorSetLayout(device, 4);
		descriptorSetLayout.addLayoutBinding(0, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
	    		VK_SHADER_STAGE_COMPUTE_BIT);
		descriptorSetLayout.addLayoutBinding(1, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
	    		VK_SHADER_STAGE_COMPUTE_BIT);
		descriptorSetLayout.addLayoutBinding(2, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
	    		VK_SHADER_STAGE_COMPUTE_BIT);
		descriptorSetLayout.addLayoutBinding(3, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
	    		VK_SHADER_STAGE_COMPUTE_BIT);
		descriptorSetLayout.create();

		descriptorSet = new DescriptorSet(device, descriptorPool.getHandle(),
		    		descriptorSetLayout.getHandlePointer());
		descriptorSet.updateDescriptorImageBuffer(sampleCoverageImageView.getHandle(),
		    	VK_IMAGE_LAYOUT_GENERAL, -1, 0,
		    	VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
		descriptorSet.updateDescriptorImageBuffer(worldPositionImageView.getHandle(),
				VK_IMAGE_LAYOUT_GENERAL, -1, 1,
	    		VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
		descriptorSet.updateDescriptorImageBuffer(lightScatteringImageView.getHandle(),
	    		VK_IMAGE_LAYOUT_GENERAL, -1, 2,
	    		VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
		descriptorSet.updateDescriptorImageBuffer(lightScatteringMask.getHandle(),
	    		VK_IMAGE_LAYOUT_GENERAL, -1, 3,
	    		VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
		
		descriptorSets = new ArrayList<DescriptorSet>();
		List<DescriptorSetLayout> descriptorSetLayouts = new ArrayList<DescriptorSetLayout>();
		
		descriptorSets.add(descriptorSet);
		descriptorSetLayouts.add(descriptorSetLayout);
		
		int pushConstantRange = Float.BYTES * 1 + Integer.BYTES * 1;
		pushConstants = memAlloc(pushConstantRange);
		pushConstants.putInt(BaseContext.getConfig().getMultisamples());
		pushConstants.putFloat(discontinuitiestThreshold);
		pushConstants.flip();
		
		ShaderModule shader = new ComputeShader(device, "res/shaders/sampleCoverage.comp.spv");
		
		computePipeline = new VkPipeline(device);
		computePipeline.setPushConstantsRange(VK_SHADER_STAGE_COMPUTE_BIT, pushConstantRange);
		computePipeline.setLayout(VkUtil.createLongBuffer(descriptorSetLayouts));
		computePipeline.createComputePipeline(shader);
		
		shader.destroy();
	}
	
	public void record(CommandBuffer commandBuffer){
		
		commandBuffer.pushConstantsCmd(computePipeline.getLayoutHandle(),
				VK_SHADER_STAGE_COMPUTE_BIT, pushConstants);
		commandBuffer.bindComputePipelineCmd(computePipeline.getHandle());
		commandBuffer.bindComputeDescriptorSetsCmd(computePipeline.getLayoutHandle(),
				VkUtil.createLongArray(descriptorSets));
		commandBuffer.dispatchCmd(width/16, height/16, 1);
	}
	
	public void shutdown(){
		sampleCoverageImage.destroy();
		sampleCoverageImageView.destroy();
		lightScatteringImage.destroy();
		lightScatteringImageView.destroy();
		computePipeline.destroy();
		descriptorSet.destroy();
		descriptorSetLayout.destroy();
	}

	public VkImageView getSampleCoverageImageView() {
		return sampleCoverageImageView;
	}

	public VkImageView getLightScatteringImageView() {
		return lightScatteringImageView;
	}
	
}
