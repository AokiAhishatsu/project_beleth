package core.context;

import core.CoreEngine;
import core.RenderEngine;
import core.platform.GLFWInput;
import core.platform.Window;
import core.scenegraph.Camera;

public abstract class BaseContext {

	protected static Configuration config;
	protected static GLFWInput input;
	protected static Camera camera;
	protected static Window window;
	protected static CoreEngine coreEngine;
	protected static RenderEngine renderEngine;
	
	public static void init() {
		
		config = Configuration.getInstance();
		input = new GLFWInput();
		coreEngine = new CoreEngine();
	}

	public static RenderEngine getRenderEngine() {
		return renderEngine;
	}

	public static void setRenderEngine(RenderEngine renderEngine) {
		BaseContext.renderEngine = renderEngine;
	}

	public static Configuration getConfig() {
		return config;
	}

	public static GLFWInput getInput() {
		return input;
	}

	public static Camera getCamera() {
		return camera;
	}

	public static Window getWindow() {
		return window;
	}

	public static CoreEngine getCoreEngine() {
		return coreEngine;
	}
	
}
