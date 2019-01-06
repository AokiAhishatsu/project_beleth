package core.components.terrain;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_ALL_GRAPHICS;

import java.nio.ByteBuffer;
import java.util.Map;

import core.common.quadtree.QuadtreeCache;
import core.common.quadtree.QuadtreeChunk;
import core.context.BaseContext;
import core.math.Transform;
import core.math.Vec2f;
import core.math.Vec4f;
import core.scenegraph.NodeComponent;
import core.scenegraph.NodeComponentType;
import core.util.BufferUtil;
import core.command.CommandBuffer;
import core.context.DeviceManager.DeviceType;
import core.context.VkContext;
import core.device.LogicalDevice;
import core.pipeline.VkPipeline;
import core.scenegraph.VkMeshData;
import core.scenegraph.VkRenderInfo;
import core.util.VkUtil;
import core.wrapper.command.SecondaryDrawCmdBuffer;
import core.wrapper.pipeline.GraphicsTessellationPipeline;

public class TerrainChunk extends QuadtreeChunk{

	public TerrainChunk(Map<NodeComponentType, NodeComponent> components, QuadtreeCache quadtreeCache,
			Transform worldTransform, Vec2f location, int levelOfDetail, Vec2f index) {
		
		super(components, quadtreeCache, worldTransform, location, levelOfDetail, index);
		
		try {
			addComponent(NodeComponentType.MAIN_RENDERINFO, components.get(NodeComponentType.MAIN_RENDERINFO).clone());
			addComponent(NodeComponentType.MESH_DATA, components.get(NodeComponentType.MESH_DATA).clone());
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		LogicalDevice device = VkContext.getDeviceManager().getLogicalDevice(DeviceType.MAJOR_GRAPHICS_DEVICE);
		
		VkRenderInfo renderInfo = getComponent(NodeComponentType.MAIN_RENDERINFO);
		VkMeshData meshData = getComponent(NodeComponentType.MESH_DATA);
		
		int pushConstantsRange = Float.BYTES * 42 + Integer.BYTES * 11;
		
		ByteBuffer pushConstants = memAlloc(pushConstantsRange);
		pushConstants.put(BufferUtil.createByteBuffer(getLocalTransform().getWorldMatrix()));
		pushConstants.put(BufferUtil.createByteBuffer(getWorldTransform().getWorldMatrixRTS()));
		pushConstants.putFloat(terrainProperties.getVerticalScaling());
		pushConstants.putFloat(terrainProperties.getHorizontalScaling());
		pushConstants.putInt(lod);
		pushConstants.putFloat(gap);
		pushConstants.put(BufferUtil.createByteBuffer(location));
		pushConstants.put(BufferUtil.createByteBuffer(index));
		for (int morphArea : terrainProperties.getLod_morphing_area()){
			pushConstants.putInt(morphArea);
		}
		pushConstants.putInt(terrainProperties.getTessellationFactor());
		pushConstants.putFloat(terrainProperties.getTessellationSlope());
		pushConstants.putFloat(terrainProperties.getTessellationShift());
		pushConstants.putFloat(terrainProperties.getUvScaling());
		pushConstants.putInt(terrainProperties.getHighDetailRange());
		pushConstants.flip();
		
		VkPipeline graphicsPipeline = new GraphicsTessellationPipeline(device.getHandle(),
				renderInfo.getShaderPipeline(), renderInfo.getVertexInput(),
				VkUtil.createLongBuffer(renderInfo.getDescriptorSetLayouts()),
				BaseContext.getConfig().getX_ScreenResolution(),
				BaseContext.getConfig().getY_ScreenResolution(),
				VkContext.getResources().getOffScreenFbo().getRenderPass().getHandle(),
				VkContext.getResources().getOffScreenFbo().getColorAttachmentCount(),
				BaseContext.getConfig().getMultisamples(),
				pushConstantsRange, VK_SHADER_STAGE_ALL_GRAPHICS,
				16);
		
		CommandBuffer commandBuffer = new SecondaryDrawCmdBuffer(
	    		device.getHandle(),
	    		device.getGraphicsCommandPool(Thread.currentThread().getId()).getHandle(), 
	    		graphicsPipeline.getHandle(), graphicsPipeline.getLayoutHandle(),
	    		VkContext.getResources().getOffScreenFbo().getFrameBuffer().getHandle(),
	    		VkContext.getResources().getOffScreenFbo().getRenderPass().getHandle(),
	    		0,
	    		VkUtil.createLongArray(renderInfo.getDescriptorSets()),
	    		meshData.getVertexBufferObject().getHandle(),
	    		meshData.getVertexCount(),
	    		pushConstants, VK_SHADER_STAGE_ALL_GRAPHICS);
		
		renderInfo.setCommandBuffer(commandBuffer);
		renderInfo.setPipeline(graphicsPipeline);
	}
	
	protected void computeWorldPos(){
		
		Vec2f chunkCenter = location.add(gap/2f);
		Vec4f worldPosition = getWorldTransform().getWorldMatrixRTS().mul(
				new Vec4f(chunkCenter.getX(),0,chunkCenter.getY(),1));
		worldPosition = worldPosition.normalize();
		worldPosition = worldPosition.mul(terrainProperties.getHorizontalScaling());
		// TODO displacment
		// Vec3f displacement = TerrainHelper.getTerrainHeight(terrainProperties, loc.getX(), loc.getY());
//		System.out.println(worldPosition);
		worldPos = worldPosition.xyz();
	}

	@Override
	public QuadtreeChunk createChildChunk(Map<NodeComponentType, NodeComponent> components, QuadtreeCache quadtreeCache,
			Transform worldTransform, Vec2f location, int levelOfDetail, Vec2f index) {

		return new TerrainChunk(components, quadtreeCache, worldTransform, location, levelOfDetail, index);
	}
	
}
