package core.context;

import java.util.HashMap;

import core.device.LogicalDevice;
import core.device.PhysicalDevice;
import core.device.VkDeviceBundle;

public class DeviceManager {

	private HashMap<DeviceType, VkDeviceBundle> devices;
	
	public DeviceManager() {
		
		devices = new HashMap<DeviceType, VkDeviceBundle>();
	}
	
	public enum DeviceType{
		
		MAJOR_GRAPHICS_DEVICE,
		SECONDARY_GRAPHICS_DEVICE,
		COMPUTING_DEVICE,
		SLI_DISCRETE_DEVICE0,
		SLI_DISCRETE_DEVICE1;
	}
	
	public VkDeviceBundle getDeviceBundle(DeviceType deviceType){
		
		return devices.get(deviceType);
	}
	
	public PhysicalDevice getPhysicalDevice(DeviceType deviceType){
		
		return devices.get(deviceType).getPhysicalDevice();
	}
	
	public LogicalDevice getLogicalDevice(DeviceType deviceType){
		
		return devices.get(deviceType).getLogicalDevice();
	}
	
	public void addDevice(DeviceType deviceType, VkDeviceBundle deviceBundle){
		
		devices.put(deviceType, deviceBundle);
	}
}