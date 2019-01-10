package core.common.water;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import core.math.Vec2f;


public class WaterConfiguration {

	private int N;
	private int L;
	private float amplitude;
	private Vec2f windDirection;
	private float windSpeed;
	private float capillarWavesSupression;
	private float motion;
	private float displacementScale;
	private float choppiness;
	private int tessellationFactor;
	private float tessellationShift;
	private float tessellationSlope;
	private int highDetailRange;
	private int uvScale;
	private float specular;
	private float emission;
	private float kReflection;
	private float kRefraction;
	private float distortion;
	private float waveMotion;
	private float normalStrength;
	private float t_delta;
	private boolean choppy;
	
	public void loadFile(String file)
	{
		Properties properties = new Properties();
		try {
			InputStream stream = WaterConfiguration.class.getClassLoader().getResourceAsStream(file);
			properties.load(stream);
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		N = Integer.valueOf(properties.getProperty("fft.resolution"));
		L = Integer.valueOf(properties.getProperty("fft.L"));
		amplitude = Float.valueOf(properties.getProperty("fft.amplitude"));
		windDirection = new Vec2f(Float.valueOf(properties.getProperty("wind.x")),
				Float.valueOf(properties.getProperty("wind.y"))).normalize();
		windSpeed = Float.valueOf(properties.getProperty("wind.speed"));
		capillarWavesSupression = Float.valueOf(properties.getProperty("fft.capillarwavesSuppression"));
		displacementScale = Float.valueOf(properties.getProperty("displacementScale"));
		choppiness = Float.valueOf(properties.getProperty("choppiness"));
		distortion = Float.valueOf(properties.getProperty("distortion"));
		waveMotion = Float.valueOf(properties.getProperty("wavemotion"));
		uvScale = Integer.valueOf(properties.getProperty("uvScale"));
		tessellationFactor = Integer.valueOf(properties.getProperty("tessellationFactor"));
		tessellationSlope = Float.valueOf(properties.getProperty("tessellationSlope"));
		tessellationShift = Float.valueOf(properties.getProperty("tessellationShift"));
		specular = Float.valueOf(properties.getProperty("specular"));
		emission = Float.valueOf(properties.getProperty("emission"));
		kReflection = Float.valueOf(properties.getProperty("kReflection"));
		kRefraction = Float.valueOf(properties.getProperty("kRefraction"));
		normalStrength = Float.valueOf(properties.getProperty("normalStrength"));
		highDetailRange = Integer.valueOf(properties.getProperty("highDetailRange"));
		t_delta = Float.valueOf(properties.getProperty("t_delta"));
		choppy = Boolean.valueOf(properties.getProperty("choppy"));
	}

	public int getN() {
		return N;
	}

	public void setN(int n) {
		N = n;
	}

	public int getL() {
		return L;
	}

	public void setL(int l) {
		L = l;
	}

	public float getAmplitude() {
		return amplitude;
	}

	public void setAmplitude(float amplitude) {
		this.amplitude = amplitude;
	}

	public Vec2f getWindDirection() {
		return windDirection;
	}

	public void setWindDirection(Vec2f windDirection) {
		this.windDirection = windDirection;
	}

	public float getWindSpeed() {
		return windSpeed;
	}

	public void setWindSpeed(float windSpeed) {
		this.windSpeed = windSpeed;
	}

	public float getCapillarWavesSupression() {
		return capillarWavesSupression;
	}

	public void setCapillarWavesSupression(float capillarWavesSupression) {
		this.capillarWavesSupression = capillarWavesSupression;
	}

	public float getMotion() {
		return motion;
	}

	public void setMotion(float motion) {
		this.motion = motion;
	}

	public float getDisplacementScale() {
		return displacementScale;
	}

	public void setDisplacementScale(float displacementScale) {
		this.displacementScale = displacementScale;
	}

	public float getChoppiness() {
		return choppiness;
	}

	public void setChoppiness(float choppiness) {
		this.choppiness = choppiness;
	}

	public int getTessellationFactor() {
		return tessellationFactor;
	}

	public void setTessellationFactor(int tessellationFactor) {
		this.tessellationFactor = tessellationFactor;
	}

	public float getTessellationShift() {
		return tessellationShift;
	}

	public void setTessellationShift(float tessellationShift) {
		this.tessellationShift = tessellationShift;
	}

	public float getTessellationSlope() {
		return tessellationSlope;
	}

	public void setTessellationSlope(float tessellationSlope) {
		this.tessellationSlope = tessellationSlope;
	}

	public int getHighDetailRange() {
		return highDetailRange;
	}

	public void setHighDetailRange(int highDetailRange) {
		this.highDetailRange = highDetailRange;
	}

	public int getUvScale() {
		return uvScale;
	}

	public void setUvScale(int uvScale) {
		this.uvScale = uvScale;
	}

	public float getSpecular() {
		return specular;
	}

	public void setSpecular(float specular) {
		this.specular = specular;
	}

	public float getEmission() {
		return emission;
	}

	public void setEmission(float emission) {
		this.emission = emission;
	}

	public float getKReflection() {
		return kReflection;
	}

	public void setKReflection(float kReflection) {
		this.kReflection = kReflection;
	}

	public float getKRefraction() {
		return kRefraction;
	}

	public void setKRefraction(float kRefraction) {
		this.kRefraction = kRefraction;
	}

	public float getDistortion() {
		return distortion;
	}

	public void setDistortion(float distortion) {
		this.distortion = distortion;
	}

	public float getWaveMotion() {
		return waveMotion;
	}

	public void setWaveMotion(float waveMotion) {
		this.waveMotion = waveMotion;
	}

	public float getNormalStrength() {
		return normalStrength;
	}

	public void setNormalStrength(float normalStrength) {
		this.normalStrength = normalStrength;
	}

	public float getT_delta() {
		return t_delta;
	}

	public void setT_delta(float t_delta) {
		this.t_delta = t_delta;
	}

	public boolean isChoppy() {
		return choppy;
	}

	public void setChoppy(boolean choppy) {
		this.choppy = choppy;
	}
	
}
