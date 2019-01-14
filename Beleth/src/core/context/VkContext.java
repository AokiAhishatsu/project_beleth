package core.context;

import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memUTF8;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import core.context.BaseContext;
import core.context.DeviceManager.DeviceType;
import core.descriptor.DescriptorPool;
import core.device.LogicalDevice;
import core.device.PhysicalDevice;
import core.device.VkDeviceBundle;
import core.platform.VkWindow;
import core.scenegraph.VkCamera;
import core.util.VkUtil;
import launcher.VideoMode;

public class VkContext extends BaseContext {

	private static ByteBuffer[] enabledLayers;
	private static VulkanInstance vkInstance;
	private static VkResources resources;
	private static DeviceManager deviceManager;
	private static long surface;

	public static void create() {

		init();

		window = new VkWindow();
		camera = new VkCamera();
		resources = new VkResources();
		deviceManager = new DeviceManager();

		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		if (!glfwVulkanSupported()) {
			throw new AssertionError("GLFW failed to find the Vulkan loader");
		}

		ByteBuffer[] layers = { memUTF8("VK_LAYER_LUNARG_standard_validation")
//            	memUTF8("VK_LAYER_LUNARG_assistant_layer")
		};

		enabledLayers = layers;

		vkInstance = new VulkanInstance(VkUtil.getValidationLayerNames(
				Integer.valueOf(config.getProperties().getProperty("validation.enable")) == 1 ? true : false, layers));

		getWindow().create();

		LongBuffer pSurface = memAllocLong(1);
		int err = glfwCreateWindowSurface(vkInstance.getHandle(), BaseContext.getWindow().getId(), null, pSurface);

		surface = pSurface.get(0);
		if (err != VK_SUCCESS) {
			throw new AssertionError("Failed to create surface: " + VkUtil.translateVulkanResult(err));
		}

		PhysicalDevice physicalDevice = new PhysicalDevice(vkInstance.getHandle(), surface);
		LogicalDevice logicalDevice = new LogicalDevice(physicalDevice, 0);
		VkDeviceBundle majorDevice = new VkDeviceBundle(physicalDevice, logicalDevice);
		VkContext.getDeviceManager().addDevice(DeviceType.MAJOR_GRAPHICS_DEVICE, majorDevice);

		DescriptorPool descriptorPool = new DescriptorPool(majorDevice.getLogicalDevice().getHandle(), 4);
		descriptorPool.addPoolSize(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 33);
		descriptorPool.addPoolSize(VK_DESCRIPTOR_TYPE_STORAGE_IMAGE, 61);
		descriptorPool.addPoolSize(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, 2);
		descriptorPool.addPoolSize(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 12);
		descriptorPool.create();
		majorDevice.getLogicalDevice().addDescriptorPool(Thread.currentThread().getId(), descriptorPool);
	}

	public static VkCamera getCamera() {

		return (VkCamera) camera;
	}

	public static VkWindow getWindow() {

		return (VkWindow) window;
	}

	public static ByteBuffer[] getEnabledLayers() {
		return enabledLayers;
	}

	public static VulkanInstance getVkInstance() {
		return vkInstance;
	}

	public static VkResources getResources() {
		return resources;
	}

	public static DeviceManager getDeviceManager() {
		return deviceManager;
	}

	public static long getSurface() {
		return surface;
	}
}
