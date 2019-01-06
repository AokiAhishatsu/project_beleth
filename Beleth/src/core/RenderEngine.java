package core;

import core.context.BaseContext;
import core.context.Configuration;
import core.scenegraph.Camera;
import core.scenegraph.Scenegraph;

public abstract class RenderEngine {

	protected Scenegraph sceneGraph;
	protected Configuration config;
	protected Camera camera;
	
	public void init()
	{
		sceneGraph = new Scenegraph();
		config = BaseContext.getConfig();
		camera = BaseContext.getCamera();
		camera.init();
	}
	
	public abstract void render();
	
	public void update()
	{
		sceneGraph.update();
		camera.update();
	}
	
	public void shutdown(){
		
		// important to shutdown scenegraph before render-engine, since
		// thread safety of instancing clusters.
		// scenegraph sets isRunning to false, render-engine signals all
		// waiting threads to shutdown
		sceneGraph.shutdown();
	}

	public Scenegraph getSceneGraph() {
		return sceneGraph;
	}
	
}
