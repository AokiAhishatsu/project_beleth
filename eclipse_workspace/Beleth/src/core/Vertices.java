package core;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.EXTDebugReport.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import helpers.VKUtil;

public class Vertices {
	
    long verticesBuf;
    VkPipelineVertexInputStateCreateInfo createInfo;
    
    public Vertices(VkPhysicalDeviceMemoryProperties deviceMemoryProperties, VkDevice device) {
    	createVertices(deviceMemoryProperties, device);
    }
    
    public void createVertices(VkPhysicalDeviceMemoryProperties deviceMemoryProperties, VkDevice device) {
        ByteBuffer vertexBuffer = memAlloc(2 * 3 * (3 + 3) * 4);
        FloatBuffer fb = vertexBuffer.asFloatBuffer();
        // first triangle
        fb.put(-0.5f).put(-0.5f).put(0.5f).put(1.0f).put(0.0f).put(0.0f);
        fb.put( 0.5f).put(-0.5f).put(0.5f).put(0.0f).put(1.0f).put(0.0f);
        fb.put( 0.0f).put( 0.5f).put(0.5f).put(0.0f).put(0.0f).put(1.0f);
        // second triangle
        fb.put( 0.5f).put(-0.5f).put(-0.5f).put(1.0f).put(1.0f).put(0.0f);
        fb.put(-0.5f).put(-0.5f).put(-0.5f).put(0.0f).put(1.0f).put(1.0f);
        fb.put( 0.0f).put( 0.5f).put(-0.5f).put(1.0f).put(0.0f).put(1.0f);

        VkMemoryAllocateInfo memAlloc = VkMemoryAllocateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                .pNext(NULL)
                .allocationSize(0)
                .memoryTypeIndex(0);
        VkMemoryRequirements memReqs = VkMemoryRequirements.calloc();

        int err;

        // Generate vertex buffer
        //  Setup
        VkBufferCreateInfo bufInfo = VkBufferCreateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                .pNext(NULL)
                .size(vertexBuffer.remaining())
                .usage(VK_BUFFER_USAGE_VERTEX_BUFFER_BIT)
                .flags(0);
        LongBuffer pBuffer = memAllocLong(1);
        err = vkCreateBuffer(device, bufInfo, null, pBuffer);
        long verticesBuf = pBuffer.get(0);
        memFree(pBuffer);
        bufInfo.free();
        if (err != VK_SUCCESS) {
            throw new AssertionError("Failed to create vertex buffer: " + VKUtil.translateVulkanResult(err));
        }

        vkGetBufferMemoryRequirements(device, verticesBuf, memReqs);
        memAlloc.allocationSize(memReqs.size());
        IntBuffer memoryTypeIndex = memAllocInt(1);
        getMemoryType(deviceMemoryProperties, memReqs.memoryTypeBits(), VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, memoryTypeIndex);
        memAlloc.memoryTypeIndex(memoryTypeIndex.get(0));
        memFree(memoryTypeIndex);
        memReqs.free();

        LongBuffer pMemory = memAllocLong(1);
        err = vkAllocateMemory(device, memAlloc, null, pMemory);
        long verticesMem = pMemory.get(0);
        memFree(pMemory);
        if (err != VK_SUCCESS) {
            throw new AssertionError("Failed to allocate vertex memory: " + VKUtil.translateVulkanResult(err));
        }

        PointerBuffer pData = memAllocPointer(1);
        err = vkMapMemory(device, verticesMem, 0, vertexBuffer.remaining(), 0, pData);
        memAlloc.free();
        long data = pData.get(0);
        memFree(pData);
        if (err != VK_SUCCESS) {
            throw new AssertionError("Failed to map vertex memory: " + VKUtil.translateVulkanResult(err));
        }

        memCopy(memAddress(vertexBuffer), data, vertexBuffer.remaining());
        memFree(vertexBuffer);
        vkUnmapMemory(device, verticesMem);
        err = vkBindBufferMemory(device, verticesBuf, verticesMem, 0);
        if (err != VK_SUCCESS) {
            throw new AssertionError("Failed to bind memory to vertex buffer: " + VKUtil.translateVulkanResult(err));
        }

        // Binding description
        VkVertexInputBindingDescription.Buffer bindingDescriptor = VkVertexInputBindingDescription.calloc(1)
                .binding(0) // <- we bind our vertex buffer to point 0
                .stride((3 + 3) * 4)
                .inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

        // Attribute descriptions
        // Describes memory layout and shader attribute locations
        VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(2);
        // Location 0 : Position
        attributeDescriptions.get(0)
                .binding(0) // <- binding point used in the VkVertexInputBindingDescription
                .location(0) // <- location in the shader's attribute layout (inside the shader source)
                .format(VK_FORMAT_R32G32B32_SFLOAT)
                .offset(0);
        // Location 1 : Color
        attributeDescriptions.get(1)
                .binding(0) // <- binding point used in the VkVertexInputBindingDescription
                .location(1) // <- location in the shader's attribute layout (inside the shader source)
                .format(VK_FORMAT_R32G32B32_SFLOAT)
                .offset(3 * 4);

        // Assign to vertex buffer
        VkPipelineVertexInputStateCreateInfo vi = VkPipelineVertexInputStateCreateInfo.calloc();
        vi.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
        vi.pNext(NULL);
        vi.pVertexBindingDescriptions(bindingDescriptor);
        vi.pVertexAttributeDescriptions(attributeDescriptions);

        this.createInfo = vi;
        this.verticesBuf = verticesBuf;

    }
}