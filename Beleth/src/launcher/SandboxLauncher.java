package launcher;

import launcher.VideoMode;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;

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

		JFrame frame = new JFrame("Sandbox Launcher");
		frame.getContentPane().setLayout(new FlowLayout());

		JComboBox<VideoMode> modeCb = new JComboBox<VideoMode>();
		for (VideoMode mode : modes)
			modeCb.addItem(mode);
		frame.add(modeCb);

		JButton playBtn = new JButton("Play", new ImageIcon("play.png"));
		playBtn.setPreferredSize(new Dimension(65, 25));
		playBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				VideoMode svm = (VideoMode) modeCb.getSelectedItem();
				String[] margs = { "-width", Integer.toString(svm.Width), "-height", Integer.toString(svm.Height) };
				new Thread(() -> {
					sandbox.SandboxWorld.main(margs);
				}).start();
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
		});
		frame.add(playBtn);

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
}
