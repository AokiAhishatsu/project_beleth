package core.components.ui;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_SHADER_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_UNORM;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_FAMILY_IGNORED;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_MIPMAP_MODE_NEAREST;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import core.scenegraph.NodeComponentType;

import core.command.CommandBuffer;
import core.command.SubmitInfo;
import core.common.ui.UIElement;
import core.context.DeviceManager.DeviceType;
import core.context.VkContext;
import core.descriptor.DescriptorPool;
import core.descriptor.DescriptorSet;
import core.descriptor.DescriptorSetLayout;
import core.device.LogicalDevice;
import core.device.VkDeviceBundle;
import core.framebuffer.VkFrameBufferObject;
import core.image.VkImage;
import core.image.VkImageView;
import core.image.VkSampler;
import core.model.Vertex.VertexLayout;
import core.pipeline.ShaderPipeline;
import core.pipeline.VkPipeline;
import core.pipeline.VkVertexInput;
import core.scenegraph.VkMeshData;
import core.scenegraph.VkRenderInfo;
import core.util.BufferUtil;
import core.util.VkUtil;
import core.wrapper.command.SecondaryDrawIndexedCmdBuffer;
import core.wrapper.image.VkImageBundle;
import core.wrapper.image.VkImageHelper;
import core.wrapper.pipeline.GraphicsPipelineAlphaBlend;


public class VkTexturePanel  extends UIElement{
	
	private VkPipeline graphicsPipeline;
	private CommandBuffer cmdBuffer;
	private SubmitInfo submitInfo;
	private VkImageBundle imageBundle;

	public VkTexturePanel(String imageFile, int xPos, int yPos, int xScaling, int yScaling,
			VkMeshData panelMeshBuffer, VkFrameBufferObject fbo) {
		super(xPos, yPos, xScaling, yScaling);
		
		// flip y-axxis for vulkan coordinate system
		getOrthographicMatrix().set(1, 1, -getOrthographicMatrix().get(1, 1));
		
		VkDeviceBundle deviceBundle = VkContext.getDeviceManager().getDeviceBundle(DeviceType.MAJOR_GRAPHICS_DEVICE);
		LogicalDevice device = deviceBundle.getLogicalDevice();
		DescriptorPool descriptorPool = device.getDescriptorPool(Thread.currentThread().getId());
		VkPhysicalDeviceMemoryProperties memoryProperties = 
				VkContext.getDeviceManager().getPhysicalDevice(DeviceType.MAJOR_GRAPHICS_DEVICE).getMemoryProperties();
		
		VkImage fontsImage = VkImageHelper.loadImageFromFile(
				device.getHandle(), memoryProperties,
				device.getTransferCommandPool(Thread.currentThread().getId()).getHandle(),
				device.getTransferQueue(),
				imageFile,
				VK_IMAGE_USAGE_SAMPLED_BIT,
				VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
				VK_ACCESS_SHADER_READ_BIT,
				VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
				VK_QUEUE_FAMILY_IGNORED);
		
		VkImageView fontsImageView = new VkImageView(device.getHandle(),
				VK_FORMAT_R8G8B8A8_UNORM, fontsImage.getHandle(), 
				VK_IMAGE_ASPECT_COLOR_BIT, 1);
		
		VkSampler sampler = new VkSampler(device.getHandle(), VK_FILTER_LINEAR, false, 1,
				VK_SAMPLER_MIPMAP_MODE_NEAREST, 0, VK_SAMPLER_ADDRESS_MODE_REPEAT);
		
		imageBundle = new VkImageBundle(fontsImage, fontsImageView, sampler);
		
		ShaderPipeline shaderPipeline = new ShaderPipeline(device.getHandle());
	    shaderPipeline.createVertexShader("shaders/ui/texturePanel.vert.spv");
	    shaderPipeline.createFragmentShader("shaders/ui/texturePanel.frag.spv");
	    shaderPipeline.createShaderPipeline();
	    
	    int pushConstantRange = Float.BYTES * 16;
		ByteBuffer pushConstants = memAlloc(pushConstantRange);
		pushConstants.put(BufferUtil.createByteBuffer(getOrthographicMatrix()));
		pushConstants.flip();
		
		VkVertexInput vertexInput = new VkVertexInput(VertexLayout.POS2D);
		
		DescriptorSetLayout descriptorSetLayout = new DescriptorSetLayout(device.getHandle(), 1);
	    descriptorSetLayout.addLayoutBinding(0, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
	    		VK_SHADER_STAGE_FRAGMENT_BIT);
	    descriptorSetLayout.create();
	    
	    DescriptorSet descriptorSet = new DescriptorSet(device.getHandle(),
	    		descriptorPool.getHandle(),
	    		descriptorSetLayout.getHandlePointer());
	    descriptorSet.updateDescriptorImageBuffer(imageBundle.getImageView().getHandle(),
	    		VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, imageBundle.getSampler().getHandle(),
	    		0, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
	    
	    List<DescriptorSet> descriptorSets = new ArrayList<DescriptorSet>();
		List<DescriptorSetLayout> descriptorSetLayouts = new ArrayList<DescriptorSetLayout>();
		
		descriptorSets.add(descriptorSet);
		descriptorSetLayouts.add(descriptorSetLayout);
		
		graphicsPipeline = new GraphicsPipelineAlphaBlend(device.getHandle(),
				shaderPipeline, vertexInput, VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST,
				VkUtil.createLongBuffer(descriptorSetLayouts),
				fbo.getWidth(), fbo.getHeight(),
				fbo.getRenderPass().getHandle(),
				fbo.getColorAttachmentCount(),
				1, pushConstantRange,
				VK_SHADER_STAGE_VERTEX_BIT);
        
		cmdBuffer = new SecondaryDrawIndexedCmdBuffer(
				device.getHandle(),
				deviceBundle.getLogicalDevice().getGraphicsCommandPool(Thread.currentThread().getId()).getHandle(),
				graphicsPipeline.getHandle(), graphicsPipeline.getLayoutHandle(),
				fbo.getFrameBuffer().getHandle(),
				fbo.getRenderPass().getHandle(),
				0,
				VkUtil.createLongArray(descriptorSets),
				panelMeshBuffer.getVertexBufferObject().getHandle(),
				panelMeshBuffer.getIndexBufferObject().getHandle(),
				panelMeshBuffer.getIndexCount(),
				pushConstants,
				VK_SHADER_STAGE_VERTEX_BIT);
		
		submitInfo = new SubmitInfo();
		submitInfo.setCommandBuffers(cmdBuffer.getHandlePointer());
		
		VkRenderInfo mainRenderInfo = VkRenderInfo.builder().commandBuffer(cmdBuffer).build();
		addComponent(NodeComponentType.MAIN_RENDERINFO, mainRenderInfo);
	}

}
