package core;

import static org.lwjgl.system.MemoryUtil.memUTF8;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import helpers.VKUtil;

public class DeviceAndGraphicsQueueFamily {
	
	private VkDevice device;
	private int queueFamilyIndex;
	private VkPhysicalDeviceMemoryProperties memoryProperties;
	
	private static final boolean validation = Boolean.parseBoolean(System.getProperty("vulkan.validation", "false"));
	private static ByteBuffer[] layers = {
            memUTF8("VK_LAYER_LUNARG_standard_validation"),
    };
	
	public DeviceAndGraphicsQueueFamily(VkPhysicalDevice physicalDevice) {
		createDeviceAndGetGraphicsQueueFamily(physicalDevice);
	}
	
	public void createDeviceAndGetGraphicsQueueFamily(VkPhysicalDevice physicalDevice) {
        IntBuffer pQueueFamilyPropertyCount = memAllocInt(1);
        vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyPropertyCount, null);
        int queueCount = pQueueFamilyPropertyCount.get(0);
        VkQueueFamilyProperties.Buffer queueProps = VkQueueFamilyProperties.calloc(queueCount);
        vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyPropertyCount, queueProps);
        memFree(pQueueFamilyPropertyCount);
        int graphicsQueueFamilyIndex;
        for (graphicsQueueFamilyIndex = 0; graphicsQueueFamilyIndex < queueCount; graphicsQueueFamilyIndex++) {
            if ((queueProps.get(graphicsQueueFamilyIndex).queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0)
                break;
        }
        queueProps.free();
        FloatBuffer pQueuePriorities = memAllocFloat(1).put(0.0f);
        pQueuePriorities.flip();
        VkDeviceQueueCreateInfo.Buffer queueCreateInfo = VkDeviceQueueCreateInfo.calloc(1)
                .sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                .queueFamilyIndex(graphicsQueueFamilyIndex)
                .pQueuePriorities(pQueuePriorities);

        PointerBuffer extensions = memAllocPointer(1);
        ByteBuffer VK_KHR_SWAPCHAIN_EXTENSION = memUTF8(VK_KHR_SWAPCHAIN_EXTENSION_NAME);
        extensions.put(VK_KHR_SWAPCHAIN_EXTENSION);
        extensions.flip();
        PointerBuffer ppEnabledLayerNames = memAllocPointer(layers.length);
        for (int i = 0; validation && i < layers.length; i++)
            ppEnabledLayerNames.put(layers[i]);
        ppEnabledLayerNames.flip();

        VkDeviceCreateInfo deviceCreateInfo = VkDeviceCreateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                .pNext(NULL)
                .pQueueCreateInfos(queueCreateInfo)
                .ppEnabledExtensionNames(extensions)
                .ppEnabledLayerNames(ppEnabledLayerNames);

        PointerBuffer pDevice = memAllocPointer(1);
        int err = vkCreateDevice(physicalDevice, deviceCreateInfo, null, pDevice);
        long device = pDevice.get(0);
        memFree(pDevice);
        if (err != VK_SUCCESS) {
            throw new AssertionError("Failed to create device: " + VKUtil.translateVulkanResult(err));
        }

        VkPhysicalDeviceMemoryProperties memoryProperties = VkPhysicalDeviceMemoryProperties.calloc();
        vkGetPhysicalDeviceMemoryProperties(physicalDevice, memoryProperties);

        this.device = new VkDevice(device, physicalDevice, deviceCreateInfo);
        this.queueFamilyIndex = graphicsQueueFamilyIndex;
        this.memoryProperties = memoryProperties;

        deviceCreateInfo.free();
        memFree(ppEnabledLayerNames);
        memFree(VK_KHR_SWAPCHAIN_EXTENSION);
        memFree(extensions);
        memFree(pQueuePriorities);

    }
	
	public VkDevice getDevice() {
		return device;
	}

	public int getQueueFamilyIndex() {
		return queueFamilyIndex;
	}

	public VkPhysicalDeviceMemoryProperties getMemoryProperties() {
		return memoryProperties;
	}
	
}
