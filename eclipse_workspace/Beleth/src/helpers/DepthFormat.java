package helpers;

import java.nio.IntBuffer;

import org.lwjgl.vulkan.VkFormatProperties;
import org.lwjgl.vulkan.VkPhysicalDevice;

import static org.lwjgl.vulkan.VK10.*;

public class DepthFormat {
	
	public static boolean getSupportedDepthFormat(VkPhysicalDevice physicalDevice, IntBuffer depthFormat) {
        // Since all depth formats may be optional, we need to find a suitable depth format to use
        // Start with the highest precision packed format
        int[] depthFormats = { 
            VK_FORMAT_D32_SFLOAT_S8_UINT,
            VK_FORMAT_D32_SFLOAT,
            VK_FORMAT_D24_UNORM_S8_UINT,
            VK_FORMAT_D16_UNORM_S8_UINT,
            VK_FORMAT_D16_UNORM
        };

        VkFormatProperties formatProps = VkFormatProperties.calloc();
        for (int format : depthFormats) {
            vkGetPhysicalDeviceFormatProperties(physicalDevice, format, formatProps);
            // Format must support depth stencil attachment for optimal tiling
            if ((formatProps.optimalTilingFeatures() & VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT) != 0) {
                depthFormat.put(0, format);
                return true;
            }
        }
        return false;
    }
	
}
