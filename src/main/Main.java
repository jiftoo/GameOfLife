package main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import game.Game;

public class Main {

	private static JFrame f;
	private static Canvas c;

	public static void main(String[] args) {
		initFrame();
		new Game(f, c, 600, 600).start(40);
	}

	private static void initFrame() {
		try {
			SwingUtilities.invokeAndWait(() -> {
				f = new JFrame();
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				// f.setResizable(false);
				f.setTitle("Conway's Game of life by 0x666c");

				c = new Canvas();
				c.setBackground(Color.BLACK);
				c.setPreferredSize(new Dimension(600, 600));
				f.add(c);
				f.pack();

				f.addComponentListener(new ComponentAdapter() {
					public void componentResized(ComponentEvent e) {
						c.setPreferredSize(f.getSize());
					}
				});

				f.setLocationRelativeTo(null);

				f.setVisible(true);

				c.createBufferStrategy(2);
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}