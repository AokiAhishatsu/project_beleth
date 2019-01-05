package core.scenegraph;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;

import java.nio.ByteBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkQueue;
import core.model.Mesh;
import core.model.Vertex.VertexLayout;
import core.scenegraph.NodeComponent;
import core.util.BufferUtil;
import core.command.CommandPool;
import core.memory.VkBuffer;
import core.wrapper.buffer.VkBufferHelper;

public class VkMeshData extends NodeComponent{
	
	private VkBuffer vertexBufferObject;
	private ByteBuffer vertexBuffer;
	private int vertexCount;
	
	private VkBuffer indexBufferObject;
	private ByteBuffer indexBuffer;
	private int indexCount;
	
	public VkMeshData() {
	}
	
	public VkMeshData(VkBuffer vertexBufferObject, ByteBuffer vertexBuffer, int vertexCount, VkBuffer indexBufferObject,
			ByteBuffer indexBuffer, int indexCount) {
		super();
		this.vertexBufferObject = vertexBufferObject;
		this.vertexBuffer = vertexBuffer;
		this.vertexCount = vertexCount;
		this.indexBufferObject = indexBufferObject;
		this.indexBuffer = indexBuffer;
		this.indexCount = indexCount;
	}

	public VkMeshData(VkDevice device, VkPhysicalDeviceMemoryProperties memoryProperties,
			CommandPool commandPool, VkQueue queue, Mesh mesh, VertexLayout vertexLayout) {
		
		ByteBuffer vertexByteBuffer = BufferUtil.createByteBuffer(mesh.getVertices(), vertexLayout);
		ByteBuffer indexByteBuffer = BufferUtil.createByteBuffer(mesh.getIndices());
		vertexCount = mesh.getVertices().length;
		indexCount = mesh.getIndices().length;
		
		vertexBufferObject = VkBufferHelper.createDeviceLocalBuffer(
				device, memoryProperties,
				commandPool.getHandle(), queue,
				vertexByteBuffer, VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);

		indexBufferObject = VkBufferHelper.createDeviceLocalBuffer(
        		device, memoryProperties,
				commandPool.getHandle(), queue,
        		indexByteBuffer, VK_BUFFER_USAGE_INDEX_BUFFER_BIT);
	}
	
	public void shutdown(){

		if(vertexBufferObject != null){
			vertexBufferObject.destroy();
		}
		if(indexBufferObject != null){
			indexBufferObject.destroy();
		}
	}

	public VkBuffer getVertexBufferObject() {
		return vertexBufferObject;
	}

	public ByteBuffer getVertexBuffer() {
		return vertexBuffer;
	}

	public int getVertexCount() {
		return vertexCount;
	}

	public VkBuffer getIndexBufferObject() {
		return indexBufferObject;
	}

	public ByteBuffer getIndexBuffer() {
		return indexBuffer;
	}

	public int getIndexCount() {
		return indexCount;
	}
	
	public static VkMeshDataBuilder builder() {
		return new VkMeshDataBuilder();
	}
	
	public static class VkMeshDataBuilder {
		
		private VkBuffer vertexBufferObject;
		private ByteBuffer vertexBuffer;
		private int vertexCount;
		
		private VkBuffer indexBufferObject;
		private ByteBuffer indexBuffer;
		private int indexCount;
		
		VkMeshDataBuilder(){
		}
		
		public VkMeshDataBuilder vertexBufferObject(VkBuffer vertexBufferObject) {
			this.vertexBufferObject = vertexBufferObject;
			return this;
		}
		
		public VkMeshDataBuilder vertexBuffer(ByteBuffer vertexBuffer) {
			this.vertexBuffer = vertexBuffer;
			return this;
		}
		
		public VkMeshDataBuilder vertexCount(int vertexCount) {
			this.vertexCount = vertexCount;
			return this;
		}
		
		public VkMeshDataBuilder indexBufferObject(VkBuffer indexBufferObject) {
			this.indexBufferObject = indexBufferObject;
			return this;
		}
		
		public VkMeshDataBuilder indexBuffer(ByteBuffer indexBuffer) {
			this.indexBuffer = indexBuffer;
			return this;
		}

		public VkMeshDataBuilder indexCount(int indexCount) {
			this.indexCount = indexCount;
			return this;
		}
		
		public VkMeshData build() {
		      return new VkMeshData(vertexBufferObject, vertexBuffer, vertexCount, indexBufferObject, indexBuffer, indexCount);
		}

	}
}
