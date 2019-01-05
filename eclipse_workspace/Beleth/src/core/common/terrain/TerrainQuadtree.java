package core.common.terrain;

import java.util.Map;

import core.common.quadtree.Quadtree;
import core.math.Transform;
import core.math.Vec2f;
import core.scenegraph.NodeComponent;
import core.scenegraph.NodeComponentType;

public abstract class TerrainQuadtree extends Quadtree{

	public TerrainQuadtree(Map<NodeComponentType, NodeComponent> components,
			int rootChunkCount, float horizontalScaling) {
	
		super();
		
		Transform worldTransformFace0 = new Transform();
		worldTransformFace0.setTranslation(-0.5f * horizontalScaling,
				0, -0.5f * horizontalScaling);
		worldTransformFace0.setScaling(horizontalScaling);

		for (int i=0; i<rootChunkCount; i++){
			for (int j=0; j<rootChunkCount; j++){
				addChild(createChildChunk(components, quadtreeCache, worldTransformFace0,
						new Vec2f(1f * i/(float)rootChunkCount,1f * j/(float)rootChunkCount),
						0, new Vec2f(i,j)));
			}
		}
	}

}
