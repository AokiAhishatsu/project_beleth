package core.wrapper.command;

import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import core.image.ImageMetaData;
import core.command.CommandBuffer;
import core.command.SubmitInfo;

public class ImageCopyCmdBuffer extends CommandBuffer{
	
	public ImageCopyCmdBuffer(VkDevice device, long commandPool) {
		super(device, commandPool, VK_COMMAND_BUFFER_LEVEL_PRIMARY);
	}
	
	public void record(long stagingBuffer, long image, ImageMetaData metaData){
		
		beginRecord(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
	    copyBufferToImageCmd(stagingBuffer, image,
	    		metaData.getWidth(), metaData.getHeight(), 1);
	    finishRecord();
	}
	
	public void submit(VkQueue queue){

		SubmitInfo submitInfo = new SubmitInfo(getHandlePointer());
		submitInfo.submit(queue);
	}

}
