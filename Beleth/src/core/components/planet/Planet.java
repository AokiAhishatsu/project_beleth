package core.components.planet;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import core.common.quadtree.Quadtree;
import core.common.terrain.TerrainConfiguration;
import core.math.Vec2f;
import core.model.Vertex.VertexLayout;
import core.scenegraph.Node;
import core.scenegraph.NodeComponent;
import core.scenegraph.NodeComponentType;
import core.util.BufferUtil;
import core.util.MeshGenerator;
import core.context.DeviceManager.DeviceType;
import core.context.VkContext;
import core.descriptor.DescriptorSet;
import core.descriptor.DescriptorSetLayout;
import core.device.LogicalDevice;
import core.memory.VkBuffer;
import core.pipeline.ShaderPipeline;
import core.pipeline.VkVertexInput;
import core.scenegraph.VkMeshData;
import core.scenegraph.VkRenderInfo;
import core.wrapper.buffer.VkBufferHelper;

public class Planet extends Node{
	
	private Quadtree quadtree;
	
	public Planet() {
		
		LogicalDevice device = VkContext.getDeviceManager().getLogicalDevice(DeviceType.MAJOR_GRAPHICS_DEVICE);
		VkPhysicalDeviceMemoryProperties memoryProperties = VkContext.getDeviceManager().getPhysicalDevice(DeviceType.MAJOR_GRAPHICS_DEVICE).getMemoryProperties();
		
		Vec2f[] mesh = MeshGenerator.TerrainChunkMesh();
		ByteBuffer vertexBuffer = BufferUtil.createByteBuffer(mesh);
		VkBuffer vertexBufferObject = VkBufferHelper.createDeviceLocalBuffer(
				device.getHandle(), memoryProperties,
				device.getTransferCommandPool(Thread.currentThread().getId()).getHandle(),
				device.getTransferQueue(),
				vertexBuffer, VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		
		VkMeshData meshData = VkMeshData.builder().vertexBufferObject(vertexBufferObject)
				.vertexBuffer(vertexBuffer).vertexCount(mesh.length).build();

		HashMap<NodeComponentType, NodeComponent> components = new HashMap<NodeComponentType, NodeComponent>();
		
		TerrainConfiguration config = new TerrainConfiguration();
		
		VkVertexInput vertexInput = new VkVertexInput(VertexLayout.POS2D);
		
		ShaderPipeline shaderPipeline = new ShaderPipeline(device.getHandle());
	    shaderPipeline.createVertexShader("res/shaders/planet/planet.vert.spv");
	    shaderPipeline.createTessellationControlShader("res/shaders/planet/planet.tesc.spv");
	    shaderPipeline.createTessellationEvaluationShader("res/shaders/planet/planet.tese.spv");
	    shaderPipeline.createGeometryShader("res/shaders/planet/planetWireframe.geom.spv");
	    shaderPipeline.createFragmentShader("res/shaders/planet/planet.frag.spv");
	    shaderPipeline.createShaderPipeline();
	    
	    List<DescriptorSet> descriptorSets = new ArrayList<DescriptorSet>();
		List<DescriptorSetLayout> descriptorSetLayouts = new ArrayList<DescriptorSetLayout>();
	    descriptorSets.add(VkContext.getCamera().getDescriptorSet());
		descriptorSetLayouts.add(VkContext.getCamera().getDescriptorSetLayout());
	    
	    VkRenderInfo renderInfo = VkRenderInfo.builder().vertexInput(vertexInput)
	    		.shaderPipeline(shaderPipeline).descriptorSets(descriptorSets)
	    		.descriptorSetLayouts(descriptorSetLayouts).build();
		
		components.put(NodeComponentType.CONFIGURATION, config);
		components.put(NodeComponentType.MAIN_RENDERINFO, renderInfo);
		components.put(NodeComponentType.MESH_DATA, meshData);
		
		PlanetQuadtree planetQuadtree = new PlanetQuadtree(components, config.getRootChunkCount(), config.getHorizontalScaling());
		
		quadtree = planetQuadtree;
		addChild(planetQuadtree);
		
		planetQuadtree.start();
	}
	
	public void render(){
		return;
	}

	public Quadtree getQuadtree() {
		return quadtree;
	}
	
}
