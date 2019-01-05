package core;

import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwTerminate;

import org.lwjgl.glfw.GLFWErrorCallback;
import core.context.BaseContext;
import core.platform.Input;
import core.platform.Window;
import core.util.Constants;

public class CoreEngine{
	
	private static int fps;
	private static float framerate = 200;
	private static float frameTime = 1.0f/framerate;
	private boolean isRunning;
	
	private Window window;
	private Input input;
	private RenderEngine renderEngine;
	private GLFWErrorCallback errorCallback;
	
	private void init()
	{
		glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
		
		renderEngine = BaseContext.getRenderEngine();
		window = BaseContext.getWindow();
		input = BaseContext.getInput();
		
		input.create(window.getId());
		window.show();
	}
	
	public void start()
	{
		init();
		
		if(isRunning)
			return;
		
		run();
	}

	public void run() {
		
		this.isRunning = true;
		
		int frames = 0;
		long frameCounter = 0;
		
		long lastTime = System.nanoTime();
		double unprocessedTime = 0;
		
		// Rendering Loop
		while(isRunning)
		{
			boolean render = false;
			
			long startTime = System.nanoTime();
			long passedTime = startTime - lastTime;
			lastTime = startTime;
			
			unprocessedTime += passedTime / (double) Constants.NANOSECOND;
			frameCounter += passedTime;
			
			while(unprocessedTime > frameTime)
			{
				render = true;
				unprocessedTime -= frameTime;
				
				if(BaseContext.getWindow().isCloseRequested()){
					stop();
				}
				
				update();
				
				if(frameCounter >= Constants.NANOSECOND)
				{
					setFps(frames);
					frames = 0;
					frameCounter = 0;
				}
			}
			if(render)
			{
				render();
				frames++;
			}
			else
			{
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}		
		}
		
		shutdown();	
	}

	private void stop()
	{
		if(!isRunning)
			return;
		
		isRunning = false;
	}
	
	private void render()
	{
		renderEngine.render();
		window.draw();
	}
	
	private void update()
	{
		input.update();
		renderEngine.update();
	}
	
	private void shutdown()
	{
		window.shutdown();
		input.shutdown();
		renderEngine.shutdown();
		errorCallback.free();
		glfwTerminate();
	}
	
	public static float getFrameTime() {
		return frameTime;
	}

	public static int getFps() {
		return fps;
	}

	public static void setFps(int fps) {
		CoreEngine.fps = fps;
	}

}
