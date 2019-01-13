package sandbox;

import core.context.VkContext;
import core.components.atmosphere.Skydome;
import core.components.atmosphere.Sun;
//import core.components.planet.Planet;
import core.components.water.Water;
import core.engine.VkDeferredEngine;

import launcher.VideoMode;

public class SandboxWorld {
	
	public static void main(String[] args) {
		
		VideoMode v = readArgs(args);		
		VkContext.create(v);

		VkDeferredEngine renderEngine = new VkDeferredEngine();
		renderEngine.setGui(new VkSystemMonitor());
		renderEngine.init();
		
		renderEngine.getSceneGraph().addObject(new Skydome());
		renderEngine.getSceneGraph().addTransparentObject(new Sun());
		renderEngine.getSceneGraph().setWater(new Water());
 		//renderEngine.getSceneGraph().setTerrain(new Planet());
		
		VkContext.setRenderEngine(renderEngine);
		VkContext.getCoreEngine().start();
	}
	
	public static VideoMode readArgs(String[] args) {
		if (args != null && args.length > 3)
		{
			int argsWidth = 0;
			int argsHeight = 0;

			for (int i=0; i < args.length; i++)
			{
				try {
					if (args[i].trim().toLowerCase().equals("-width") && Integer.parseInt(args[i+1]) > 0)
					{
						argsWidth = Integer.parseInt(args[i+1]);
						continue;
					}
					if (args[i].trim().toLowerCase().equals("-height") && Integer.parseInt(args[i+1]) > 0)
						argsHeight = Integer.parseInt(args[i+1]);
					
				} catch (NumberFormatException e) {
					System.out.println("Error parsing:" + args[i] + " " + args[i+1]);
					break;
				}
							
			}
			if (argsWidth > 0 && argsHeight > 0)
				return new VideoMode(argsWidth, argsHeight);
			else
				System.out.println("video mode args failed!");
		}
		else
			System.out.println("No video mode args provided using config values.");
		return null;
	}
}
