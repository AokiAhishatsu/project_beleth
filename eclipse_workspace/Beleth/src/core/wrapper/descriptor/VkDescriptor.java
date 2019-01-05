package core.wrapper.descriptor;

import core.descriptor.DescriptorSet;
import core.descriptor.DescriptorSetLayout;

public abstract class VkDescriptor {

	protected DescriptorSet descriptorSet;
	protected DescriptorSetLayout descriptorSetLayout;
	
	public void destroy(){
		
		descriptorSet.destroy();
		descriptorSetLayout.destroy();
	}

	public DescriptorSet getDescriptorSet() {
		return descriptorSet;
	}

	public DescriptorSetLayout getDescriptorSetLayout() {
		return descriptorSetLayout;
	}
	
}
