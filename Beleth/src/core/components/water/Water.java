package core.components.water;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_SHADER_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_UNORM;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_GENERAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_STORAGE_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_FAMILY_IGNORED;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_MIPMAP_MODE_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_MIPMAP_MODE_NEAREST;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_ALL_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_COMPUTE_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_GEOMETRY_BIT;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkQueue;
import core.math.Vec2f;

import core.math.Vec4f;
import core.memory.VkBuffer;
import core.model.Vertex.VertexLayout;
import core.command.CommandBuffer;
import core.command.SubmitInfo;
import core.common.water.WaterConfiguration;
import core.components.fft.FFT;
import core.components.util.NormalRenderer;
import core.context.BaseContext;
import core.context.DeviceManager.DeviceType;
import core.context.VkContext;
import core.descriptor.DescriptorPool;
import core.descriptor.DescriptorSet;
import core.descriptor.DescriptorSetLayout;
import core.device.LogicalDevice;
import core.device.VkDeviceBundle;
import core.framebuffer.VkFrameBufferObject;
import core.image.VkImage;
import core.image.VkImageView;
import core.image.VkSampler;
import core.pipeline.ShaderModule;
import core.pipeline.ShaderPipeline;
import core.pipeline.VkPipeline;
import core.pipeline.VkVertexInput;
import core.scenegraph.NodeComponentType;
import core.scenegraph.RenderList;
import core.scenegraph.Renderable;
import core.scenegraph.Scenegraph;
import core.scenegraph.VkMeshData;
import core.scenegraph.VkRenderInfo;
import core.synchronization.Fence;
import core.synchronization.VkSemaphore;
import core.target.FrameBufferObject.Attachment;
import core.util.BufferUtil;
import core.util.Constants;
import core.util.MeshGenerator;
import core.util.Util;
import core.util.VkUtil;
import core.wrapper.buffer.VkBufferHelper;
import core.wrapper.buffer.VkUniformBuffer;
import core.wrapper.command.ComputeCmdBuffer;
import core.wrapper.command.MipMapGenerationCmdBuffer;
import core.wrapper.command.PrimaryCmdBuffer;
import core.wrapper.command.SecondaryDrawCmdBuffer;
import core.wrapper.image.Image2DDeviceLocal;
import core.wrapper.image.VkImageHelper;
import core.wrapper.pipeline.GraphicsTessellationPipeline;

public class Water extends Renderable{
	
	private WaterConfiguration waterConfiguration;

	public WaterConfiguration getWaterConfiguration() {
		return waterConfiguration;
	}

	private long systemTime = System.currentTimeMillis();
	private FFT fft;
	private NormalRenderer normalRenderer;
	private Vec4f clipplane;
	private float clip_offset;
	private float motion;
	private float distortion;
	private VkImage image_dudv;
	private VkImageView imageView_dudv;
	
	private VkUniformBuffer uniformBuffer;
	
	private VkSampler dxSampler;
	private VkSampler dySampler;
	private VkSampler dzSampler;
	private VkSampler dudvSampler;
	private VkSampler normalSampler;
	private VkSampler reflectionSampler;
	private VkSampler refractionSampler;
	
	// Reflection/Refraction Resources
	private VkFrameBufferObject offScreenReflecRefracFbo;
	
	// Reflection Resources
	private RenderList offScreenReflectionRenderList;
	private LinkedHashMap<String, CommandBuffer> reflectionSecondaryCmdBuffers;
	private PrimaryCmdBuffer offscreenReflectionCmdBuffer;
	private SubmitInfo offScreenReflectionSubmitInfo;
	private VkPipeline deferredReflectionPipeline;
	private CommandBuffer deferredReflectionCmdBuffer;
	private SubmitInfo deferredReflectionSubmitInfo;
	private VkImage deferredReflectionImage;
	private VkImageView deferredReflectionImageView;
	private CommandBuffer reflectionMipmapGenerationCmd;
	private SubmitInfo reflectionMipmapSubmitInfo;
	private VkSemaphore offscreenReflectionSignalSemaphore;
	private Fence deferredReflectionFence;
	
	// Refraction Resources
	private RenderList offScreenRefractionRenderList;
	private LinkedHashMap<String, CommandBuffer> refractionSecondaryCmdBuffers;
	private PrimaryCmdBuffer offscreenRefractionCmdBuffer;
	private SubmitInfo offScreenRefractionSubmitInfo;
	private VkPipeline deferredRefractionPipeline;
	private CommandBuffer deferredRefractionCmdBuffer;
	private SubmitInfo deferredRefractionSubmitInfo;
	private VkImage deferredRefractionImage;
	private VkImageView deferredRefractionImageView;
	private CommandBuffer refractionMipmapGenerationCmd;
	private SubmitInfo refractionMipmapSubmitInfo;
	private VkSemaphore offscreenRefractionSignalSemaphore;
	private Fence deferredRefractionFence;
	
	// queues for render reflection/refraction
	private VkQueue graphicsQueue;
	private VkQueue computeQueue;
	
	public Water() {
		
		VkDeviceBundle deviceBundle = VkContext.getDeviceManager().getDeviceBundle(DeviceType.MAJOR_GRAPHICS_DEVICE);
		LogicalDevice device = deviceBundle.getLogicalDevice();
		DescriptorPool descriptorPool = device.getDescriptorPool(Thread.currentThread().getId());
		VkPhysicalDeviceMemoryProperties memoryProperties = 
				VkContext.getDeviceManager().getPhysicalDevice(DeviceType.MAJOR_GRAPHICS_DEVICE).getMemoryProperties();
		graphicsQueue = device.getGraphicsQueue();
		computeQueue = device.getComputeQueue();
		
		offScreenReflectionRenderList = new RenderList();
		offScreenRefractionRenderList = new RenderList();
		reflectionSecondaryCmdBuffers = new LinkedHashMap<String, CommandBuffer>();
		refractionSecondaryCmdBuffers = new LinkedHashMap<String, CommandBuffer>();
		offScreenReflecRefracFbo = VkContext.getResources().getOffScreenReflectionFbo();
		
		getWorldTransform().setScaling(Constants.ZFAR,1,Constants.ZFAR);
		getWorldTransform().setTranslation(-Constants.ZFAR/2,0,-Constants.ZFAR/2);
		
		clip_offset = 4;
		clipplane = new Vec4f(0,-1,0,getWorldTransform().getTranslation().getY() + clip_offset);
		
		waterConfiguration = new WaterConfiguration();
		waterConfiguration.loadFile("res/water-config.properties");
		
		image_dudv = VkImageHelper.loadImageFromFileMipmap(
				device.getHandle(), memoryProperties,
				device.getGraphicsCommandPool(Thread.currentThread().getId()).getHandle(),
				device.getGraphicsQueue(),
				"res/textures/water/dudv/dudv1.jpg",
				VK_IMAGE_USAGE_SAMPLED_BIT,
				VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
				VK_ACCESS_SHADER_READ_BIT,
				VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
				VK_QUEUE_FAMILY_IGNORED);
		
		imageView_dudv = new VkImageView(device.getHandle(),
				VK_FORMAT_R8G8B8A8_UNORM, image_dudv.getHandle(), 
				VK_IMAGE_ASPECT_COLOR_BIT, Util.getMipLevelCount(image_dudv.getMetaData()));
		
		deferredReflectionImage = new Image2DDeviceLocal(device.getHandle(), memoryProperties,
				offScreenReflecRefracFbo.getWidth(), offScreenReflecRefracFbo.getHeight(),
				VK_FORMAT_R8G8B8A8_UNORM,
				VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_STORAGE_BIT |
				VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT,
				1, Util.getLog2N(offScreenReflecRefracFbo.getWidth()));
		
		deferredReflectionImageView = new VkImageView(device.getHandle(),
				VK_FORMAT_R8G8B8A8_UNORM, deferredReflectionImage.getHandle(),
				VK_IMAGE_ASPECT_COLOR_BIT,
				Util.getLog2N(offScreenReflecRefracFbo.getWidth()));
		
		deferredRefractionImage = new Image2DDeviceLocal(device.getHandle(), memoryProperties,
				offScreenReflecRefracFbo.getWidth(), offScreenReflecRefracFbo.getHeight(),
				VK_FORMAT_R8G8B8A8_UNORM,
				VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_STORAGE_BIT |
				VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT,
				1, Util.getLog2N(offScreenReflecRefracFbo.getWidth()));
		
		deferredRefractionImageView = new VkImageView(device.getHandle(),
				VK_FORMAT_R8G8B8A8_UNORM, deferredRefractionImage.getHandle(),
				VK_IMAGE_ASPECT_COLOR_BIT,
				Util.getLog2N(offScreenReflecRefracFbo.getWidth()));
		
		dySampler = new VkSampler(device.getHandle(), VK_FILTER_LINEAR, false, 0,
				VK_SAMPLER_MIPMAP_MODE_NEAREST, 0, VK_SAMPLER_ADDRESS_MODE_REPEAT);
		dxSampler = new VkSampler(device.getHandle(), VK_FILTER_LINEAR, false, 0,
				VK_SAMPLER_MIPMAP_MODE_NEAREST, 0, VK_SAMPLER_ADDRESS_MODE_REPEAT);
	    dzSampler = new VkSampler(device.getHandle(), VK_FILTER_LINEAR, false, 0,
	    		VK_SAMPLER_MIPMAP_MODE_NEAREST, 0, VK_SAMPLER_ADDRESS_MODE_REPEAT);
	    dudvSampler = new VkSampler(device.getHandle(), VK_FILTER_LINEAR, false, 0,
	    		VK_SAMPLER_MIPMAP_MODE_LINEAR, Util.getMipLevelCount(image_dudv.getMetaData()),
	    		VK_SAMPLER_ADDRESS_MODE_REPEAT);
	    normalSampler = new VkSampler(device.getHandle(), VK_FILTER_LINEAR, false, 0,
	    		VK_SAMPLER_MIPMAP_MODE_LINEAR, Util.getLog2N(waterConfiguration.getN()),
	    		VK_SAMPLER_ADDRESS_MODE_REPEAT);
	    reflectionSampler = new VkSampler(device.getHandle(), VK_FILTER_LINEAR, false, 0,
	    		VK_SAMPLER_MIPMAP_MODE_LINEAR, Util.getLog2N(offScreenReflecRefracFbo.getWidth()),
	    		VK_SAMPLER_ADDRESS_MODE_REPEAT);
	    refractionSampler = new VkSampler(device.getHandle(), VK_FILTER_LINEAR, false, 0,
	    		VK_SAMPLER_MIPMAP_MODE_LINEAR, Util.getLog2N(offScreenReflecRefracFbo.getWidth()),
	    		VK_SAMPLER_ADDRESS_MODE_REPEAT);
		
		fft = new FFT(deviceBundle,
				waterConfiguration.getN(), waterConfiguration.getL(), waterConfiguration.getT_delta(),
				waterConfiguration.getAmplitude(), waterConfiguration.getWindDirection(),
				waterConfiguration.getWindSpeed(), waterConfiguration.getCapillarWavesSupression());
		
		normalRenderer = new NormalRenderer(
				VkContext.getDeviceManager().getDeviceBundle(DeviceType.MAJOR_GRAPHICS_DEVICE),
				waterConfiguration.getN(), waterConfiguration.getNormalStrength(),
				fft.getDyImageView(), dySampler);
		
		normalRenderer.setWaitSemaphores(fft.getFftSignalSemaphore().getHandlePointer());
		
		ShaderPipeline shaderPipeline = new ShaderPipeline(device.getHandle());
	    shaderPipeline.createVertexShader("res/shaders/water/water.vert.spv");
	    shaderPipeline.createTessellationControlShader("res/shaders/water/water.tesc.spv");
	    shaderPipeline.createTessellationEvaluationShader("res/shaders/water/water.tese.spv");
	    shaderPipeline.createGeometryShader("res/shaders/water/water.geom.spv");
	    shaderPipeline.createFragmentShader("res/shaders/water/water.frag.spv");
	    shaderPipeline.createShaderPipeline();
	    
	    ByteBuffer ubo = memAlloc(Float.BYTES * 2);
		ubo.putFloat(0);
		ubo.putFloat(0);
		ubo.flip();
	    
	    uniformBuffer = new VkUniformBuffer(device.getHandle(), memoryProperties, ubo);
	    
	    DescriptorSetLayout descriptorSetLayout = new DescriptorSetLayout(device.getHandle(), 8);
	    descriptorSetLayout.addLayoutBinding(0, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
	    		VK_SHADER_STAGE_GEOMETRY_BIT | VK_SHADER_STAGE_FRAGMENT_BIT);
	    descriptorSetLayout.addLayoutBinding(1, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
				VK_SHADER_STAGE_GEOMETRY_BIT | VK_SHADER_STAGE_FRAGMENT_BIT);
	    descriptorSetLayout.addLayoutBinding(2, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
				VK_SHADER_STAGE_GEOMETRY_BIT | VK_SHADER_STAGE_FRAGMENT_BIT);
	    descriptorSetLayout.addLayoutBinding(3, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
				VK_SHADER_STAGE_FRAGMENT_BIT);
	    descriptorSetLayout.addLayoutBinding(4, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
				VK_SHADER_STAGE_FRAGMENT_BIT);
	    descriptorSetLayout.addLayoutBinding(5, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
				VK_SHADER_STAGE_FRAGMENT_BIT);
	    descriptorSetLayout.addLayoutBinding(6, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
				VK_SHADER_STAGE_FRAGMENT_BIT);
	    descriptorSetLayout.addLayoutBinding(7, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
				VK_SHADER_STAGE_FRAGMENT_BIT | VK_SHADER_STAGE_GEOMETRY_BIT);
	    descriptorSetLayout.create();
	    
	    DescriptorSet descriptorSet = new DescriptorSet(device.getHandle(),
	    		descriptorPool.getHandle(),
	    		descriptorSetLayout.getHandlePointer());
	    descriptorSet.updateDescriptorImageBuffer(
	    		fft.getDyImageView().getHandle(),
	    		VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
	    		dySampler.getHandle(), 0, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
	    descriptorSet.updateDescriptorImageBuffer(
	    		fft.getDxImageView().getHandle(),
	    		VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
	    		dxSampler.getHandle(), 1, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
	    descriptorSet.updateDescriptorImageBuffer(
	    		fft.getDzImageView().getHandle(),
	    		VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
	    		dzSampler.getHandle(), 2, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
	    descriptorSet.updateDescriptorImageBuffer(
	    		imageView_dudv.getHandle(),
	    		VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
	    		dudvSampler.getHandle(), 3, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
	    descriptorSet.updateDescriptorImageBuffer(
	    		normalRenderer.getNormalImageView().getHandle(),
	    		VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
	    		normalSampler.getHandle(), 4, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
	    descriptorSet.updateDescriptorImageBuffer(
	    		deferredReflectionImageView.getHandle(),
	    		VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
	    		reflectionSampler.getHandle(), 5, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
	    descriptorSet.updateDescriptorImageBuffer(
	    		deferredRefractionImageView.getHandle(),
	    		VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
	    		refractionSampler.getHandle(), 6, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
	    descriptorSet.updateDescriptorBuffer(uniformBuffer.getHandle(),
	    		ubo.limit(), 0, 7, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
	    
	    List<DescriptorSet> descriptorSets = new ArrayList<DescriptorSet>();
		List<DescriptorSetLayout> descriptorSetLayouts = new ArrayList<DescriptorSetLayout>();
		
		descriptorSets.add(VkContext.getCamera().getDescriptorSet());
		descriptorSets.add(descriptorSet);
		descriptorSetLayouts.add(VkContext.getCamera().getDescriptorSetLayout());
		descriptorSetLayouts.add(descriptorSetLayout);
	    
	    VkVertexInput vertexInput = new VkVertexInput(VertexLayout.POS2D);
		
	    Vec2f[] vertices = MeshGenerator.generatePatch2D4x4(128);
	    
		ByteBuffer vertexBuffer = BufferUtil.createByteBuffer(vertices);
		
		VkBuffer vertexBufferObject = VkBufferHelper.createDeviceLocalBuffer(
				device.getHandle(), memoryProperties,
				device.getTransferCommandPool(Thread.currentThread().getId()).getHandle(),
				device.getTransferQueue(),
				vertexBuffer, VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		
		int pushConstantsRange = Float.BYTES * 26 + Integer.BYTES * 5;
		
		ByteBuffer pushConstants = memAlloc(pushConstantsRange);
		pushConstants.put(BufferUtil.createByteBuffer(getWorldTransform().getWorldMatrix()));
		pushConstants.putFloat(waterConfiguration.getWindDirection().getX());
		pushConstants.putFloat(waterConfiguration.getWindDirection().getY());
		pushConstants.putFloat(waterConfiguration.getTessellationSlope());
		pushConstants.putFloat(waterConfiguration.getTessellationShift());
		pushConstants.putInt(waterConfiguration.getTessellationFactor());
		pushConstants.putInt(waterConfiguration.getUvScale());
		pushConstants.putFloat(waterConfiguration.getDisplacementScale());
		pushConstants.putFloat(waterConfiguration.getChoppiness());
		pushConstants.putInt(waterConfiguration.getHighDetailRange());
		pushConstants.putFloat(waterConfiguration.getKReflection());
		pushConstants.putFloat(waterConfiguration.getKRefraction());
		pushConstants.putInt(BaseContext.getConfig().getWindowWidth());
		pushConstants.putInt(BaseContext.getConfig().getWindowHeight());
		pushConstants.putFloat(waterConfiguration.getEmission());
		pushConstants.putFloat(waterConfiguration.getSpecular());
		pushConstants.flip();
		
		VkPipeline graphicsPipeline = new GraphicsTessellationPipeline(device.getHandle(),
				shaderPipeline, vertexInput, VkUtil.createLongBuffer(descriptorSetLayouts),
				BaseContext.getConfig().getX_ScreenResolution(),
				BaseContext.getConfig().getY_ScreenResolution(),
				VkContext.getResources().getOffScreenFbo().getRenderPass().getHandle(),
				VkContext.getResources().getOffScreenFbo().getColorAttachmentCount(),
				BaseContext.getConfig().getMultisamples(),
				pushConstantsRange, VK_SHADER_STAGE_ALL_GRAPHICS,
				16);
		
		CommandBuffer commandBuffer = new SecondaryDrawCmdBuffer(
	    		device.getHandle(), device.getGraphicsCommandPool(Thread.currentThread().getId()).getHandle(), 
	    		graphicsPipeline.getHandle(), graphicsPipeline.getLayoutHandle(),
	    		VkContext.getResources().getOffScreenFbo().getFrameBuffer().getHandle(),
	    		VkContext.getResources().getOffScreenFbo().getRenderPass().getHandle(),
	    		0,
	    		VkUtil.createLongArray(descriptorSets),
	    		vertexBufferObject.getHandle(),
	    		vertices.length,
	    		pushConstants, VK_SHADER_STAGE_ALL_GRAPHICS);
	    
		VkMeshData meshData = VkMeshData.builder().vertexBufferObject(vertexBufferObject)
		    		.vertexBuffer(vertexBuffer).build();
		VkRenderInfo mainRenderInfo = VkRenderInfo.builder().commandBuffer(commandBuffer)
				.descriptorSets(descriptorSets).descriptorSetLayouts(descriptorSetLayouts)
				.pipeline(graphicsPipeline).build();
	    
	    addComponent(NodeComponentType.MESH_DATA, meshData);
	    addComponent(NodeComponentType.MAIN_RENDERINFO, mainRenderInfo);
	    
	    createReflectionRefractionResources(device, memoryProperties, descriptorPool);
	}
	
	public void createReflectionRefractionResources(LogicalDevice device,
			VkPhysicalDeviceMemoryProperties memoryProperties,
			DescriptorPool descriptorPool){
		
		List<DescriptorSet> descriptorSets = new ArrayList<DescriptorSet>();
		List<DescriptorSetLayout> descriptorSetLayouts = new ArrayList<DescriptorSetLayout>();
		
		DescriptorSetLayout descriptorSetLayout = new DescriptorSetLayout(device.getHandle(), 3);
		descriptorSetLayout.addLayoutBinding(0, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
				VK_SHADER_STAGE_COMPUTE_BIT);
		descriptorSetLayout.addLayoutBinding(1, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
				VK_SHADER_STAGE_COMPUTE_BIT);
		descriptorSetLayout.addLayoutBinding(2, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE,
				VK_SHADER_STAGE_COMPUTE_BIT);
	    descriptorSetLayout.create();
	    
	    DescriptorSet descriptorSetReflection = new DescriptorSet(device.getHandle(),
	    		descriptorPool.getHandle(),
	    		descriptorSetLayout.getHandlePointer());
	    descriptorSetReflection.updateDescriptorImageBuffer(
	    		deferredReflectionImageView.getHandle(),
	    		VK_IMAGE_LAYOUT_GENERAL, -1,
	    		0, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
	    descriptorSetReflection.updateDescriptorImageBuffer(
	    		offScreenReflecRefracFbo.getAttachmentImageView(Attachment.ALBEDO).getHandle(),
	    		VK_IMAGE_LAYOUT_GENERAL, -1,
	    		1, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
	    descriptorSetReflection.updateDescriptorImageBuffer(
	    		offScreenReflecRefracFbo.getAttachmentImageView(Attachment.NORMAL).getHandle(),
	    		VK_IMAGE_LAYOUT_GENERAL, -1,
	    		2, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
		
		descriptorSets.add(VkContext.getCamera().getDescriptorSet());
		descriptorSets.add(descriptorSetReflection);
		descriptorSetLayouts.add(VkContext.getCamera().getDescriptorSetLayout());
		descriptorSetLayouts.add(descriptorSetLayout);
		
		DescriptorSet descriptorSetRefraction = new DescriptorSet(device.getHandle(),
				descriptorPool.getHandle(),
	    		descriptorSetLayout.getHandlePointer());
		descriptorSetRefraction.updateDescriptorImageBuffer(
				deferredRefractionImageView.getHandle(),
	    		VK_IMAGE_LAYOUT_GENERAL, -1,
	    		0, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
		descriptorSetRefraction.updateDescriptorImageBuffer(
				offScreenReflecRefracFbo.getAttachmentImageView(Attachment.ALBEDO).getHandle(),
	    		VK_IMAGE_LAYOUT_GENERAL, -1,
	    		1, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);
		descriptorSetRefraction.updateDescriptorImageBuffer(
				offScreenReflecRefracFbo.getAttachmentImageView(Attachment.NORMAL).getHandle(),
	    		VK_IMAGE_LAYOUT_GENERAL, -1,
	    		2, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE);

		deferredReflectionPipeline = new VkPipeline(device.getHandle());
		deferredReflectionPipeline.setLayout(VkUtil.createLongBuffer(descriptorSetLayouts));
		deferredReflectionPipeline.createComputePipeline(
				new ShaderModule(device.getHandle(), "res/shaders/water/waterDeferredReflecRefrac.comp.spv",
						VK_SHADER_STAGE_COMPUTE_BIT));
		
		deferredReflectionCmdBuffer = new ComputeCmdBuffer(device.getHandle(),
				device.getComputeCommandPool(Thread.currentThread().getId()).getHandle(),
				deferredReflectionPipeline.getHandle(),
				deferredReflectionPipeline.getLayoutHandle(),
				VkUtil.createLongArray(descriptorSets),
				offScreenReflecRefracFbo.getWidth()/8, offScreenReflecRefracFbo.getHeight()/8, 1);
		
		deferredRefractionPipeline = new VkPipeline(device.getHandle());
		descriptorSets.set(1, descriptorSetRefraction);
		deferredRefractionPipeline.setLayout(VkUtil.createLongBuffer(descriptorSetLayouts));
		deferredRefractionPipeline.createComputePipeline(
				new ShaderModule(device.getHandle(), "res/shaders/water/waterDeferredReflecRefrac.comp.spv",
						VK_SHADER_STAGE_COMPUTE_BIT));
		
		descriptorSets.set(1, descriptorSetRefraction);
		
		deferredRefractionCmdBuffer = new ComputeCmdBuffer(device.getHandle(),
				device.getComputeCommandPool(Thread.currentThread().getId()).getHandle(),
				deferredRefractionPipeline.getHandle(),
				deferredRefractionPipeline.getLayoutHandle(),
				VkUtil.createLongArray(descriptorSets),
				offScreenReflecRefracFbo.getWidth()/8, offScreenReflecRefracFbo.getHeight()/8, 1);
		
		offscreenReflectionCmdBuffer = new PrimaryCmdBuffer(device.getHandle(), 
				device.getComputeCommandPool(Thread.currentThread().getId()).getHandle());
		
		offscreenRefractionCmdBuffer = new PrimaryCmdBuffer(device.getHandle(), 
				device.getComputeCommandPool(Thread.currentThread().getId()).getHandle());
		
		deferredReflectionFence = new Fence(device.getHandle());
		deferredRefractionFence = new Fence(device.getHandle());
		
		offscreenReflectionSignalSemaphore = new VkSemaphore(device.getHandle());
		offscreenRefractionSignalSemaphore = new VkSemaphore(device.getHandle());
		
		offScreenReflectionSubmitInfo = new SubmitInfo();
		offScreenReflectionSubmitInfo.setCommandBuffers(offscreenReflectionCmdBuffer.getHandlePointer());
		offScreenReflectionSubmitInfo.setSignalSemaphores(offscreenReflectionSignalSemaphore.getHandlePointer());
		
		IntBuffer pWaitDstStageMask = memAllocInt(1);
		pWaitDstStageMask.put(0, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
		
		offScreenRefractionSubmitInfo = new SubmitInfo();
		offScreenRefractionSubmitInfo.setCommandBuffers(offscreenRefractionCmdBuffer.getHandlePointer());
		offScreenRefractionSubmitInfo.setSignalSemaphores(offscreenRefractionSignalSemaphore.getHandlePointer());
		
		deferredReflectionSubmitInfo = new SubmitInfo();
		deferredReflectionSubmitInfo.setCommandBuffers(deferredReflectionCmdBuffer.getHandlePointer());
		deferredReflectionSubmitInfo.setWaitSemaphores(offscreenReflectionSignalSemaphore.getHandlePointer());
		deferredReflectionSubmitInfo.setWaitDstStageMask(pWaitDstStageMask);
		deferredReflectionSubmitInfo.setFence(deferredReflectionFence);
		
		deferredRefractionSubmitInfo = new SubmitInfo();
		deferredRefractionSubmitInfo.setCommandBuffers(deferredRefractionCmdBuffer.getHandlePointer());
		deferredRefractionSubmitInfo.setWaitSemaphores(offscreenRefractionSignalSemaphore.getHandlePointer());
		deferredRefractionSubmitInfo.setWaitDstStageMask(pWaitDstStageMask);
		deferredRefractionSubmitInfo.setFence(deferredRefractionFence);
		
		reflectionMipmapGenerationCmd = new MipMapGenerationCmdBuffer(device.getHandle(),
				device.getGraphicsCommandPool(Thread.currentThread().getId()).getHandle(),
				deferredReflectionImage.getHandle(),
				offScreenReflecRefracFbo.getWidth(), offScreenReflecRefracFbo.getHeight(),
				Util.getLog2N(offScreenReflecRefracFbo.getWidth()),
				VK_IMAGE_LAYOUT_UNDEFINED, 0, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
				VK_IMAGE_LAYOUT_GENERAL, VK_ACCESS_SHADER_READ_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT);
		
		reflectionMipmapSubmitInfo = new SubmitInfo();
		reflectionMipmapSubmitInfo.setCommandBuffers(reflectionMipmapGenerationCmd.getHandlePointer());
		
		refractionMipmapGenerationCmd = new MipMapGenerationCmdBuffer(device.getHandle(),
				device.getGraphicsCommandPool(Thread.currentThread().getId()).getHandle(),
				deferredRefractionImage.getHandle(),
				offScreenReflecRefracFbo.getWidth(), offScreenReflecRefracFbo.getHeight(),
				Util.getLog2N(offScreenReflecRefracFbo.getWidth()),
				VK_IMAGE_LAYOUT_UNDEFINED, 0, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
				VK_IMAGE_LAYOUT_GENERAL, VK_ACCESS_SHADER_READ_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT);
		
		refractionMipmapSubmitInfo = new SubmitInfo();
		refractionMipmapSubmitInfo.setCommandBuffers(refractionMipmapGenerationCmd.getHandlePointer());
	}
	
	public void render(){
		
		fft.render();
		normalRenderer.render(VK_QUEUE_FAMILY_IGNORED);
		
		// render reflection
		BaseContext.getConfig().setClipplane(clipplane);
		
		// mirror scene to clipplane
		Scenegraph sceneGraph = getParentObject();
		sceneGraph.getWorldTransform().setScaling(1,-1,1);
		sceneGraph.update();
		sceneGraph.getRoot().record(offScreenReflectionRenderList);
		
		for (String key : offScreenReflectionRenderList.getKeySet()) {
			
			if(!reflectionSecondaryCmdBuffers.containsKey(key)){
				if (offScreenReflectionRenderList.get(key).getComponents()
						.containsKey(NodeComponentType.REFLECTION_RENDERINFO)){
					VkRenderInfo renderInfo = offScreenReflectionRenderList.get(key)
							.getComponent(NodeComponentType.REFLECTION_RENDERINFO);
					reflectionSecondaryCmdBuffers.put(key,renderInfo.getCommandBuffer());
				}
			}
		}
		
		// render reflection scene
		if (!offScreenReflectionRenderList.getObjectList().isEmpty()){
			offscreenReflectionCmdBuffer.reset();
			offscreenReflectionCmdBuffer.record(
					offScreenReflecRefracFbo.getRenderPass().getHandle(),
					offScreenReflecRefracFbo.getFrameBuffer().getHandle(),
					offScreenReflecRefracFbo.getWidth(),
					offScreenReflecRefracFbo.getHeight(),
					offScreenReflecRefracFbo.getColorAttachmentCount(),
					offScreenReflecRefracFbo.getDepthAttachmentCount(),
					VkUtil.createPointerBuffer(reflectionSecondaryCmdBuffers.values()));
			offScreenReflectionSubmitInfo.submit(
					graphicsQueue);
		}
		
		deferredReflectionSubmitInfo.submit(computeQueue);
		deferredReflectionFence.waitForFence();
		
		// antimirror scene to clipplane
		sceneGraph.getWorldTransform().setScaling(1,1,1);
		sceneGraph.update();
		sceneGraph.getRoot().record(offScreenRefractionRenderList);
		
		for (String key : offScreenRefractionRenderList.getKeySet()) {
			
			if(!refractionSecondaryCmdBuffers.containsKey(key)){
				if (offScreenRefractionRenderList.get(key).getComponents()
						.containsKey(NodeComponentType.REFLECTION_RENDERINFO)){
					VkRenderInfo renderInfo = offScreenRefractionRenderList.get(key)
							.getComponent(NodeComponentType.REFLECTION_RENDERINFO);
					refractionSecondaryCmdBuffers.put(key,renderInfo.getCommandBuffer());
				}
			}
		}
		
		// render refraction scene
		if (!offScreenRefractionRenderList.getObjectList().isEmpty()){
			offscreenRefractionCmdBuffer.reset();
			offscreenRefractionCmdBuffer.record(
					offScreenReflecRefracFbo.getRenderPass().getHandle(),
					offScreenReflecRefracFbo.getFrameBuffer().getHandle(),
					offScreenReflecRefracFbo.getWidth(),
					offScreenReflecRefracFbo.getHeight(),
					offScreenReflecRefracFbo.getColorAttachmentCount(),
					offScreenReflecRefracFbo.getDepthAttachmentCount(),
					Constants.DEEPOCEAN_COLOR,
					VkUtil.createPointerBuffer(refractionSecondaryCmdBuffers.values()));
			offScreenRefractionSubmitInfo.submit(
					graphicsQueue);
		}
		
		deferredRefractionSubmitInfo.submit(computeQueue);
		
		reflectionMipmapSubmitInfo.submit(graphicsQueue);
		
		deferredRefractionFence.waitForFence();
		refractionMipmapSubmitInfo.submit(graphicsQueue);
		
		motion += (System.currentTimeMillis() - systemTime) * waterConfiguration.getWaveMotion();
		distortion += (System.currentTimeMillis() - systemTime) * waterConfiguration.getDistortion();
		float[] v = {motion, distortion};
		uniformBuffer.mapMemory(BufferUtil.createByteBuffer(v));
		systemTime = System.currentTimeMillis();
	}
	
	@Override
	public void shutdown(){
		
		super.shutdown();
		
		fft.destroy();
		normalRenderer.destroy();
		image_dudv.destroy();
		imageView_dudv.destroy();
		uniformBuffer.destroy();
		dxSampler.destroy();
		dySampler.destroy();
		dzSampler.destroy();
		dudvSampler.destroy();
		normalSampler.destroy();
		reflectionSampler.destroy();
		refractionSampler.destroy();
		offscreenReflectionCmdBuffer.destroy();
		deferredReflectionPipeline.destroy();
		deferredReflectionCmdBuffer.destroy();
		deferredReflectionImage.destroy();
		deferredReflectionImageView.destroy();
		reflectionMipmapGenerationCmd.destroy();
		offscreenReflectionSignalSemaphore.destroy();
		deferredReflectionFence.destroy();
		offscreenRefractionCmdBuffer.destroy();
		deferredRefractionPipeline.destroy();
		deferredRefractionCmdBuffer.destroy();
		deferredRefractionImage.destroy();
		deferredRefractionImageView.destroy();
		refractionMipmapGenerationCmd.destroy();
		offscreenRefractionSignalSemaphore.destroy();
		deferredRefractionFence.destroy();
	}
}
