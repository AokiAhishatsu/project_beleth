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
import launcher.VideoMode;
import core.context.VkContext;

public class VkWindow extends Window {
	
	private VideoMode vMode = null;
	
	public VkWindow() {
		super(VkContext.getConfig().getDisplayTitle(), VkContext.getConfig().getWindowWidth(),
				VkContext.getConfig().getWindowHeight());
	}
	
	public VkWindow(VideoMode vMode) {
		super(VkContext.getConfig().getDisplayTitle(), vMode.Width, vMode.Height);
		this.vMode = vMode;
	}
	
	public void create() {

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
		glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
		
		if (vMode != null)
			setId(glfwCreateWindow(vMode.Width, vMode.Height, getTitle(), 0, 0));
		else
			setId(glfwCreateWindow(getWidth(), getHeight(), getTitle(), 0, 0));

		if (getId() == 0) {
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
