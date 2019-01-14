package core.context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import core.math.Vec4f;
import core.util.Constants;

public class Configuration {
	
	private static Configuration instance;
	
	private boolean isValidated;
	
	// screen settings
	private int x_ScreenResolution;
	private int y_ScreenResolution;

	// window settings
	private String displayTitle;
	private int windowWidth;
	private int windowHeight;
	
	// anitaliasing
	private int multisamples;
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
	
	private final String filePath = "src/res/engine-config.properties";
	
	private Configuration(){
		
		properties = new Properties();
		try {
			InputStream input = new FileInputStream(filePath);
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
		
		isValidated = Integer.valueOf(properties.getProperty("validation.enable")) == 1 ? true : false;
		
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
	
	public static Configuration getInstance () {
		if (Configuration.instance == null)
			Configuration.instance = new Configuration ();
		return Configuration.instance;
	}
	
	public void saveParamChanges() {
		try {
			OutputStream out = new FileOutputStream(filePath);
			properties.store(out, "Beleth engine-config");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getX_ScreenResolution() {
		return x_ScreenResolution;
	}

	public void setX_ScreenResolution(int x_ScreenResolution) {
		this.x_ScreenResolution = x_ScreenResolution;
		this.properties.setProperty("screen.resolution.x", String.valueOf(x_ScreenResolution));
	}

	public int getY_ScreenResolution() {
		return y_ScreenResolution;
	}

	public void setY_ScreenResolution(int y_ScreenResolution) {
		this.y_ScreenResolution = y_ScreenResolution;
		this.properties.setProperty("screen.resolution.y", String.valueOf(y_ScreenResolution));
	}

	public String getDisplayTitle() {
		return displayTitle;
	}

	public void setDisplayTitle(String displayTitle) {
		this.displayTitle = displayTitle;
		this.properties.setProperty("display.title", displayTitle);
	}

	public int getWindowWidth() {
		return windowWidth;
	}

	public void setWindowWidth(int windowWidth) {
		this.windowWidth = windowWidth;
		this.properties.setProperty("display.width", String.valueOf(windowWidth));
	}

	public int getWindowHeight() {
		return windowHeight;
	}

	public void setWindowHeight(int windowHeight) {
		this.windowHeight = windowHeight;
		this.properties.setProperty("display.height", String.valueOf(windowHeight));
	}

	public boolean getValidation() {
		return isValidated;
	}

	public void setValidation(boolean validation) {
		this.isValidated = validation;
		this.properties.setProperty("validation.enable", String.valueOf(validation));
	}

	public boolean isFxaaEnabled() {
		return fxaaEnabled;
	}

	public void setFxaaEnabled(boolean fxaaEnabled) {
		this.fxaaEnabled = fxaaEnabled;
		this.properties.setProperty("fxaa.enable", String.valueOf(fxaaEnabled));
	}

	public float getSightRange() {
		return sightRange;
	}

	public void setSightRange(float sightRange) {
		this.sightRange = sightRange;
		this.properties.setProperty("sightRange", String.valueOf(sightRange));
	}

	public boolean isSsaoEnabled() {
		return ssaoEnabled;
	}

	public void setSsaoEnabled(boolean ssaoEnabled) {
		this.ssaoEnabled = ssaoEnabled;
		this.properties.setProperty("ssao.enable", String.valueOf(ssaoEnabled));
	}

	public boolean isBloomEnabled() {
		return bloomEnabled;
	}

	public void setBloomEnabled(boolean bloomEnabled) {
		this.bloomEnabled = bloomEnabled;
		this.properties.setProperty("bloom.enable", String.valueOf(bloomEnabled));
	}

	public boolean isDepthOfFieldBlurEnabled() {
		return depthOfFieldBlurEnabled;
	}

	public void setDepthOfFieldBlurEnabled(boolean depthOfFieldBlurEnabled) {
		this.depthOfFieldBlurEnabled = depthOfFieldBlurEnabled;
		this.properties.setProperty("depthOfFieldBlur.enable", String.valueOf(depthOfFieldBlurEnabled));
	}

	public boolean isMotionBlurEnabled() {
		return motionBlurEnabled;
	}

	public void setMotionBlurEnabled(boolean motionBlurEnabled) {
		this.motionBlurEnabled = motionBlurEnabled;
		this.properties.setProperty("motionBlur.enable", String.valueOf(motionBlurEnabled));
	}

	public boolean isLightScatteringEnabled() {
		return lightScatteringEnabled;
	}

	public void setLightScatteringEnabled(boolean lightScatteringEnabled) {
		this.lightScatteringEnabled = lightScatteringEnabled;
		this.properties.setProperty("lightScattering.enable", String.valueOf(lightScatteringEnabled));
	}

	public boolean isLensFlareEnabled() {
		return lensFlareEnabled;
	}

	public void setLensFlareEnabled(boolean lensFlareEnabled) {
		this.lensFlareEnabled = lensFlareEnabled;
		this.properties.setProperty("lensFlare.enable", String.valueOf(lensFlareEnabled));
	}

	public boolean isRenderWireframe() {
		return renderWireframe;
	}

	public void setRenderWireframe(boolean renderWireframe) {
		this.renderWireframe = renderWireframe;
		// Not in file yet.
	}

	public boolean isRenderUnderwater() {
		return renderUnderwater;
	}

	public void setRenderUnderwater(boolean renderUnderwater) {
		this.renderUnderwater = renderUnderwater;
		// Not in file yet.
	}

	public boolean isRenderReflection() {
		return renderReflection;
	}

	public void setRenderReflection(boolean renderReflection) {
		this.renderReflection = renderReflection;
		// Not in file yet.
	}

	public boolean isRenderRefraction() {
		return renderRefraction;
	}

	public void setRenderRefraction(boolean renderRefraction) {
		this.renderRefraction = renderRefraction;
		// Not in file yet.
	}

	public Vec4f getClipplane() {
		return clipplane;
	}

	public void setClipplane(Vec4f clipplane) {
		this.clipplane = clipplane;
		// Not in file yet.
	}

	public int getMultisamples() {
		return multisamples;
	}
	
	public void setMultisamples(int multisamples) {
		this.multisamples = multisamples;
		this.properties.setProperty("multisamples", String.valueOf(multisamples));
	}
}
