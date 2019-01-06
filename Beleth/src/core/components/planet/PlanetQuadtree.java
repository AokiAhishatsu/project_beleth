package core.components.planet;

import java.util.Map;

import core.common.planet.SphericalCubeQuadtree;
import core.common.quadtree.QuadtreeCache;
import core.common.quadtree.QuadtreeChunk;
import core.math.Transform;
import core.math.Vec2f;
import core.scenegraph.NodeComponent;
import core.scenegraph.NodeComponentType;
import core.components.terrain.TerrainChunk;

public class PlanetQuadtree extends SphericalCubeQuadtree{

	public PlanetQuadtree(Map<NodeComponentType, NodeComponent> components,
			int rootChunkCount, float horizontalScaling) {
		
		super(components, rootChunkCount, horizontalScaling);
	}

	@Override
	public QuadtreeChunk createChildChunk(Map<NodeComponentType, NodeComponent> components, QuadtreeCache quadtreeCache,
			Transform worldTransform, Vec2f location, int levelOfDetail, Vec2f index) {

		return new TerrainChunk(components, quadtreeCache, worldTransform, location, levelOfDetail, index);
	}

}
