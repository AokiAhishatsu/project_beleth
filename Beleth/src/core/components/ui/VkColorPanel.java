package core.components.ui;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;

import java.nio.ByteBuffer;

import core.common.ui.UIElement;
import core.math.Vec4f;
import core.model.Vertex.VertexLayout;
import core.scenegraph.NodeComponentType;
import core.util.BufferUtil;
import core.command.CommandBuffer;
import core.command.SubmitInfo;
import core.context.DeviceManager.DeviceType;
import core.context.VkContext;
import core.device.LogicalDevice;
import core.device.VkDeviceBundle;
import core.framebuffer.VkFrameBufferObject;
import core.pipeline.ShaderPipeline;
import core.pipeline.VkPipeline;
import core.pipeline.VkVertexInput;
import core.scenegraph.VkMeshData;
import core.scenegraph.VkRenderInfo;
import core.wrapper.command.SecondaryDrawIndexedCmdBuffer;
import core.wrapper.pipeline.GraphicsPipelineAlphaBlend;

public class VkColorPanel extends UIElement{

	private VkPipeline graphicsPipeline;
	private CommandBuffer cmdBuffer;
	private SubmitInfo submitInfo;
	
	public VkColorPanel(Vec4f rgba, int xPos, int yPos, int xScaling, int yScaling,
			VkMeshData panelMeshBuffer, VkFrameBufferObject fbo) {
		super(xPos, yPos, xScaling, yScaling);
		
		// flip y-axxis for vulkan coordinate system
		getOrthographicMatrix().set(1, 1, -getOrthographicMatrix().get(1, 1));
		
		VkDeviceBundle deviceBundle = VkContext.getDeviceManager().getDeviceBundle(DeviceType.MAJOR_GRAPHICS_DEVICE);
		LogicalDevice device = deviceBundle.getLogicalDevice();
		
		ShaderPipeline shaderPipeline = new ShaderPipeline(device.getHandle());
	    shaderPipeline.createVertexShader("res/shaders/ui/colorPanel.vert.spv");
	    shaderPipeline.createFragmentShader("res/shaders/ui/colorPanel.frag.spv");
	    shaderPipeline.createShaderPipeline();

	    int pushConstantRange = Float.BYTES * 20;
		ByteBuffer pushConstants = memAlloc(pushConstantRange);
		pushConstants.put(BufferUtil.createByteBuffer(getOrthographicMatrix()));
		pushConstants.putFloat(rgba.getX());
		pushConstants.putFloat(rgba.getY());
		pushConstants.putFloat(rgba.getZ());
		pushConstants.putFloat(rgba.getW());
		pushConstants.flip();
		
		VkVertexInput vertexInput = new VkVertexInput(VertexLayout.POS2D);
        
        graphicsPipeline = new GraphicsPipelineAlphaBlend(device.getHandle(),
				shaderPipeline, vertexInput, VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST,
				null,
				fbo.getWidth(), fbo.getHeight(),
				fbo.getRenderPass().getHandle(),
				fbo.getColorAttachmentCount(),
				1, pushConstantRange,
				VK_SHADER_STAGE_FRAGMENT_BIT | VK_SHADER_STAGE_VERTEX_BIT);
        
		cmdBuffer = new SecondaryDrawIndexedCmdBuffer(
				device.getHandle(),
				deviceBundle.getLogicalDevice().getGraphicsCommandPool(Thread.currentThread().getId()).getHandle(),
				graphicsPipeline.getHandle(), graphicsPipeline.getLayoutHandle(),
				fbo.getFrameBuffer().getHandle(),
				fbo.getRenderPass().getHandle(),
				0,
				null,
				panelMeshBuffer.getVertexBufferObject().getHandle(),
				panelMeshBuffer.getIndexBufferObject().getHandle(),
				panelMeshBuffer.getIndexCount(),
				pushConstants,
				VK_SHADER_STAGE_FRAGMENT_BIT | VK_SHADER_STAGE_VERTEX_BIT);
		
		submitInfo = new SubmitInfo();
		submitInfo.setCommandBuffers(cmdBuffer.getHandlePointer());
		
		VkRenderInfo mainRenderInfo = VkRenderInfo.builder().commandBuffer(cmdBuffer).build();
		addComponent(NodeComponentType.MAIN_RENDERINFO, mainRenderInfo);
	}

}
