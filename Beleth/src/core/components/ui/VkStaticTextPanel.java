package core.components.ui;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import core.common.ui.UITextPanel;
import core.model.Vertex.VertexLayout;
import core.scenegraph.NodeComponentType;
import core.util.BufferUtil;
import core.command.CommandBuffer;
import core.context.DeviceManager.DeviceType;
import core.context.VkContext;
import core.descriptor.DescriptorPool;
import core.descriptor.DescriptorSet;
import core.descriptor.DescriptorSetLayout;
import core.device.LogicalDevice;
import core.device.VkDeviceBundle;
import core.framebuffer.VkFrameBufferObject;
import core.image.VkImageView;
import core.image.VkSampler;
import core.memory.VkBuffer;
import core.pipeline.ShaderPipeline;
import core.pipeline.VkPipeline;
import core.pipeline.VkVertexInput;
import core.scenegraph.VkRenderInfo;
import core.util.VkUtil;
import core.wrapper.buffer.VkBufferHelper;
import core.wrapper.command.SecondaryDrawIndexedCmdBuffer;
import core.wrapper.pipeline.GraphicsPipelineAlphaBlend;

public class VkStaticTextPanel extends UITextPanel{
	
	public VkStaticTextPanel(String text, int xPos, int yPos, int xScaling, int yScaling,
			VkImageView fontsImageView, VkSampler fontsSampler, VkFrameBufferObject fbo) {
		super(text, xPos, yPos, xScaling, yScaling);
		// flip y-axxis for vulkan coordinate system
		getOrthographicMatrix().set(1, 1, -getOrthographicMatrix().get(1, 1));
		
		VkDeviceBundle deviceBundle = VkContext.getDeviceManager().getDeviceBundle(DeviceType.MAJOR_GRAPHICS_DEVICE);
		LogicalDevice device = deviceBundle.getLogicalDevice();
		DescriptorPool descriptorPool = device.getDescriptorPool(Thread.currentThread().getId());
		VkPhysicalDeviceMemoryProperties memoryProperties = 
				VkContext.getDeviceManager().getPhysicalDevice(DeviceType.MAJOR_GRAPHICS_DEVICE).getMemoryProperties();
		
		ShaderPipeline shaderPipeline = new ShaderPipeline(device.getHandle());
	    shaderPipeline.createVertexShader("res/shaders/ui/staticTextPanel.vert.spv");
	    shaderPipeline.createFragmentShader("res/shaders/ui/textPanel.frag.spv");
	    shaderPipeline.createShaderPipeline();

	    int pushConstantRange = Float.BYTES * 16;
		ByteBuffer pushConstants = memAlloc(pushConstantRange);
		pushConstants.put(BufferUtil.createByteBuffer(getOrthographicMatrix()));
		pushConstants.flip();
		
		DescriptorSetLayout descriptorSetLayout = new DescriptorSetLayout(device.getHandle(), 1);
	    descriptorSetLayout.addLayoutBinding(0, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
	    		VK_SHADER_STAGE_FRAGMENT_BIT);
	    descriptorSetLayout.create();
	    
	    DescriptorSet descriptorSet = new DescriptorSet(device.getHandle(),
	    		descriptorPool.getHandle(),
	    		descriptorSetLayout.getHandlePointer());
	    descriptorSet.updateDescriptorImageBuffer(fontsImageView.getHandle(),
	    		VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, fontsSampler.getHandle(),
	    		0, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
	    
	    List<DescriptorSet> descriptorSets = new ArrayList<DescriptorSet>();
		List<DescriptorSetLayout> descriptorSetLayouts = new ArrayList<DescriptorSetLayout>();
		
		descriptorSets.add(descriptorSet);
		descriptorSetLayouts.add(descriptorSetLayout);
		
		VkVertexInput vertexInput = new VkVertexInput(VertexLayout.POS2D_UV);
		ByteBuffer vertexBuffer = BufferUtil.createByteBuffer(panel.getVertices(), VertexLayout.POS2D_UV);
		ByteBuffer indexBuffer = BufferUtil.createByteBuffer(panel.getIndices());
		
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
        
        VkPipeline graphicsPipeline = new GraphicsPipelineAlphaBlend(device.getHandle(),
				shaderPipeline, vertexInput, VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST,
				VkUtil.createLongBuffer(descriptorSetLayouts),
				fbo.getWidth(), fbo.getHeight(),
				fbo.getRenderPass().getHandle(),
				fbo.getColorAttachmentCount(),
				1, pushConstantRange,
				VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT);
        
		CommandBuffer cmdBuffer = new SecondaryDrawIndexedCmdBuffer(
				device.getHandle(),
				deviceBundle.getLogicalDevice().getGraphicsCommandPool(Thread.currentThread().getId()).getHandle(),
				graphicsPipeline.getHandle(), graphicsPipeline.getLayoutHandle(),
				fbo.getFrameBuffer().getHandle(),
				fbo.getRenderPass().getHandle(),
				0,
				VkUtil.createLongArray(descriptorSets),
				vertexBufferObject.getHandle(), indexBufferObject.getHandle(),
				panel.getIndices().length,
				pushConstants,
				VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT);

		VkRenderInfo mainRenderInfo = VkRenderInfo.builder().commandBuffer(cmdBuffer)
				.pipeline(graphicsPipeline).vertexInput(vertexInput).descriptorSetLayouts(descriptorSetLayouts)
				.descriptorSets(descriptorSets).build();
		addComponent(NodeComponentType.MAIN_RENDERINFO, mainRenderInfo);
		
		shaderPipeline.destroy();
	}

}
