package launcher;

import org.lwjgl.Version;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import window.Window;

public class Launcher {

	public static void main(String[] args) {
		
		Launcher launcher = new Launcher();
		launcher.run();

	}
	
	public void run() {
		System.out.println("Hello LWJGL " + Version.getVersion() + "!");

		Window window = new Window();

		// Free the window callbacks and destroy the window
		Callbacks.glfwFreeCallbacks(window.id());
		GLFW.glfwDestroyWindow(window.id());

		// Terminate GLFW and free the error callback
		GLFW.glfwTerminate();
		GLFW.glfwSetErrorCallback(null).free();
	}

}
