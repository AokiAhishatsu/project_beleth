package core.context;

import java.util.HashMap;

import javax.management.Descriptor;

import core.framebuffer.VkFrameBufferObject;

public class VkResources {

	private VkFrameBufferObject offScreenFbo;
	private VkFrameBufferObject offScreenReflectionFbo;
	private VkFrameBufferObject transparencyFbo;

	private HashMap<String, Descriptor> descriptors;

	public VkFrameBufferObject getOffScreenFbo() {
		return offScreenFbo;
	}

	public void setOffScreenFbo(VkFrameBufferObject offScreenFbo) {
		this.offScreenFbo = offScreenFbo;
	}

	public VkFrameBufferObject getOffScreenReflectionFbo() {
		return offScreenReflectionFbo;
	}

	public void setOffScreenReflectionFbo(VkFrameBufferObject offScreenReflectionFbo) {
		this.offScreenReflectionFbo = offScreenReflectionFbo;
	}

	public VkFrameBufferObject getTransparencyFbo() {
		return transparencyFbo;
	}

	public void setTransparencyFbo(VkFrameBufferObject transparencyFbo) {
		this.transparencyFbo = transparencyFbo;
	}

	public HashMap<String, Descriptor> getDescriptors() {
		return descriptors;
	}

	public void setDescriptors(HashMap<String, Descriptor> descriptors) {
		this.descriptors = descriptors;
	}
	
}