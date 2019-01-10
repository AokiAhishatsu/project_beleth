package core.engine;

import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_SHADER_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_SHADER_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT;
import static org.lwjgl.vulkan.VK10.vkDeviceWaitIdle;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.LinkedHashMap;

import core.swapchain.SwapChain;
import core.synchronization.VkSemaphore;
import core.target.FrameBufferObject.Attachment;
import core.util.VkUtil;
import core.wrapper.command.PrimaryCmdBuffer;
import core.RenderEngine;
import core.command.CommandBuffer;
import core.command.SubmitInfo;
import core.components.filter.Bloom;
import core.components.planet.Planet;
import core.components.ui.VkGUI;
import core.context.BaseContext;
import core.context.DeviceManager.DeviceType;
import core.context.VkContext;
import core.device.VkDeviceBundle;
import core.framebuffer.VkFrameBufferObject;
import core.image.VkImageView;
import core.scenegraph.NodeComponentType;
import core.scenegraph.RenderList;
import core.scenegraph.VkRenderInfo;

public class VkDeferredEngine extends RenderEngine {
	
	private SwapChain swapChain;
	private VkDeviceBundle graphicsDevice;
	
	private VkFrameBufferObject offScreenFbo;
	private VkFrameBufferObject reflectionFbo;
	private VkFrameBufferObject transparencyFbo;
	
	private VkSemaphore offScreenSemaphore;
	private VkSemaphore deferredStageSemaphore;
	private VkSemaphore transparencySemaphore;
	private VkSemaphore postProcessingSemaphore;
	
	private CommandBuffer deferredStageCmdBuffer;
	private SubmitInfo deferredStageSubmitInfo;
	
	private CommandBuffer postProcessingCmdBuffer;
	private SubmitInfo postProcessingSubmitInfo;
	
	private PrimaryCmdBuffer offScreenPrimaryCmdBuffer;
	private LinkedHashMap<String, CommandBuffer> offScreenSecondaryCmdBuffers;
	private RenderList offScreenRenderList;
	private SubmitInfo offScreenSubmitInfo;
	
	private PrimaryCmdBuffer transparencyPrimaryCmdBuffer;
	private LinkedHashMap<String, CommandBuffer> transparencySecondaryCmdBuffers;
	private RenderList transparencyRenderList;
	private SubmitInfo transparencySubmitInfo;
	
	// uniform buffers
	// private VkUniformBuffer renderStateUbo;

	private SampleCoverage sampleCoverage;
	private DeferredLighting deferredLighting;
	private FXAA fxaa;
	private OpaqueTransparencyBlending opaqueTransparencyBlending;
	
	// post processing filter
	private Bloom bloom;
	
	// gui
	private VkGUI gui;
	public void setGui(VkGUI gui) {
		this.gui = gui;
	}

	@Override
	public void init() {
		
		super.init();
		
		offScreenRenderList = new RenderList();
		transparencyRenderList = new RenderList();
		offScreenSecondaryCmdBuffers = new LinkedHashMap<String, CommandBuffer>();
		transparencySecondaryCmdBuffers = new LinkedHashMap<String, CommandBuffer>();
		
		graphicsDevice = VkContext.getDeviceManager().getDeviceBundle(DeviceType.MAJOR_GRAPHICS_DEVICE);
	    
	    offScreenFbo = new OffScreenFbo(graphicsDevice.getLogicalDevice().getHandle(),
	    		graphicsDevice.getPhysicalDevice().getMemoryProperties());
	    reflectionFbo = new ReflectionFbo(graphicsDevice.getLogicalDevice().getHandle(),
	    		graphicsDevice.getPhysicalDevice().getMemoryProperties());
	    transparencyFbo = new TransparencyFbo(graphicsDevice.getLogicalDevice().getHandle(),
	    		graphicsDevice.getPhysicalDevice().getMemoryProperties());
	    
	    VkContext.getResources().setOffScreenFbo(offScreenFbo);
	    VkContext.getResources().setOffScreenReflectionFbo(reflectionFbo);
	    VkContext.getResources().setTransparencyFbo(transparencyFbo);
	    
	    // Semaphore creations
	    offScreenSemaphore = new VkSemaphore(graphicsDevice.getLogicalDevice().getHandle());
	    deferredStageSemaphore = new VkSemaphore(graphicsDevice.getLogicalDevice().getHandle());
	    transparencySemaphore = new VkSemaphore(graphicsDevice.getLogicalDevice().getHandle());
	    postProcessingSemaphore = new VkSemaphore(graphicsDevice.getLogicalDevice().getHandle());
	    
	    // offscreen opaque primary command buffer creation
	    offScreenPrimaryCmdBuffer =  new PrimaryCmdBuffer(graphicsDevice.getLogicalDevice().getHandle(),
	    		graphicsDevice.getLogicalDevice().getGraphicsCommandPool(Thread.currentThread().getId()).getHandle());
	    offScreenSubmitInfo = new SubmitInfo();
	    offScreenSubmitInfo.setCommandBuffers(offScreenPrimaryCmdBuffer.getHandlePointer());
	    offScreenSubmitInfo.setSignalSemaphores(offScreenSemaphore.getHandlePointer());
	    
	    // offscreen transparency primary command buffer creation
	    transparencyPrimaryCmdBuffer =  new PrimaryCmdBuffer(graphicsDevice.getLogicalDevice().getHandle(),
	    		graphicsDevice.getLogicalDevice().getGraphicsCommandPool(Thread.currentThread().getId()).getHandle());
	    transparencySubmitInfo = new SubmitInfo();
	    transparencySubmitInfo.setCommandBuffers(transparencyPrimaryCmdBuffer.getHandlePointer());
	    transparencySubmitInfo.setSignalSemaphores(transparencySemaphore.getHandlePointer());
	    
	    sampleCoverage = new SampleCoverage(graphicsDevice,
	    		BaseContext.getConfig().getX_ScreenResolution(),
	    		BaseContext.getConfig().getY_ScreenResolution(),
	    		offScreenFbo.getAttachmentImageView(Attachment.POSITION),
	    		offScreenFbo.getAttachmentImageView(Attachment.LIGHT_SCATTERING));
	    
	    deferredLighting = new DeferredLighting(graphicsDevice,
	    		BaseContext.getConfig().getX_ScreenResolution(),
	    		BaseContext.getConfig().getY_ScreenResolution(),
	    		offScreenFbo.getAttachmentImageView(Attachment.ALBEDO),
	    		offScreenFbo.getAttachmentImageView(Attachment.POSITION),
	    		offScreenFbo.getAttachmentImageView(Attachment.NORMAL),
	    		offScreenFbo.getAttachmentImageView(Attachment.SPECULAR_EMISSION),
	    		sampleCoverage.getSampleCoverageImageView());
	    
	    LongBuffer opaqueTransparencyBlendWaitSemaphores = memAllocLong(2);
	    opaqueTransparencyBlendWaitSemaphores.put(0, deferredStageSemaphore.getHandle());
	    opaqueTransparencyBlendWaitSemaphores.put(1, transparencySemaphore.getHandle());
	    opaqueTransparencyBlending = new OpaqueTransparencyBlending(graphicsDevice,
	    		BaseContext.getConfig().getX_ScreenResolution(),
	    		BaseContext.getConfig().getY_ScreenResolution(),
	    		deferredLighting.getDeferredLightingSceneImageView(),
	    		offScreenFbo.getAttachmentImageView(Attachment.DEPTH),
	    		sampleCoverage.getLightScatteringImageView(),
	    		transparencyFbo.getAttachmentImageView(Attachment.ALBEDO),
	    		transparencyFbo.getAttachmentImageView(Attachment.DEPTH),
	    		transparencyFbo.getAttachmentImageView(Attachment.ALPHA),
	    		transparencyFbo.getAttachmentImageView(Attachment.LIGHT_SCATTERING),
	    		opaqueTransparencyBlendWaitSemaphores);
	    
	    VkImageView displayImageView = opaqueTransparencyBlending.getBlendedSceneImageView();

	    if (BaseContext.getConfig().isFxaaEnabled()){
		    fxaa = new FXAA(graphicsDevice,
		    		BaseContext.getConfig().getX_ScreenResolution(),
		    		BaseContext.getConfig().getY_ScreenResolution(),
		    		displayImageView);
		    
		    displayImageView = fxaa.getFxaaImageView();
	    }
	    
	    if (BaseContext.getConfig().isBloomEnabled()){
		    bloom = new Bloom(graphicsDevice,
		    		BaseContext.getConfig().getX_ScreenResolution(),
		    		BaseContext.getConfig().getY_ScreenResolution(),
		    		displayImageView);
		    
		    displayImageView = bloom.getBloomSceneImageView();
	    }
	    
	    if (gui != null){
	    	// all post procssing effects and FXAA disabled
	    	if (!BaseContext.getConfig().isFxaaEnabled() && !BaseContext.getConfig().isBloomEnabled()){
	    		gui.init(displayImageView, opaqueTransparencyBlending.getSignalSemaphore().getHandlePointer());
				displayImageView = gui.getImageView();
	    	}
	    	else{
	    		gui.init(displayImageView, postProcessingSemaphore.getHandlePointer());
				displayImageView = gui.getImageView();
	    	}
		}
	    
	    swapChain = new SwapChain(graphicsDevice.getLogicalDevice(), graphicsDevice.getPhysicalDevice(),
	    		VkContext.getSurface(), displayImageView.getHandle());
	    
	    // record sample coverage + deferred lighting command buffer
	    deferredStageCmdBuffer = new CommandBuffer(graphicsDevice.getLogicalDevice().getHandle(),
	    		graphicsDevice.getLogicalDevice().getComputeCommandPool(Thread.currentThread().getId()).getHandle(),
	    		VK_COMMAND_BUFFER_LEVEL_PRIMARY);
	    deferredStageCmdBuffer.beginRecord(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);
	    sampleCoverage.record(deferredStageCmdBuffer);
	    deferredStageCmdBuffer.pipelineMemoryBarrierCmd(
	    		VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT,
	    		VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
	    		VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
	    deferredLighting.record(deferredStageCmdBuffer);
	    deferredStageCmdBuffer.finishRecord();
	    
	    IntBuffer pComputeShaderWaitDstStageMask = memAllocInt(1);
        pComputeShaderWaitDstStageMask.put(0, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
	    deferredStageSubmitInfo = new SubmitInfo(deferredStageCmdBuffer.getHandlePointer());
	    deferredStageSubmitInfo.setWaitSemaphores(offScreenSemaphore.getHandlePointer());
	    deferredStageSubmitInfo.setWaitDstStageMask(pComputeShaderWaitDstStageMask);
	    deferredStageSubmitInfo.setSignalSemaphores(deferredStageSemaphore.getHandlePointer());
	    
	    // record post processing command buffer
	    postProcessingCmdBuffer = new CommandBuffer(graphicsDevice.getLogicalDevice().getHandle(),
	    		graphicsDevice.getLogicalDevice().getComputeCommandPool(Thread.currentThread().getId()).getHandle(),
	    		VK_COMMAND_BUFFER_LEVEL_PRIMARY);
	    postProcessingCmdBuffer.beginRecord(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);
	    if (BaseContext.getConfig().isFxaaEnabled()){
	    	fxaa.record(postProcessingCmdBuffer);
	    }
	    postProcessingCmdBuffer.pipelineMemoryBarrierCmd(
	    		VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT,
	    		VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
	    		VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
	    if (BaseContext.getConfig().isBloomEnabled()){
	    	bloom.record(postProcessingCmdBuffer);
	    }
	    postProcessingCmdBuffer.finishRecord();
	
	    postProcessingSubmitInfo = new SubmitInfo(postProcessingCmdBuffer.getHandlePointer());
	    postProcessingSubmitInfo.setWaitSemaphores(opaqueTransparencyBlending.getSignalSemaphore().getHandlePointer());
	    postProcessingSubmitInfo.setWaitDstStageMask(pComputeShaderWaitDstStageMask);
	    postProcessingSubmitInfo.setSignalSemaphores(postProcessingSemaphore.getHandlePointer());
	}
    
	@Override
	public void render() {
		
		sceneGraph.render();
		offScreenRenderList.setChanged(false);
		sceneGraph.record(offScreenRenderList);
		
		// update Terrain/Planet Quadtree
		if (sceneGraph.hasTerrain()){
			if (camera.isCameraMoved()){
				// start waiting updateQuadtree thread
				((Planet) sceneGraph.getTerrain()).getQuadtree().signal();
			}
		}

		// record new primary Command Buffer if renderList has changed
		if (offScreenRenderList.hasChanged()){
			
			offScreenSecondaryCmdBuffers.clear();
			
			offScreenRenderList.getKeySet().forEach(key ->
			{
				if(!offScreenSecondaryCmdBuffers.containsKey(key)){
					VkRenderInfo mainRenderInfo = offScreenRenderList.get(key)
							.getComponent(NodeComponentType.MAIN_RENDERINFO);
					offScreenSecondaryCmdBuffers.put(key, mainRenderInfo.getCommandBuffer());
				}
			});

			// primary render command buffer
			offScreenPrimaryCmdBuffer.reset();
			offScreenPrimaryCmdBuffer.record(offScreenFbo.getRenderPass().getHandle(),
					offScreenFbo.getFrameBuffer().getHandle(),
					offScreenFbo.getWidth(),
					offScreenFbo.getHeight(),
					offScreenFbo.getColorAttachmentCount(),
					offScreenFbo.getDepthAttachmentCount(),
					VkUtil.createPointerBuffer(offScreenSecondaryCmdBuffers.values()));
		}
		
		if (!offScreenRenderList.isEmpty()){
			offScreenSubmitInfo.submit(graphicsDevice.getLogicalDevice().getGraphicsQueue());
		}
		
		deferredStageSubmitInfo.submit(graphicsDevice.getLogicalDevice().getComputeQueue());
		
		transparencyRenderList.setChanged(false);
		sceneGraph.recordTransparentObjects(transparencyRenderList);
		
		if (transparencyRenderList.hasChanged()){
			
			transparencySecondaryCmdBuffers.clear();
			
			transparencyRenderList.getKeySet().forEach(key ->
			{
				if(!transparencySecondaryCmdBuffers.containsKey(key)){
					VkRenderInfo mainRenderInfo = transparencyRenderList.get(key)
							.getComponent(NodeComponentType.MAIN_RENDERINFO);
					transparencySecondaryCmdBuffers.put(key, mainRenderInfo.getCommandBuffer());
				}
			});
		
			// primary render command buffer
			transparencyPrimaryCmdBuffer.reset();
			transparencyPrimaryCmdBuffer.record(transparencyFbo.getRenderPass().getHandle(),
					transparencyFbo.getFrameBuffer().getHandle(),
					transparencyFbo.getWidth(),
					transparencyFbo.getHeight(),
					transparencyFbo.getColorAttachmentCount(),
					transparencyFbo.getDepthAttachmentCount(),
					VkUtil.createPointerBuffer(transparencySecondaryCmdBuffers.values()));
		}
		
		if(!transparencyRenderList.isEmpty()){
			transparencySubmitInfo.submit(graphicsDevice.getLogicalDevice().getGraphicsQueue());
		}
		
		opaqueTransparencyBlending.render();
		
		postProcessingSubmitInfo.submit(graphicsDevice.getLogicalDevice().getComputeQueue());
		
		if (gui != null){
			gui.render();
		}
		
		swapChain.draw(graphicsDevice.getLogicalDevice().getGraphicsQueue(),
				gui != null ? gui.getSignalSemaphore(): postProcessingSemaphore);
		swapChain.getDrawFence().waitForFence();
	}

	@Override
	public void update() {

		super.update();
		if (gui != null){
			gui.update();
		}
	}

	@Override
	public void shutdown() {
		
		// wait for queues to be finished before destroy vulkan objects
		vkDeviceWaitIdle(graphicsDevice.getLogicalDevice().getHandle());
		
		super.shutdown();
		
		offScreenFbo.destroy();
		reflectionFbo.destroy();
		transparencyFbo.destroy();
		offScreenSemaphore.destroy();
		deferredStageSemaphore.destroy();
		transparencySemaphore.destroy();
		postProcessingSemaphore.destroy();
		offScreenPrimaryCmdBuffer.destroy();
		deferredStageCmdBuffer.destroy();
		postProcessingCmdBuffer.destroy();
		transparencyPrimaryCmdBuffer.destroy();
		sampleCoverage.shutdown();
		deferredLighting.shutdown();
		if (fxaa != null){
			fxaa.shutdown();
		}
		opaqueTransparencyBlending.shutdown();
		if (bloom != null){
			bloom.shutdown();
		}
		if (gui != null){
			gui.shutdown();
		}
		swapChain.destroy();
		BaseContext.getCamera().shutdown();
		graphicsDevice.getLogicalDevice().destroy();
		VkContext.getVkInstance().destroy();		
	}
	
}
