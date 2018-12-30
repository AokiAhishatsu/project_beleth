package core.command;

import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO;
import static org.lwjgl.vulkan.VK10.vkQueueSubmit;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSubmitInfo;
import core.synchronization.Fence;
import core.util.VkUtil;

public class SubmitInfo {

	private VkSubmitInfo handle;
	private Fence fence;
	
	public SubmitInfo() {
	
		handle = VkSubmitInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
                .pNext(0);
	}
	
	public SubmitInfo(PointerBuffer buffers){
		
		this();
		
		setCommandBuffers(buffers);
	}
	
	public void setCommandBuffers(PointerBuffer buffers){
		
		handle.pCommandBuffers(buffers);
	}
	
	public void setWaitSemaphores(LongBuffer semaphores){
		
		handle.waitSemaphoreCount(semaphores.remaining());
		handle.pWaitSemaphores(semaphores);
	}
	
	public void setSignalSemaphores(LongBuffer semaphores){
		
		handle.pSignalSemaphores(semaphores);
	}
	
	public void setWaitDstStageMask(IntBuffer waitDstStageMasks){
		
		handle.pWaitDstStageMask(waitDstStageMasks);
	}
	
	public void clearWaitSemaphores(){
		
		handle.waitSemaphoreCount(0);
		handle.pWaitSemaphores(null);
	}
	
	public void clearSignalSemaphores(){
		
		handle.pSignalSemaphores(null);
	}
	
	public void submit(VkQueue queue){
		
		if (fence != null){
			fence.reset();
		}
		
		VkUtil.vkCheckResult(vkQueueSubmit(queue, handle,
				fence == null ? VK_NULL_HANDLE : fence.getHandle()));
	}

	public Fence getFence() {
		return fence;
	}

	public void setFence(Fence fence) {
		this.fence = fence;
	}

	public VkSubmitInfo getHandle() {
		return handle;
	}
	
	
}