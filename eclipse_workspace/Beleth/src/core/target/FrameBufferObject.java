package core.target;

public abstract class FrameBufferObject {

	protected int height; 
	protected int width;
	protected int colorAttachmentCount;
	protected int depthAttachmentCount;
	
	public enum Attachment {
		
		COLOR,
		ALBEDO,
		ALPHA,
		NORMAL,
		POSITION,
		SPECULAR_EMISSION,
		LIGHT_SCATTERING,
		DEPTH;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public int getColorAttachmentCount() {
		return colorAttachmentCount;
	}

	public int getDepthAttachmentCount() {
		return depthAttachmentCount;
	}
	
}
