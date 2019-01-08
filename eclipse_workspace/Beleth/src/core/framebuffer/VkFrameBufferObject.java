package core.framebuffer;

import java.util.HashMap;
import java.util.Map;

import core.target.FrameBufferObject;
import core.image.VkImageView;
import core.pipeline.RenderPass;
import core.wrapper.image.VkImageBundle;

public class VkFrameBufferObject extends FrameBufferObject{
	
	protected VkFrameBuffer frameBuffer;
	protected RenderPass renderPass;
	protected HashMap<Attachment, VkImageBundle> attachments = new HashMap<>();
	
	public VkImageView getAttachmentImageView(Attachment type){
		
		return attachments.get(type).getImageView();
	}
	
	public void destroy(){
		
		frameBuffer.destroy();
		renderPass.destroy();
		
		for (Map.Entry<Attachment, VkImageBundle> attachment : attachments.entrySet()) {
			attachment.getValue().destroy();
		}
	}

	public VkFrameBuffer getFrameBuffer() {
		return frameBuffer;
	}

	public RenderPass getRenderPass() {
		return renderPass;
	}

	public HashMap<Attachment, VkImageBundle> getAttachments() {
		return attachments;
	}
	
}