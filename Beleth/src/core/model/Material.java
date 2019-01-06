package core.model;

import core.math.Vec3f;
import core.scenegraph.NodeComponent;

public class Material<T> extends NodeComponent{
	
	private String name;
	private T diffusemap;
	private T normalmap;
	private T heightmap;
	private T ambientmap;
	private T specularmap;
	private T alphamap;
	private Vec3f color;
	private float heightScaling;
	private float horizontalScaling;
	private float emission;
	private float shininess;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public T getDiffusemap() {
		return diffusemap;
	}
	public void setDiffusemap(T diffusemap) {
		this.diffusemap = diffusemap;
	}
	public T getNormalmap() {
		return normalmap;
	}
	public void setNormalmap(T normalmap) {
		this.normalmap = normalmap;
	}
	public T getHeightmap() {
		return heightmap;
	}
	public void setHeightmap(T heightmap) {
		this.heightmap = heightmap;
	}
	public T getAmbientmap() {
		return ambientmap;
	}
	public void setAmbientmap(T ambientmap) {
		this.ambientmap = ambientmap;
	}
	public T getSpecularmap() {
		return specularmap;
	}
	public void setSpecularmap(T specularmap) {
		this.specularmap = specularmap;
	}
	public T getAlphamap() {
		return alphamap;
	}
	public void setAlphamap(T alphamap) {
		this.alphamap = alphamap;
	}
	public Vec3f getColor() {
		return color;
	}
	public void setColor(Vec3f color) {
		this.color = color;
	}
	public float getHeightScaling() {
		return heightScaling;
	}
	public void setHeightScaling(float heightScaling) {
		this.heightScaling = heightScaling;
	}
	public float getHorizontalScaling() {
		return horizontalScaling;
	}
	public void setHorizontalScaling(float horizontalScaling) {
		this.horizontalScaling = horizontalScaling;
	}
	public float getEmission() {
		return emission;
	}
	public void setEmission(float emission) {
		this.emission = emission;
	}
	public float getShininess() {
		return shininess;
	}
	public void setShininess(float shininess) {
		this.shininess = shininess;
	}
	
	
}