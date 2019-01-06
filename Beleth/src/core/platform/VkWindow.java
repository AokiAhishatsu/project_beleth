package core.platform;

import static org.lwjgl.glfw.GLFW.GLFW_CLIENT_API;
import static org.lwjgl.glfw.GLFW.GLFW_NO_API;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_TRUE;

import core.platform.Window;
import core.context.VkContext;

public class VkWindow extends Window{
	
	public VkWindow() {
	
		super(VkContext.getConfig().getDisplayTitle(),
			VkContext.getConfig().getWindowWidth(), VkContext.getConfig().getWindowHeight());
	}
	
	@Override
	public void create() {
		
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        
        setId(glfwCreateWindow(getWidth(), getHeight(), getTitle(), 0, 0));
        
        if(getId() == 0) {
		    throw new RuntimeException("Failed to create window");
		}
        
        setIcon("res/textures/logo/Beleth_icon32.png");
	}
	
	@Override
	public void show() {
		glfwShowWindow(getId());
	}

	@Override
	public void draw() {
	}

	@Override
	public void shutdown() {
		
		glfwDestroyWindow(getId());
	}

	@Override
	public boolean isCloseRequested() {
		
		return glfwWindowShouldClose(getId());
	}

	@Override
	public void resize(int x, int y) {
		// TODO Auto-generated method stub
		
	}

}
