package launcher;

import launcher.VideoMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;

import core.context.Configuration;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.GraphicsDevice;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.FlowLayout;

import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;


public class SandboxLauncher {
	
	public static void main(String[] args) {

		Configuration conf = Configuration.getInstance();
		
		// Get available video modes
		int monitor = (int) glfwGetPrimaryMonitor();
		GraphicsEnvironment localEnvironment = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] deviceList = localEnvironment.getScreenDevices();
		DisplayMode[] modeList = deviceList[monitor].getDisplayModes();

		List<VideoMode> modes = new ArrayList<>();
		for (DisplayMode mode : modeList) {
			VideoMode nm = new VideoMode(mode.getWidth(), mode.getHeight());
			if (!modes.contains(nm))
				modes.add(nm);
		}
		System.out.println(modes.size() + " video modes found");
		
		// Frame
		JFrame frame = new JFrame("Sandbox Launcher");
		
		// FlowLayout
		frame.getContentPane().setLayout(new FlowLayout());
		
		// ComboBox
		JComboBox<VideoMode> modeCombo = new JComboBox<VideoMode>();
		VideoMode confMode = new VideoMode(conf.getWindowWidth(), conf.getWindowHeight());
		
		if (!modes.contains(confMode))
			modeCombo.addItem(confMode);
		
		for (VideoMode mode : modes)
			modeCombo.addItem(mode);
		
		modeCombo.setSelectedItem(confMode);
		frame.add(modeCombo);
		
		// CheckBox fullscreen
		JCheckBox fullCb = new JCheckBox("Fullscreen");
		frame.add(fullCb);

		// Play button
		JButton playBtn = new JButton("Play", new ImageIcon("play.png"));
		playBtn.setPreferredSize(new Dimension(65, 25));
		playBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				VideoMode svm = (VideoMode) modeCombo.getSelectedItem();
				conf.setWindowWidth(svm.Width);
				conf.setWindowHeight(svm.Height);
				conf.setX_ScreenResolution(svm.Width);
				conf.setY_ScreenResolution(svm.Height);
				conf.setFullscreen(fullCb.isSelected());
				conf.saveParamChanges();
				new Thread(() -> {sandbox.SandboxWorld.main(null);}).start();
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
		});
		frame.add(playBtn);
		
		// Misc
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
}
