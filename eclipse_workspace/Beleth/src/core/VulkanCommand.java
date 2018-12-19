package core;

import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;
import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubpassDescription;

import helpers.VKUtil;

public class VulkanCommand {
	
	private static final int VK_FLAGS_NONE = 0;
	
	public static long createCommandPool(VkDevice device, int queueNodeIndex) {
        VkCommandPoolCreateInfo cmdPoolInfo = VkCommandPoolCreateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
                .queueFamilyIndex(queueNodeIndex)
                .flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
        LongBuffer pCmdPool = memAllocLong(1);
        int err = vkCreateCommandPool(device, cmdPoolInfo, null, pCmdPool);
        long commandPool = pCmdPool.get(0);
        cmdPoolInfo.free();
        memFree(pCmdPool);
        if (err != VK_SUCCESS) {
            throw new AssertionError("Failed to create command pool: " + VKUtil.translateVulkanResult(err));
        }
        return commandPool;
    }
	
	public static VkCommandBuffer createCommandBuffer(VkDevice device, long commandPool) {
        VkCommandBufferAllocateInfo cmdBufAllocateInfo = VkCommandBufferAllocateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                .commandPool(commandPool)
                .level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                .commandBufferCount(1);
        PointerBuffer pCommandBuffer = memAllocPointer(1);
        int err = vkAllocateCommandBuffers(device, cmdBufAllocateInfo, pCommandBuffer);
        cmdBufAllocateInfo.free();
        long commandBuffer = pCommandBuffer.get(0);
        memFree(pCommandBuffer);
        if (err != VK_SUCCESS) {
            throw new AssertionError("Failed to allocate command buffer: " + VKUtil.translateVulkanResult(err));
        }
        return new VkCommandBuffer(commandBuffer, device);
    }
	
	public static long createRenderPass(VkDevice device, int colorFormat, int depthFormat) {
        VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc(2);
        attachments.get(0) // <- color attachment
                .format(colorFormat)
                .samples(VK_SAMPLE_COUNT_1_BIT)
                .loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
                .storeOp(VK_ATTACHMENT_STORE_OP_STORE)
                .stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
                .stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
                .initialLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
                .finalLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
        attachments.get(1) // <- depth-stencil attachment
                .format(depthFormat)
                .samples(VK_SAMPLE_COUNT_1_BIT)
                .loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
                .storeOp(VK_ATTACHMENT_STORE_OP_STORE)
                .stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
                .stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
                .initialLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
                .finalLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

        VkAttachmentReference.Buffer colorReference = VkAttachmentReference.calloc(1)
                .attachment(0)
                .layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

        VkAttachmentReference depthReference = VkAttachmentReference.calloc()
                .attachment(1)
                .layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

        VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1)
                .pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
                .flags(VK_FLAGS_NONE)
                .pInputAttachments(null)
                .colorAttachmentCount(colorReference.remaining())
                .pColorAttachments(colorReference) // <- only color attachment
                .pResolveAttachments(null)
                .pDepthStencilAttachment(depthReference) // <- and depth-stencil
                .pPreserveAttachments(null);

        VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
                .pNext(NULL)
                .pAttachments(attachments)
                .pSubpasses(subpass)
                .pDependencies(null);

        LongBuffer pRenderPass = memAllocLong(1);
        int err = vkCreateRenderPass(device, renderPassInfo, null, pRenderPass);
        long renderPass = pRenderPass.get(0);
        memFree(pRenderPass);
        renderPassInfo.free();
        depthReference.free();
        colorReference.free();
        subpass.free();
        attachments.free();
        if (err != VK_SUCCESS) {
            throw new AssertionError("Failed to create clear render pass: " + VKUtil.translateVulkanResult(err));
        }
        return renderPass;
    }
	
	public static VkQueue createDeviceQueue(VkDevice device, int queueFamilyIndex) {
        PointerBuffer pQueue = memAllocPointer(1);
        vkGetDeviceQueue(device, queueFamilyIndex, 0, pQueue);
        long queue = pQueue.get(0);
        memFree(pQueue);
        return new VkQueue(queue, device);
    }
	
	
}
