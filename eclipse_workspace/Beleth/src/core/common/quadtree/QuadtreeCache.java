package core.common.quadtree;

import java.util.HashMap;

import core.scenegraph.Node;

public class QuadtreeCache {

	private HashMap<String, QuadtreeChunk> chunks;
	
	public QuadtreeCache() {
		
		chunks = new HashMap<String, QuadtreeChunk>();
	}
	
	public boolean contains(String key){
		
		return chunks.containsKey(key);
	}
	
	public void addChunk(Node chunk){
		
		chunks.put(((QuadtreeChunk) chunk).getQuadtreeCacheKey(), (QuadtreeChunk) chunk);
	}
	
	public void addChunk(QuadtreeChunk chunk){
	
		chunks.put(chunk.getQuadtreeCacheKey(), chunk);
	}
	
	public QuadtreeChunk getChunk(String key){
		
		return chunks.get(key);
	}
	
	public void removeChunk(String key){
		
		chunks.remove(key);
	}
	
	public QuadtreeChunk getAndRemoveChunk(String key){
		
		QuadtreeChunk chunk = chunks.get(key);
		chunks.remove(key);
		return chunk;
	}

	public HashMap<String, QuadtreeChunk> getChunks() {
		return chunks;
	}
	
}
