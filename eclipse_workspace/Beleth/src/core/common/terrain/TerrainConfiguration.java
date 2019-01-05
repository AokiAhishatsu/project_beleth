package core.common.terrain;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import core.image.Image;
import core.model.Material;
import core.scenegraph.NodeComponent;

public class TerrainConfiguration extends NodeComponent{

	private float verticalScaling;
	private float horizontalScaling;
	private int rootChunkCount;
	private int waterReflectionShift;
	private float uvScaling;
	private int tessellationFactor;
	private float tessellationSlope;
	private float tessellationShift;
	private int highDetailRange;
	private Image heightmap;
	private Image normalmap;
	private Image ambientmap;
	private Image splatmap;
	private FloatBuffer heightmapDataBuffer;
	private List<Material<Image>> materials = new ArrayList<>();
	private int fractalMapResolution;
	private int lodCount;
	private int[] lod_range = new int[8];
	private int[] lod_morphing_area = new int[8];
	
//	private List<FractalMap> fractals = new ArrayList<>();
	
	public TerrainConfiguration() {
		
		Properties properties = new Properties();
		try {
			InputStream stream = TerrainConfiguration.class.getClassLoader()
					.getResourceAsStream("terrain-config.properties");
			properties.load(stream);
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		verticalScaling = Float.valueOf(properties.getProperty("verticalScaling"));
		horizontalScaling = Float.valueOf(properties.getProperty("horizontalScaling"));
		rootChunkCount = Integer.valueOf(properties.getProperty("rootChunkCount"));
		tessellationFactor = Integer.valueOf(properties.getProperty("tessellationFactor"));
		tessellationSlope = Float.valueOf(properties.getProperty("tessellationSlope"));
		tessellationShift = Float.valueOf(properties.getProperty("tessellationShift"));
		uvScaling = Float.valueOf(properties.getProperty("uvScaling"));
		highDetailRange = Integer.valueOf(properties.getProperty("highDetailRange"));
		lodCount = Integer.valueOf(properties.getProperty("lod.count"));
		
		for (int i=0; i<lodCount; i++){
			
			if (Integer.valueOf(properties.getProperty("lodRanges.lod" + i)) == 0){
				lod_range[i] = 0;
				lod_morphing_area[i] = 0;
			}
			else {
				setLodRange(i, Integer.valueOf(properties.getProperty("lodRanges.lod" + i)));
			}
		}
	}

	public void setLodRange(int index, int lod_range) {
		this.lod_range[index] = lod_range;
		lod_morphing_area[index] = lod_range - getMorphingArea4Lod(index+1);
	}
	
	private int getMorphingArea4Lod(int lod){
		return (int) ((horizontalScaling/rootChunkCount) / (Math.pow(2, lod)));
	}

	public float getVerticalScaling() {
		return verticalScaling;
	}

	public void setVerticalScaling(float verticalScaling) {
		this.verticalScaling = verticalScaling;
	}

	public float getHorizontalScaling() {
		return horizontalScaling;
	}

	public void setHorizontalScaling(float horizontalScaling) {
		this.horizontalScaling = horizontalScaling;
	}

	public int getRootChunkCount() {
		return rootChunkCount;
	}

	public void setRootChunkCount(int rootChunkCount) {
		this.rootChunkCount = rootChunkCount;
	}

	public int getWaterReflectionShift() {
		return waterReflectionShift;
	}

	public void setWaterReflectionShift(int waterReflectionShift) {
		this.waterReflectionShift = waterReflectionShift;
	}

	public float getUvScaling() {
		return uvScaling;
	}

	public void setUvScaling(float uvScaling) {
		this.uvScaling = uvScaling;
	}

	public int getTessellationFactor() {
		return tessellationFactor;
	}

	public void setTessellationFactor(int tessellationFactor) {
		this.tessellationFactor = tessellationFactor;
	}

	public float getTessellationSlope() {
		return tessellationSlope;
	}

	public void setTessellationSlope(float tessellationSlope) {
		this.tessellationSlope = tessellationSlope;
	}

	public float getTessellationShift() {
		return tessellationShift;
	}

	public void setTessellationShift(float tessellationShift) {
		this.tessellationShift = tessellationShift;
	}

	public int getHighDetailRange() {
		return highDetailRange;
	}

	public void setHighDetailRange(int highDetailRange) {
		this.highDetailRange = highDetailRange;
	}

	public Image getHeightmap() {
		return heightmap;
	}

	public void setHeightmap(Image heightmap) {
		this.heightmap = heightmap;
	}

	public Image getNormalmap() {
		return normalmap;
	}

	public void setNormalmap(Image normalmap) {
		this.normalmap = normalmap;
	}

	public Image getAmbientmap() {
		return ambientmap;
	}

	public void setAmbientmap(Image ambientmap) {
		this.ambientmap = ambientmap;
	}

	public Image getSplatmap() {
		return splatmap;
	}

	public void setSplatmap(Image splatmap) {
		this.splatmap = splatmap;
	}

	public FloatBuffer getHeightmapDataBuffer() {
		return heightmapDataBuffer;
	}

	public void setHeightmapDataBuffer(FloatBuffer heightmapDataBuffer) {
		this.heightmapDataBuffer = heightmapDataBuffer;
	}

	public List<Material<Image>> getMaterials() {
		return materials;
	}

	public void setMaterials(List<Material<Image>> materials) {
		this.materials = materials;
	}

	public int getFractalMapResolution() {
		return fractalMapResolution;
	}

	public void setFractalMapResolution(int fractalMapResolution) {
		this.fractalMapResolution = fractalMapResolution;
	}

	public int getLodCount() {
		return lodCount;
	}

	public void setLodCount(int lodCount) {
		this.lodCount = lodCount;
	}

	public int[] getLod_range() {
		return lod_range;
	}

	public void setLod_range(int[] lod_range) {
		this.lod_range = lod_range;
	}

	public int[] getLod_morphing_area() {
		return lod_morphing_area;
	}

	public void setLod_morphing_area(int[] lod_morphing_area) {
		this.lod_morphing_area = lod_morphing_area;
	}
	
}
