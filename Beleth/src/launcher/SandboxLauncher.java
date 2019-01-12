package launcher;

import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

public class SandboxLauncher {

	private static JTextField textfield1, textfield2, textfield3;
		
	public static void main(String[] args) {
			  		  
		JFrame f = new JFrame("Sandbox Launcher");
		
		f.getContentPane().setLayout(new FlowLayout());
		textfield1 = new JTextField("Text field 1", 10);
		textfield2 = new JTextField("Text field 2", 10);
		textfield3 = new JTextField("Text field 3", 10);
		f.getContentPane().add(textfield1);
		f.getContentPane().add(textfield2);
		f.getContentPane().add(textfield3);
		
		JButton b = new JButton("Play >", new ImageIcon("play.png"));    
		b.setBounds(200, 200, 200, 100);
		f.getContentPane().add(b);   
		
		f.setSize(250,300);    
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//f.pack();
		f.setVisible(true);
	}
	

}
