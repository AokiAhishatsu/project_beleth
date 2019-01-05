package sandbox;

import core.context.VkContext;
import core.components.atmosphere.Skydome;
import core.components.atmosphere.Sun;
import core.components.water.Water;
import core.engine.VkDeferredEngine;

public class SandboxWorld {
	
	public static void main(String[] args) {
		
		VkContext.create();

		VkDeferredEngine renderEngine = new VkDeferredEngine();
		renderEngine.setGui(new VkSystemMonitor());
		renderEngine.init();
		
		renderEngine.getSceneGraph().addObject(new Skydome());
		renderEngine.getSceneGraph().addTransparentObject(new Sun());
		renderEngine.getSceneGraph().setWater(new Water());
//		renderEngine.getSceneGraph().setTerrain(new Planet());
		
		VkContext.setRenderEngine(renderEngine);
		VkContext.getCoreEngine().start();
	}
}
