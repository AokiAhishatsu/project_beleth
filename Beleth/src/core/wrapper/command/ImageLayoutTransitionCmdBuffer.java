package core.wrapper.command;

import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import core.command.CommandBuffer;
import core.command.SubmitInfo;
import core.synchronization.Fence;

public class ImageLayoutTransitionCmdBuffer extends CommandBuffer{
	
	public ImageLayoutTransitionCmdBuffer(VkDevice device, long commandPool) {
		super(device, commandPool, VK_COMMAND_BUFFER_LEVEL_PRIMARY);
	}

	public void record(long image, int cmdBufferUsage, 
			int oldLayout, int newLayout, int srcAccessMask, int dstAccessMask,
			int srcStageMask, int dstStageMask, int mipLevels){
		
		beginRecord(cmdBufferUsage);
		pipelineImageMemoryBarrierCmd(image, oldLayout, newLayout, srcAccessMask, dstAccessMask, 
				srcStageMask, dstStageMask, 0, mipLevels);
		finishRecord();
	}
	
	public void submit(VkQueue queue){
		
		SubmitInfo submitInfo = new SubmitInfo(getHandlePointer());
		submitInfo.submit(queue);
	}
	
	public void submit(VkQueue queue, Fence fence){
		
		SubmitInfo submitInfo = new SubmitInfo(getHandlePointer());
		submitInfo.setFence(fence);
		submitInfo.submit(queue);
		fence.waitForFence();
	}

}
