package core.components.atmosphere;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_SHADER_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_UNORM;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_POINT_LIST;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_GRAPHICS_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_MIPMAP_MODE_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import core.context.BaseContext;
import core.math.Vec3f;
import core.model.Vertex.VertexLayout;
import core.scenegraph.NodeComponentType;
import core.scenegraph.Renderable;
import core.util.BufferUtil;
import core.command.CommandBuffer;
import core.context.DeviceManager.DeviceType;
import core.context.VkContext;
import core.descriptor.DescriptorSet;
import core.descriptor.DescriptorSetLayout;
import core.device.LogicalDevice;
import core.image.VkImage;
import core.image.VkImageView;
import core.image.VkSampler;
import core.memory.VkBuffer;
import core.pipeline.ShaderPipeline;
import core.pipeline.VkPipeline;
import core.pipeline.VkVertexInput;
import core.scenegraph.VkMeshData;
import core.scenegraph.VkRenderInfo;
import core.util.VkUtil;
import core.wrapper.buffer.VkBufferHelper;
import core.wrapper.command.SecondaryDrawCmdBuffer;
import core.wrapper.image.VkImageBundle;
import core.wrapper.image.VkImageHelper;
import core.wrapper.pipeline.GraphicsPipeline;

public class Sun extends Renderable{
	
	private VkImageBundle sunImageBundle;
	private VkImageBundle sunImageBundle_lightScattering;

	public Sun() {
		
		LogicalDevice device = VkContext.getDeviceManager().getLogicalDevice(DeviceType.MAJOR_GRAPHICS_DEVICE);
		VkPhysicalDeviceMemoryProperties memoryProperties = 
				VkContext.getDeviceManager().getPhysicalDevice(DeviceType.MAJOR_GRAPHICS_DEVICE).getMemoryProperties();
		
		getWorldTransform().setTranslation(new Vec3f(-2,-1.4f,-2).normalize().mul(-2600));
		Vec3f origin = new Vec3f(0,0,0);
		Vec3f[] array = new Vec3f[1];
		array[0] = origin;
		
		VkImage sunImage = VkImageHelper.loadImageFromFile(
				device.getHandle(), memoryProperties,
				device.getTransferCommandPool(Thread.currentThread().getId()).getHandle(),
				device.getTransferQueue(),
				"textures/sun/sun.png",
				VK_IMAGE_USAGE_SAMPLED_BIT,
				VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
				VK_ACCESS_SHADER_READ_BIT,
				VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
				VK_QUEUE_GRAPHICS_BIT);
		
		VkImageView sunImageView = new VkImageView(device.getHandle(),
				VK_FORMAT_R8G8B8A8_UNORM, sunImage.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);
		
		VkSampler sunImageSampler = new VkSampler(device.getHandle(), VK_FILTER_LINEAR,
				false, 0, VK_SAMPLER_MIPMAP_MODE_LINEAR, 0, VK_SAMPLER_ADDRESS_MODE_REPEAT);
		
		sunImageBundle = new VkImageBundle(sunImage, sunImageView, sunImageSampler);
		
		VkImage sunImage_lightScattering = VkImageHelper.loadImageFromFile(
				device.getHandle(), memoryProperties,
				device.getTransferCommandPool(Thread.currentThread().getId()).getHandle(),
				device.getTransferQueue(),
				"textures/sun/sun_small1.png",
				VK_IMAGE_USAGE_SAMPLED_BIT,
				VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
				VK_ACCESS_SHADER_READ_BIT,
				VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
				VK_QUEUE_GRAPHICS_BIT);
		
		VkImageView sunImageView_lightScattering = new VkImageView(device.getHandle(),
				VK_FORMAT_R8G8B8A8_UNORM, sunImage_lightScattering.getHandle(), VK_IMAGE_ASPECT_COLOR_BIT);
		
		VkSampler sunImageSampler_lightScattering = new VkSampler(device.getHandle(), VK_FILTER_LINEAR,
				false, 0, VK_SAMPLER_MIPMAP_MODE_LINEAR, 0, VK_SAMPLER_ADDRESS_MODE_REPEAT);
		
		sunImageBundle_lightScattering = new VkImageBundle(sunImage_lightScattering,
				sunImageView_lightScattering, sunImageSampler_lightScattering);
		
		VkVertexInput vertexInput = new VkVertexInput(VertexLayout.POS);
		ByteBuffer vertexBuffer = BufferUtil.createByteBuffer(array);
		
		VkBuffer vertexBufferObject = VkBufferHelper.createDeviceLocalBuffer(
				device.getHandle(), memoryProperties,
				device.getTransferCommandPool(Thread.currentThread().getId()).getHandle(),
				device.getTransferQueue(),
				vertexBuffer, VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		
		List<DescriptorSet> descriptorSets = new ArrayList<DescriptorSet>();
		List<DescriptorSetLayout> descriptorSetLayouts = new ArrayList<DescriptorSetLayout>();
		
		DescriptorSetLayout descriptorSetLayout = new DescriptorSetLayout(device.getHandle(), 2);
	    descriptorSetLayout.addLayoutBinding(0, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
	    		VK_SHADER_STAGE_FRAGMENT_BIT);
	    descriptorSetLayout.addLayoutBinding(1, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
	    		VK_SHADER_STAGE_FRAGMENT_BIT);
	    descriptorSetLayout.create();
	    
	    DescriptorSet descriptorSet = new DescriptorSet(device.getHandle(),
	    		device.getDescriptorPool(Thread.currentThread().getId()).getHandle(),
	    		descriptorSetLayout.getHandlePointer());
	    descriptorSet.updateDescriptorImageBuffer(
	    		sunImageView.getHandle(),
	    		VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
	    		sunImageSampler.getHandle(), 0,
				VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
	    descriptorSet.updateDescriptorImageBuffer(
	    		sunImageView_lightScattering.getHandle(),
				VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
				sunImageSampler_lightScattering.getHandle(), 1,
				VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
	    
	    descriptorSets.add(VkContext.getCamera().getDescriptorSet());
		descriptorSets.add(descriptorSet);
		descriptorSetLayouts.add(VkContext.getCamera().getDescriptorSetLayout());
		descriptorSetLayouts.add(descriptorSetLayout);
		
		int pushConstantRange = Float.BYTES * 16;
		ByteBuffer pushConstants = memAlloc(pushConstantRange);
		pushConstants.put(BufferUtil.createByteBuffer(getWorldTransform().getWorldMatrix()));
		pushConstants.flip();
	    
	    ShaderPipeline shaderPipeline = new ShaderPipeline(device.getHandle());
	    shaderPipeline.createVertexShader("shaders/sun/sun.vert.spv");
	    shaderPipeline.createFragmentShader("shaders/sun/sun.frag.spv");
	    shaderPipeline.createShaderPipeline();
	    
	    VkPipeline graphicsPipeline = new GraphicsPipeline(device.getHandle(),
	    		shaderPipeline, vertexInput, VK_PRIMITIVE_TOPOLOGY_POINT_LIST,
	    		VkUtil.createLongBuffer(descriptorSetLayouts),
				BaseContext.getConfig().getX_ScreenResolution(),
				BaseContext.getConfig().getY_ScreenResolution(),
				VkContext.getResources().getTransparencyFbo().getRenderPass().getHandle(),
				VkContext.getResources().getTransparencyFbo().getColorAttachmentCount(),
				1,
				pushConstantRange, VK_SHADER_STAGE_VERTEX_BIT);
	    
	    CommandBuffer mainCommandBuffer = new SecondaryDrawCmdBuffer(
	    		device.getHandle(),
	    		device.getGraphicsCommandPool(Thread.currentThread().getId()).getHandle(), 
	    		graphicsPipeline.getHandle(), graphicsPipeline.getLayoutHandle(),
	    		VkContext.getResources().getTransparencyFbo().getFrameBuffer().getHandle(),
	    		VkContext.getResources().getTransparencyFbo().getRenderPass().getHandle(),
	    		0,
	    		VkUtil.createLongArray(descriptorSets),
	    		vertexBufferObject.getHandle(), 1,
	    		pushConstants, VK_SHADER_STAGE_VERTEX_BIT);
	    
	    VkMeshData meshData = VkMeshData.builder().vertexBufferObject(vertexBufferObject)
	    		.vertexBuffer(vertexBuffer).build();
	    VkRenderInfo mainRenderInfo = VkRenderInfo.builder().commandBuffer(mainCommandBuffer)
	    		.pipeline(graphicsPipeline).descriptorSets(descriptorSets)
	    		.descriptorSetLayouts(descriptorSetLayouts).build();
	    
	    addComponent(NodeComponentType.MESH_DATA, meshData);
	    addComponent(NodeComponentType.MAIN_RENDERINFO, mainRenderInfo);
	    
	    shaderPipeline.destroy();
	}
	
	public void shutdown(){
		
		super.shutdown();
		sunImageBundle.destroy();
		sunImageBundle_lightScattering.destroy();
	}
	
}
