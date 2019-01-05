package core.wrapper.image;

import core.image.VkImage;
import core.image.VkImageView;
import core.image.VkSampler;

public class VkImageBundle {

	protected VkImage image;
	protected VkImageView imageView;
	protected VkSampler sampler;
	
	public VkImageBundle() {

	}

	public VkImageBundle(VkImage image, VkImageView imageView, VkSampler sampler) {
		super();
		this.image = image;
		this.imageView = imageView;
		this.sampler = sampler;
	}

	public VkImageBundle(VkImage image, VkImageView imageView) {
		this.image = image;
		this.imageView = imageView;
	}
	
	public void destroy(){
		
		if (sampler != null){
			sampler.destroy();
		}
		imageView.destroy();
		image.destroy();
	}

	public VkImage getImage() {
		return image;
	}

	public VkImageView getImageView() {
		return imageView;
	}

	public VkSampler getSampler() {
		return sampler;
	}

}
