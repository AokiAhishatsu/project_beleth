package sandbox;

import java.lang.management.ManagementFactory;
import java.nio.LongBuffer;

import core.common.ui.UIScreen;
import core.CoreEngine;
import core.math.Vec4f;
import core.image.VkImageView;
import core.components.ui.VkColorPanel;
import core.components.ui.VkDynamicTextPanel;
import core.components.ui.VkGUI;
import core.components.ui.VkStaticTextPanel;
import core.components.ui.VkTexturePanel;

public class VkSystemMonitor extends VkGUI {
	
	@SuppressWarnings("restriction")
	private com.sun.management.OperatingSystemMXBean bean;
	
	@SuppressWarnings("restriction")
	@Override
	public void init(VkImageView imageView, LongBuffer waitSemaphores) {
		super.init(imageView, waitSemaphores);
		bean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		UIScreen screen0 = new UIScreen();
		screen0.getElements().add(new VkColorPanel(new Vec4f(0,0,0,0.5f), 0, 215, 325, 225,
				panelMeshBuffer, guiOverlayFbo));
		screen0.getElements().add(new VkStaticTextPanel("FPS:", 20, 45, 40, 40,
				fontsImageBundle.getImageView(), fontsImageBundle.getSampler(), guiOverlayFbo));
		screen0.getElements().add(new VkStaticTextPanel("CPU:", 20, 90, 40, 40,
				fontsImageBundle.getImageView(), fontsImageBundle.getSampler(), guiOverlayFbo));
		screen0.getElements().add(new VkDynamicTextPanel("000", 120, 45, 40, 40,
				fontsImageBundle.getImageView(), fontsImageBundle.getSampler(), guiOverlayFbo));
		screen0.getElements().add(new VkDynamicTextPanel("000", 120, 90, 40, 40,
				fontsImageBundle.getImageView(), fontsImageBundle.getSampler(), guiOverlayFbo));
		screen0.getElements().add(new VkTexturePanel("textures/logo/Vulkan_Logo.png", 0, 220, 310, 130,
				panelMeshBuffer, guiOverlayFbo));
		getScreens().add(screen0);
	}
	
	public void update(){
		
		getScreens().get(0).getElements().get(3).update(Integer.toString(CoreEngine.getFps()));
		@SuppressWarnings("restriction")
		String cpuLoad = Double.toString(bean.getSystemCpuLoad());
		if (cpuLoad.length() == 3){
			cpuLoad = cpuLoad.substring(2, 3);
		}
		else{
			cpuLoad = cpuLoad.substring(2, 4);
		}
		getScreens().get(0).getElements().get(4).update(cpuLoad + "%");
	}
	
}
