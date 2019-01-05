package core.context;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import core.math.Vec4f;
import core.util.Constants;

public class Configuration {
	
	// screen settings
	private int x_ScreenResolution;
	private int y_ScreenResolution;

	// window settings
	private String displayTitle;
	private int windowWidth;
	private int windowHeight;
	
	// anitaliasing
	private final int multisamples;
	private boolean fxaaEnabled;
	
	// static render settings
	private float sightRange;
	// post processing effects
	private boolean ssaoEnabled;
	private boolean bloomEnabled;
	private boolean depthOfFieldBlurEnabled;
	private boolean motionBlurEnabled;
	private boolean lightScatteringEnabled;
	private boolean lensFlareEnabled;
	
	// dynamic render settings
	private boolean renderWireframe;
	private boolean renderUnderwater;
	private boolean renderReflection;
	private boolean renderRefraction;
	private Vec4f clipplane;
	
	private final Properties properties;
	
	public Configuration(){
		
		properties = new Properties();
		try {
			InputStream input = new FileInputStream("src/res/engine-config.properties");
			properties.load(input);
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		windowWidth = Integer.valueOf(properties.getProperty("display.width"));
		windowHeight = Integer.valueOf(properties.getProperty("display.height"));
		displayTitle = properties.getProperty("display.title");
		x_ScreenResolution = Integer.valueOf(properties.getProperty("screen.resolution.x"));
		y_ScreenResolution = Integer.valueOf(properties.getProperty("screen.resolution.y"));
		multisamples = Integer.valueOf(properties.getProperty("multisamples"));
		fxaaEnabled = Integer.valueOf(properties.getProperty("fxaa.enable")) == 1 ? true : false;
		sightRange = Float.valueOf(properties.getProperty("sightRange"));
		
		bloomEnabled = Integer.valueOf(properties.getProperty("bloom.enable")) == 1 ? true : false;
		ssaoEnabled = Integer.valueOf(properties.getProperty("ssao.enable")) == 1 ? true : false;
		motionBlurEnabled = Integer.valueOf(properties.getProperty("motionBlur.enable")) == 1 ? true : false;
		lightScatteringEnabled = Integer.valueOf(properties.getProperty("lightScattering.enable")) == 1 ? true : false;
		depthOfFieldBlurEnabled = Integer.valueOf(properties.getProperty("depthOfFieldBlur.enable")) == 1 ? true : false;
		lensFlareEnabled = Integer.valueOf(properties.getProperty("lensFlare.enable")) == 1 ? true : false;
		
		renderWireframe = false;
		renderUnderwater = false;
		renderReflection = false;
		renderRefraction = false;
		clipplane = Constants.ZEROPLANE;
		
	}

	public int getX_ScreenResolution() {
		return x_ScreenResolution;
	}

	public void setX_ScreenResolution(int x_ScreenResolution) {
		this.x_ScreenResolution = x_ScreenResolution;
	}

	public int getY_ScreenResolution() {
		return y_ScreenResolution;
	}

	public void setY_ScreenResolution(int y_ScreenResolution) {
		this.y_ScreenResolution = y_ScreenResolution;
	}

	public String getDisplayTitle() {
		return displayTitle;
	}

	public void setDisplayTitle(String displayTitle) {
		this.displayTitle = displayTitle;
	}

	public int getWindowWidth() {
		return windowWidth;
	}

	public void setWindowWidth(int windowWidth) {
		this.windowWidth = windowWidth;
	}

	public int getWindowHeight() {
		return windowHeight;
	}

	public void setWindowHeight(int windowHeight) {
		this.windowHeight = windowHeight;
	}

	public boolean isFxaaEnabled() {
		return fxaaEnabled;
	}

	public void setFxaaEnabled(boolean fxaaEnabled) {
		this.fxaaEnabled = fxaaEnabled;
	}

	public float getSightRange() {
		return sightRange;
	}

	public void setSightRange(float sightRange) {
		this.sightRange = sightRange;
	}

	public boolean isSsaoEnabled() {
		return ssaoEnabled;
	}

	public void setSsaoEnabled(boolean ssaoEnabled) {
		this.ssaoEnabled = ssaoEnabled;
	}

	public boolean isBloomEnabled() {
		return bloomEnabled;
	}

	public void setBloomEnabled(boolean bloomEnabled) {
		this.bloomEnabled = bloomEnabled;
	}

	public boolean isDepthOfFieldBlurEnabled() {
		return depthOfFieldBlurEnabled;
	}

	public void setDepthOfFieldBlurEnabled(boolean depthOfFieldBlurEnabled) {
		this.depthOfFieldBlurEnabled = depthOfFieldBlurEnabled;
	}

	public boolean isMotionBlurEnabled() {
		return motionBlurEnabled;
	}

	public void setMotionBlurEnabled(boolean motionBlurEnabled) {
		this.motionBlurEnabled = motionBlurEnabled;
	}

	public boolean isLightScatteringEnabled() {
		return lightScatteringEnabled;
	}

	public void setLightScatteringEnabled(boolean lightScatteringEnabled) {
		this.lightScatteringEnabled = lightScatteringEnabled;
	}

	public boolean isLensFlareEnabled() {
		return lensFlareEnabled;
	}

	public void setLensFlareEnabled(boolean lensFlareEnabled) {
		this.lensFlareEnabled = lensFlareEnabled;
	}

	public boolean isRenderWireframe() {
		return renderWireframe;
	}

	public void setRenderWireframe(boolean renderWireframe) {
		this.renderWireframe = renderWireframe;
	}

	public boolean isRenderUnderwater() {
		return renderUnderwater;
	}

	public void setRenderUnderwater(boolean renderUnderwater) {
		this.renderUnderwater = renderUnderwater;
	}

	public boolean isRenderReflection() {
		return renderReflection;
	}

	public void setRenderReflection(boolean renderReflection) {
		this.renderReflection = renderReflection;
	}

	public boolean isRenderRefraction() {
		return renderRefraction;
	}

	public void setRenderRefraction(boolean renderRefraction) {
		this.renderRefraction = renderRefraction;
	}

	public Vec4f getClipplane() {
		return clipplane;
	}

	public void setClipplane(Vec4f clipplane) {
		this.clipplane = clipplane;
	}

	public int getMultisamples() {
		return multisamples;
	}

	public Properties getProperties() {
		return properties;
	}
	
}
