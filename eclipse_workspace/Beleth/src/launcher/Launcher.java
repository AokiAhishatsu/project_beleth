package launcher;

import static org.lwjgl.vulkan.EXTDebugReport.*;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDebugReportCallbackEXT;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

import core.DeviceAndGraphicsQueueFamily;
import core.VulkanInstant;
import helpers.Debugger;
import window.Window;

public class Launcher {

	public static void main(String[] args) {
		run();
	}
	
	public static void run() {
		//Init the window with 720x480 resolution and "Vulkan" as a title
		Window window = new Window(720, 480, "Vulkan");
		
		//Init a Vulkan pipeline for rendering
		VulkanInstant instance = new VulkanInstant();
        final VkDebugReportCallbackEXT debugCallback = new VkDebugReportCallbackEXT() {
            public int invoke(int flags, int objectType, long object, long location, int messageCode, long pLayerPrefix, long pMessage, long pUserData) {
                System.err.println("ERROR OCCURED: " + VkDebugReportCallbackEXT.getString(pMessage));
                return 0;
            }
        };
        long debugCallbackHandle = Debugger.setupDebugging(instance.getInstant(), VK_DEBUG_REPORT_ERROR_BIT_EXT | VK_DEBUG_REPORT_WARNING_BIT_EXT, debugCallback);
        DeviceAndGraphicsQueueFamily deviceAndGraphicsQueueFamily = new DeviceAndGraphicsQueueFamily(instance.getPhysicalDevice());
        VkDevice device = deviceAndGraphicsQueueFamily.getDevice();
        int queueFamilyIndex = deviceAndGraphicsQueueFamily.getQueueFamilyIndex();
        VkPhysicalDeviceMemoryProperties memoryProperties = deviceAndGraphicsQueueFamily.getMemoryProperties();
        
		loop(window);
		
		//Free the window callbacks and destroy the window
		freeWindow(window, instance.getInstant());
		
	}

	private static void loop(Window window) {
		while ( !GLFW.glfwWindowShouldClose(window.id()) ) {
			GLFW.glfwPollEvents();
		}
	}
	
	private static void freeWindow(Window window, VkInstance instance) {
		VK10.vkDestroyInstance(instance, null);
		Callbacks.glfwFreeCallbacks(window.id());
		GLFW.glfwDestroyWindow(window.id());
		GLFW.glfwTerminate();
		GLFW.glfwSetErrorCallback(null).free();
	}

}
