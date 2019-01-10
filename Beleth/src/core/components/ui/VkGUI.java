package core.components.ui;

import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_COLOR_ATTACHMENT_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_MEMORY_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_SHADER_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_DEPENDENCY_BY_REGION_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_FILTER_NEAREST;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R16G16B16A16_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_UNORM;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_GENERAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_FAMILY_IGNORED;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_MIPMAP_MODE_NEAREST;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SUBPASS_EXTERNAL;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkQueue;
import core.common.ui.GUI;
import core.common.ui.UIPanelLoader;
import core.context.BaseContext;
import core.model.Mesh;
import core.model.Vertex.VertexLayout;
import core.scenegraph.NodeComponentType;
import core.scenegraph.RenderList;
import core.target.FrameBufferObject.Attachment;
import core.util.BufferUtil;
import core.util.MeshGenerator;
import core.command.CommandBuffer;
import core.command.SubmitInfo;
import core.context.DeviceManager.DeviceType;
import core.context.VkContext;
import core.descriptor.DescriptorSet;
import core.descriptor.DescriptorSetLayout;
import core.device.LogicalDevice;
import core.framebuffer.FrameBufferColorAttachment;
import core.framebuffer.VkFrameBuffer;
import core.framebuffer.VkFrameBufferObject;
import core.image.VkImage;
import core.image.VkImageView;
import core.image.VkSampler;
import core.memory.VkBuffer;
import core.pipeline.RenderPass;
import core.pipeline.ShaderPipeline;
import core.pipeline.VkPipeline;
import core.pipeline.VkVertexInput;
import core.scenegraph.VkMeshData;
import core.scenegraph.VkRenderInfo;
import core.synchronization.VkSemaphore;
import core.util.VkUtil;
import core.wrapper.buffer.VkBufferHelper;
import core.wrapper.command.PrimaryCmdBuffer;
import core.wrapper.command.SecondaryDrawIndexedCmdBuffer;
import core.wrapper.image.VkImageBundle;
import core.wrapper.image.VkImageHelper;
import core.wrapper.pipeline.GraphicsPipeline;

public class VkGUI extends GUI{

	protected VkFrameBufferObject guiOverlayFbo;
	protected VkImageBundle fontsImageBundle;
	protected VkMeshData panelMeshBuffer;
	
	private PrimaryCmdBuffer guiPrimaryCmdBuffer;
	private LinkedHashMap<String, CommandBuffer> guiSecondaryCmdBuffers;
	private RenderList guiRenderList;
	private SubmitInfo guiSubmitInfo;
	private VkQueue queue;
	
	// underlay image resources
	private CommandBuffer underlayImageCmdBuffer;
	private VkPipeline underlayImagePipeline;
	private DescriptorSet underlayImageDescriptorSet;
	private DescriptorSetLayout underlayImageDescriptorSetLayout;
	private VkSampler underlayImageSampler;
	
	private VkSemaphore signalSemaphore;
	
	public VkSemaphore getSignalSemaphore() {
		return signalSemaphore;
	}

	public void init(VkImageView underlayImageView, LongBuffer waitSemaphores) {

		LogicalDevice device = VkContext.getDeviceManager()
				.getLogicalDevice(DeviceType.MAJOR_GRAPHICS_DEVICE);
		VkPhysicalDeviceMemoryProperties memoryProperties = VkContext.getDeviceManager()
				.getPhysicalDevice(DeviceType.MAJOR_GRAPHICS_DEVICE).getMemoryProperties();
		queue = device.getGraphicsQueue();
		
		guiOverlayFbo = new SingleAttachmentFbo(device.getHandle(), memoryProperties);
		
		guiRenderList = new RenderList();
		guiSecondaryCmdBuffers = new LinkedHashMap<String, CommandBuffer>();
		
		guiPrimaryCmdBuffer =  new PrimaryCmdBuffer(device.getHandle(),
				device.getGraphicsCommandPool(Thread.currentThread().getId()).getHandle());
		
		IntBuffer pWaitDstStageMask = memAllocInt(1);
        pWaitDstStageMask.put(0, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT);
        signalSemaphore = new VkSemaphore(device.getHandle());
        guiSubmitInfo = new SubmitInfo();
		guiSubmitInfo.setCommandBuffers(guiPrimaryCmdBuffer.getHandlePointer());
		guiSubmitInfo.setWaitSemaphores(waitSemaphores);
		guiSubmitInfo.setWaitDstStageMask(pWaitDstStageMask);
		guiSubmitInfo.setSignalSemaphores(signalSemaphore.getHandlePointer());
		
		// fonts Image 
		VkImage fontsImage = VkImageHelper.loadImageFromFile(
				device.getHandle(), memoryProperties,
				device.getTransferCommandPool(Thread.currentThread().getId()).getHandle(),
				device.getTransferQueue(),
				"res/gui/tex/Fonts.png",
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
		
		fontsImageBundle = new VkImageBundle(fontsImage, fontsImageView, sampler);
		
		// panel mesh buffer
		panelMeshBuffer = new VkMeshData(device.getHandle(),
				memoryProperties, device.getTransferCommandPool(Thread.currentThread().getId()),
				device.getTransferQueue(), UIPanelLoader.load("res/gui/basicPanel.gui"),
				VertexLayout.POS2D);
		
		// fullscreen underlay Image resources
		ShaderPipeline shaderPipeline = new ShaderPipeline(device.getHandle());
	    shaderPipeline.createVertexShader("res/shaders/quad/quad.vert.spv");
	    shaderPipeline.createFragmentShader("res/shaders/quad/quad.frag.spv");
	    shaderPipeline.createShaderPipeline();
	    
	    VkVertexInput vertexInputInfo = new VkVertexInput(VertexLayout.POS_UV);
		
	    Mesh fullScreenQuad = MeshGenerator.NDCQuad2D();
        ByteBuffer vertexBuffer = BufferUtil.createByteBuffer(fullScreenQuad.getVertices(), VertexLayout.POS_UV);
        ByteBuffer indexBuffer = BufferUtil.createByteBuffer(fullScreenQuad.getIndices());
        
        VkBuffer vertexBufferObject = VkBufferHelper.createDeviceLocalBuffer(
        		device.getHandle(), memoryProperties,
        		device.getTransferCommandPool(Thread.currentThread().getId()).getHandle(),
        		device.getTransferQueue(),
        		vertexBuffer, VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
        
        VkBuffer indexBufferObject = VkBufferHelper.createDeviceLocalBuffer(
        		device.getHandle(), memoryProperties,
        		device.getTransferCommandPool(Thread.currentThread().getId()).getHandle(),
        		device.getTransferQueue(),
        		indexBuffer, VK_BUFFER_USAGE_INDEX_BUFFER_BIT);
        
        underlayImageDescriptorSetLayout = new DescriptorSetLayout(device.getHandle(),1);
        underlayImageDescriptorSetLayout.addLayoutBinding(0, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
	    						VK_SHADER_STAGE_FRAGMENT_BIT);
        underlayImageDescriptorSetLayout.create();
	    
        underlayImageSampler = new VkSampler(device.getHandle(), VK_FILTER_NEAREST, false, 0, 
	    		VK_SAMPLER_MIPMAP_MODE_NEAREST, 0, VK_SAMPLER_ADDRESS_MODE_REPEAT);
	    
	    underlayImageDescriptorSet = new DescriptorSet(device.getHandle(),
	    		device.getDescriptorPool(Thread.currentThread().getId()).getHandle(),
	    		underlayImageDescriptorSetLayout.getHandlePointer());
	    underlayImageDescriptorSet.updateDescriptorImageBuffer(underlayImageView.getHandle(),
	    		VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
	    		underlayImageSampler.getHandle(), 0, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
        
        List<DescriptorSet> descriptorSets = new ArrayList<DescriptorSet>();
		List<DescriptorSetLayout> descriptorSetLayouts = new ArrayList<DescriptorSetLayout>();
		
		descriptorSets.add(underlayImageDescriptorSet);
		descriptorSetLayouts.add(underlayImageDescriptorSetLayout);
    
        underlayImagePipeline = new GraphicsPipeline(device.getHandle(),
				shaderPipeline, vertexInputInfo, VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST,
				VkUtil.createLongBuffer(descriptorSetLayouts),
				guiOverlayFbo.getWidth(), guiOverlayFbo.getHeight(),
				guiOverlayFbo.getRenderPass().getHandle(),
				guiOverlayFbo.getColorAttachmentCount(), 1);
        
        underlayImageCmdBuffer = new SecondaryDrawIndexedCmdBuffer(
				device.getHandle(),
				device.getGraphicsCommandPool(Thread.currentThread().getId()).getHandle(),
				underlayImagePipeline.getHandle(), underlayImagePipeline.getLayoutHandle(),
				guiOverlayFbo.getFrameBuffer().getHandle(),
				guiOverlayFbo.getRenderPass().getHandle(),
				0,
				VkUtil.createLongArray(descriptorSets),
				vertexBufferObject.getHandle(), indexBufferObject.getHandle(),
				fullScreenQuad.getIndices().length);
        
        guiSecondaryCmdBuffers.put("0", underlayImageCmdBuffer);
        
        shaderPipeline.destroy();
	}
	
	public void render(){
		
		record(guiRenderList);
		
		for (String key : guiRenderList.getKeySet()) {
			if(!guiSecondaryCmdBuffers.containsKey(key)){
				VkRenderInfo mainRenderInfo = guiRenderList.get(key)
						.getComponent(NodeComponentType.MAIN_RENDERINFO);
				guiSecondaryCmdBuffers.put(key, mainRenderInfo.getCommandBuffer());
			}
		}
		
		// primary render command buffer
		if (!guiRenderList.getObjectList().isEmpty()){
			guiPrimaryCmdBuffer.reset();
			guiPrimaryCmdBuffer.record(guiOverlayFbo.getRenderPass().getHandle(),
					guiOverlayFbo.getFrameBuffer().getHandle(),
					guiOverlayFbo.getWidth(),
					guiOverlayFbo.getHeight(),
					guiOverlayFbo.getColorAttachmentCount(),
					guiOverlayFbo.getDepthAttachmentCount(),
					VkUtil.createPointerBuffer(guiSecondaryCmdBuffers.values()));
			
			guiSubmitInfo.submit(queue);
		}
	}
	
	private class SingleAttachmentFbo extends VkFrameBufferObject{
		
		public SingleAttachmentFbo(VkDevice device,
				VkPhysicalDeviceMemoryProperties memoryProperties) {
			
			width = BaseContext.getConfig().getX_ScreenResolution();
			height = BaseContext.getConfig().getY_ScreenResolution();
			
			VkImageBundle colorAttachment = new FrameBufferColorAttachment(device, memoryProperties,
					width, height, VK_FORMAT_R16G16B16A16_SFLOAT, 1);
			
			attachments.put(Attachment.COLOR, colorAttachment);
			
			renderPass = new RenderPass(device);
			renderPass.addColorAttachment(0, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
					VK_FORMAT_R16G16B16A16_SFLOAT, 1, VK_IMAGE_LAYOUT_UNDEFINED,
					VK_IMAGE_LAYOUT_GENERAL);
			
			renderPass.addSubpassDependency(VK_SUBPASS_EXTERNAL, 0,
					VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT,
					VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
					VK_ACCESS_MEMORY_READ_BIT,
					VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT,
					VK_DEPENDENCY_BY_REGION_BIT);
			renderPass.addSubpassDependency(0, VK_SUBPASS_EXTERNAL,
					VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
					VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT,
					VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT,
					VK_ACCESS_MEMORY_READ_BIT,
					VK_DEPENDENCY_BY_REGION_BIT);
			renderPass.createSubpass();
			renderPass.createRenderPass();

			depthAttachmentCount = 0;
			colorAttachmentCount = renderPass.getAttachmentCount()-depthAttachmentCount;
			
			LongBuffer pImageViews = memAllocLong(renderPass.getAttachmentCount());
			pImageViews.put(0, attachments.get(Attachment.COLOR).getImageView().getHandle());

			frameBuffer = new VkFrameBuffer(device, width, height, 1, pImageViews, renderPass.getHandle());
		}
	}
	
	public VkImageView getImageView(){
		return guiOverlayFbo.getAttachmentImageView(Attachment.COLOR);
	}
	
	public void shutdown(){
		
		super.shutdown();
		
		signalSemaphore.destroy();
		fontsImageBundle.destroy();
		panelMeshBuffer.shutdown();
		guiPrimaryCmdBuffer.destroy();
		underlayImageCmdBuffer.destroy();
		underlayImagePipeline.destroy();
		underlayImageDescriptorSet.destroy();
		underlayImageDescriptorSetLayout.destroy();
		underlayImageSampler.destroy();
	}

}
