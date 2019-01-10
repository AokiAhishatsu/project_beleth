package core.device;

public class VkDeviceBundle {

	private PhysicalDevice physicalDevice;
	private LogicalDevice logicalDevice;
	
	public VkDeviceBundle(PhysicalDevice physicalDevice, LogicalDevice logicalDevice) {
		super();
		this.physicalDevice = physicalDevice;
		this.logicalDevice = logicalDevice;
	}
	
	public PhysicalDevice getPhysicalDevice() {
		return physicalDevice;
	}
	
	public LogicalDevice getLogicalDevice() {
		return logicalDevice;
	}
	
}