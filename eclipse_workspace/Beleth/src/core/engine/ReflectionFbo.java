package core.engine;

import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_COLOR_ATTACHMENT_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_MEMORY_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_DEPENDENCY_BY_REGION_BIT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_D16_UNORM;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R16G16B16A16_UNORM;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_UNORM;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_GENERAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SUBPASS_EXTERNAL;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import core.context.BaseContext;
import core.framebuffer.FrameBufferColorAttachment;
import core.framebuffer.FrameBufferDepthAttachment;
import core.framebuffer.VkFrameBuffer;
import core.framebuffer.VkFrameBufferObject;
import core.pipeline.RenderPass;
import core.wrapper.image.VkImageBundle;

public class ReflectionFbo extends VkFrameBufferObject{

	public ReflectionFbo(VkDevice device,
			VkPhysicalDeviceMemoryProperties memoryProperties) {
		
		width = BaseContext.getConfig().getX_ScreenResolution()/1;
		height = BaseContext.getConfig().getY_ScreenResolution()/1;
		
		VkImageBundle albedoBuffer = new FrameBufferColorAttachment(device, memoryProperties, width, height,
				VK_FORMAT_R8G8B8A8_UNORM, 1);
		VkImageBundle normalBuffer = new FrameBufferColorAttachment(device, memoryProperties, width, height, 
				VK_FORMAT_R16G16B16A16_UNORM, 1);
		VkImageBundle depthBuffer = new FrameBufferDepthAttachment(device, memoryProperties, width, height,
				VK_FORMAT_D16_UNORM, 1);
		
		attachments.put(Attachment.ALBEDO, albedoBuffer);
		attachments.put(Attachment.NORMAL, normalBuffer);
		attachments.put(Attachment.DEPTH, depthBuffer);
		
		renderPass = new RenderPass(device);
		renderPass.addColorAttachment(0, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
				VK_FORMAT_R8G8B8A8_UNORM, 1, VK_IMAGE_LAYOUT_UNDEFINED,
				VK_IMAGE_LAYOUT_GENERAL);
		renderPass.addColorAttachment(1, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
				VK_FORMAT_R16G16B16A16_UNORM, 1, VK_IMAGE_LAYOUT_UNDEFINED,
				VK_IMAGE_LAYOUT_GENERAL);
		renderPass.addDepthAttachment(2, VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL,
				VK_FORMAT_D16_UNORM, 1, VK_IMAGE_LAYOUT_UNDEFINED,
				VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
		
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
		
		depthAttachmentCount = 1;
		colorAttachmentCount = renderPass.getAttachmentCount()-depthAttachmentCount;
		
		LongBuffer pImageViews = memAllocLong(renderPass.getAttachmentCount());
		pImageViews.put(0, attachments.get(Attachment.ALBEDO).getImageView().getHandle());
		pImageViews.put(1, attachments.get(Attachment.NORMAL).getImageView().getHandle());
		pImageViews.put(2, attachments.get(Attachment.DEPTH).getImageView().getHandle());
		
		frameBuffer = new VkFrameBuffer(device, width, height, 1,
				pImageViews, renderPass.getHandle());
	}
	
}