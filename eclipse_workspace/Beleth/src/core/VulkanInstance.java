package core;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.EXTDebugReport.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.glfw.GLFWVulkan.*;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;

import helpers.VKUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class VulkanInstance {
	
	private VkInstance instance;
	private VkApplicationInfo appInfo;
	private VkPhysicalDevice physicalDevice;
	
	private static final boolean validation = Boolean.parseBoolean(System.getProperty("vulkan.validation", "false"));
	private static ByteBuffer[] layers = {
            memUTF8("VK_LAYER_LUNARG_standard_validation"),
    };
	
	public VulkanInstance() {
		if ( !glfwVulkanSupported() )
            throw new AssertionError("GLFW failed to find the Vulkan loader");
		PointerBuffer requiredExtensions = glfwGetRequiredInstanceExtensions();
        if (requiredExtensions == null) {
            throw new AssertionError("Failed to find list of required Vulkan extensions");
        }
		this.instance = createInstance(requiredExtensions);
		this.physicalDevice = getFirstPhysicalDevice(instance);
	}
	
	private VkInstance createInstance(PointerBuffer requiredExtensions) {
        appInfo = VkApplicationInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
                .pApplicationName(memUTF8("GLFW Vulkan Demo"))
                .pEngineName(memUTF8(""))
                .apiVersion(VK_MAKE_VERSION(1, 0, 2));
        PointerBuffer ppEnabledExtensionNames = memAllocPointer(requiredExtensions.remaining() + 1);
        ppEnabledExtensionNames.put(requiredExtensions);
        ByteBuffer VK_EXT_DEBUG_REPORT_EXTENSION = memUTF8(VK_EXT_DEBUG_REPORT_EXTENSION_NAME);
        ppEnabledExtensionNames.put(VK_EXT_DEBUG_REPORT_EXTENSION);
        ppEnabledExtensionNames.flip();
        PointerBuffer ppEnabledLayerNames = memAllocPointer(layers.length);
        for (int i = 0; validation && i < layers.length; i++)
            ppEnabledLayerNames.put(layers[i]);
        ppEnabledLayerNames.flip();
        VkInstanceCreateInfo pCreateInfo = VkInstanceCreateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                .pNext(NULL)
                .pApplicationInfo(appInfo)
                .ppEnabledExtensionNames(ppEnabledExtensionNames)
                .ppEnabledLayerNames(ppEnabledLayerNames);
        PointerBuffer pInstance = memAllocPointer(1);
        int err = vkCreateInstance(pCreateInfo, null, pInstance);
        long instance = pInstance.get(0);
        memFree(pInstance);
        if (err != VK_SUCCESS) {
            throw new AssertionError("Failed to create VkInstance: " + VKUtil.translateVulkanResult(err));
        }
        VkInstance ret = new VkInstance(instance, pCreateInfo);
        pCreateInfo.free();
        memFree(ppEnabledLayerNames);
        memFree(VK_EXT_DEBUG_REPORT_EXTENSION);
        memFree(ppEnabledExtensionNames);
        memFree(appInfo.pApplicationName());
        memFree(appInfo.pEngineName());
        appInfo.free();
        return ret;
    }
	
	private VkPhysicalDevice getFirstPhysicalDevice(VkInstance instance) {
        IntBuffer pPhysicalDeviceCount = memAllocInt(1);
        int err = vkEnumeratePhysicalDevices(instance, pPhysicalDeviceCount, null);
        if (err != VK_SUCCESS) {
            throw new AssertionError("Failed to get number of physical devices: " + VKUtil.translateVulkanResult(err));
        }
        PointerBuffer pPhysicalDevices = memAllocPointer(pPhysicalDeviceCount.get(0));
        err = vkEnumeratePhysicalDevices(instance, pPhysicalDeviceCount, pPhysicalDevices);
        long physicalDevice = pPhysicalDevices.get(0);
        memFree(pPhysicalDeviceCount);
        memFree(pPhysicalDevices);
        if (err != VK_SUCCESS) {
            throw new AssertionError("Failed to get physical devices: " + VKUtil.translateVulkanResult(err));
        }
        return new VkPhysicalDevice(physicalDevice, instance);
    }
	
	public VkApplicationInfo getAppInfo() {
		return appInfo;
	}
	
	public VkInstance getInstance() {
		return instance;
	}
	
	public VkPhysicalDevice getPhysicalDevice() {
		return physicalDevice;
	}

}
