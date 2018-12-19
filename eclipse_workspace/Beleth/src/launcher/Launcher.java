package launcher;

import static org.lwjgl.vulkan.EXTDebugReport.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.glfw.GLFWVulkan.*;

import java.nio.LongBuffer;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDebugReportCallbackEXT;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkQueue;

import core.ColorAndDepthFormatAndSpace;
import core.DeviceAndGraphicsQueueFamily;
import core.Vertices;
import core.VulkanCommand;
import core.VulkanInstance;
import helpers.Debugger;
import helpers.VKUtil;
import window.Window;

public class Launcher {

	public static void main(String[] args) {
		run();
	}
	
	private static void run() {
		//Init the window with 720x480 resolution and "Vulkan" as a title
		Window window = new Window(720, 480, "Vulkan");
		
		//Init a Vulkan pipeline for rendering
		VulkanInstance instance = new VulkanInstance();
        final VkDebugReportCallbackEXT debugCallback = new VkDebugReportCallbackEXT() {
            public int invoke(int flags, int objectType, long object, long location, int messageCode, long pLayerPrefix, long pMessage, long pUserData) {
                System.err.println("ERROR OCCURED: " + VkDebugReportCallbackEXT.getString(pMessage));
                return 0;
            }
        };
        long debugCallbackHandle = Debugger.setupDebugging(instance.getInstance(), VK_DEBUG_REPORT_ERROR_BIT_EXT | VK_DEBUG_REPORT_WARNING_BIT_EXT, debugCallback);
        DeviceAndGraphicsQueueFamily deviceAndGraphicsQueueFamily = new DeviceAndGraphicsQueueFamily(instance.getPhysicalDevice());
        VkDevice device = deviceAndGraphicsQueueFamily.getDevice();
        int queueFamilyIndex = deviceAndGraphicsQueueFamily.getQueueFamilyIndex();
        VkPhysicalDeviceMemoryProperties memoryProperties = deviceAndGraphicsQueueFamily.getMemoryProperties();
        long surface = createSurface(instance, window);
        ColorAndDepthFormatAndSpace colorAndDepthFormatAndSpace = new ColorAndDepthFormatAndSpace(instance.getPhysicalDevice(), surface);
        
        long commandPool = VulkanCommand.createCommandPool(device, queueFamilyIndex);
        VkCommandBuffer setupCommandBuffer = VulkanCommand.createCommandBuffer(device, commandPool);
        VkCommandBuffer postPresentCommandBuffer = VulkanCommand.createCommandBuffer(device, commandPool);
        VkQueue queue = VulkanCommand.createDeviceQueue(device, queueFamilyIndex);
        long renderPass = VulkanCommand.createRenderPass(device, colorAndDepthFormatAndSpace.getColorFormat(), colorAndDepthFormatAndSpace.getDepthFormat());
        long renderCommandPool = VulkanCommand.createCommandPool(device, queueFamilyIndex);
        
        
        Vertices vertices = new Vertices(memoryProperties, device);
        
		loop(window);
		
		//Free the window callbacks and destroy the window
		freeWindow(window, instance.getInstance());
		
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
	
	private static long createSurface(VulkanInstance instance, Window window) {
		LongBuffer pSurface = memAllocLong(1);
        int err = glfwCreateWindowSurface(instance.getInstance(), window.id(), null, pSurface);
        if (err != VK_SUCCESS) {
            throw new AssertionError("Failed to create surface: " + VKUtil.translateVulkanResult(err));
        }
        return pSurface.get(0);
	}

}
