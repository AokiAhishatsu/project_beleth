package core.scenegraph;

import java.util.List;

import core.scenegraph.NodeComponent;
import core.command.CommandBuffer;
import core.descriptor.DescriptorSet;
import core.descriptor.DescriptorSetLayout;
import core.pipeline.ShaderPipeline;
import core.pipeline.VkPipeline;
import core.pipeline.VkVertexInput;

public class VkRenderInfo extends NodeComponent{

	private VkPipeline pipeline;
	private CommandBuffer commandBuffer;
	private VkVertexInput vertexInput;
	private ShaderPipeline shaderPipeline;
	private List<DescriptorSetLayout> descriptorSetLayouts;
	private List<DescriptorSet> descriptorSets;

	public VkRenderInfo(VkPipeline pipeline, CommandBuffer commandBuffer, VkVertexInput vertexInput,
			ShaderPipeline shaderPipeline, List<DescriptorSetLayout> descriptorSetLayouts,
			List<DescriptorSet> descriptorSets) {
		super();
		this.pipeline = pipeline;
		this.commandBuffer = commandBuffer;
		this.vertexInput = vertexInput;
		this.shaderPipeline = shaderPipeline;
		this.descriptorSetLayouts = descriptorSetLayouts;
		this.descriptorSets = descriptorSets;
	}

	public void shutdown(){
		
		if(pipeline != null){
			pipeline.destroy();
		}
		if (commandBuffer != null){
			commandBuffer.destroy();
		}
		if(shaderPipeline != null){
			shaderPipeline.destroy();
		}
		if(descriptorSetLayouts != null){
			for (DescriptorSetLayout layout : descriptorSetLayouts){
				if (layout.getHandle() != -1)
					layout.destroy();
			}
		}
		if(descriptorSets != null){
			for (DescriptorSet set : descriptorSets){
				if (set.getHandle() != -1)
					set.destroy();
			}
		}
	}

	public VkPipeline getPipeline() {
		return pipeline;
	}

	public void setPipeline(VkPipeline pipeline) {
		this.pipeline = pipeline;
	}

	public CommandBuffer getCommandBuffer() {
		return commandBuffer;
	}

	public void setCommandBuffer(CommandBuffer commandBuffer) {
		this.commandBuffer = commandBuffer;
	}

	public VkVertexInput getVertexInput() {
		return vertexInput;
	}

	public void setVertexInput(VkVertexInput vertexInput) {
		this.vertexInput = vertexInput;
	}

	public ShaderPipeline getShaderPipeline() {
		return shaderPipeline;
	}

	public void setShaderPipeline(ShaderPipeline shaderPipeline) {
		this.shaderPipeline = shaderPipeline;
	}

	public List<DescriptorSetLayout> getDescriptorSetLayouts() {
		return descriptorSetLayouts;
	}

	public void setDescriptorSetLayouts(List<DescriptorSetLayout> descriptorSetLayouts) {
		this.descriptorSetLayouts = descriptorSetLayouts;
	}

	public List<DescriptorSet> getDescriptorSets() {
		return descriptorSets;
	}

	public void setDescriptorSets(List<DescriptorSet> descriptorSets) {
		this.descriptorSets = descriptorSets;
	}
	
	public static VkRenderInfoBuilder builder() {
		return new VkRenderInfoBuilder();
	}
	
	public static class VkRenderInfoBuilder{
		private VkPipeline pipeline;
		private CommandBuffer commandBuffer;
		private VkVertexInput vertexInput;
		private ShaderPipeline shaderPipeline;
		private List<DescriptorSetLayout> descriptorSetLayouts;
		private List<DescriptorSet> descriptorSets;
		
		VkRenderInfoBuilder(){
		}
		
		public VkRenderInfoBuilder pipeline(VkPipeline pipeline) {
		      this.pipeline = pipeline;
		      return this;
		}
		public VkRenderInfoBuilder commandBuffer(CommandBuffer commandBuffer) {
		      this.commandBuffer = commandBuffer;
		      return this;
		}
		public VkRenderInfoBuilder vertexInput(VkVertexInput vertexInput) {
		      this.vertexInput = vertexInput;
		      return this;
		}
		public VkRenderInfoBuilder shaderPipeline(ShaderPipeline shaderPipeline) {
		      this.shaderPipeline = shaderPipeline;
		      return this;
		}
		public VkRenderInfoBuilder descriptorSetLayouts(List<DescriptorSetLayout> descriptorSetLayouts) {
		      this.descriptorSetLayouts = descriptorSetLayouts;
		      return this;
		}
		public VkRenderInfoBuilder descriptorSets(List<DescriptorSet> descriptorSets) {
		      this.descriptorSets = descriptorSets;
		      return this;
		}
		
		public VkRenderInfo build() {
		      return new VkRenderInfo(pipeline, commandBuffer, vertexInput,
		  			shaderPipeline, descriptorSetLayouts, descriptorSets);
		}
		
	}
}
