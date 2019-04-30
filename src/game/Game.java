package game;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Game {
	public static final int SW = 600, SH = 600;

	private final BufferStrategy bs;
	private final JFrame frame;
	private final Canvas canvas;

	private boolean lmb = false, rmb = false;
	private boolean paused = false;

	private Field field;
	
	private Thread gameThread;
	
	public Game(JFrame f, Canvas c, int w, int h) {
		this.frame = f;
		this.canvas = c;
		
		c.requestFocus();
		c.requestFocusInWindow();
		
		this.bs = c.getBufferStrategy();

		c.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					lmb = true;
				}
				if (SwingUtilities.isRightMouseButton(e)) {
					rmb = true;
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					lmb = false;
				}
				if (SwingUtilities.isRightMouseButton(e)) {
					rmb = false;
				}
			}
		});
		c.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_SPACE) {
					paused ^= true;
				}
				if(e.getKeyCode() == KeyEvent.VK_N) {
					field.clear();
				}
				if(e.getKeyCode() == KeyEvent.VK_G) {
					field.toggleGrid();
				}
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					System.exit(0);
				}
				if(e.getKeyCode() == KeyEvent.VK_S) {
					start(25);
				}
				if(e.getKeyCode() == KeyEvent.VK_F) {
					start(40);
				}
			}
		});
	}
	
	@SuppressWarnings("deprecation")
	public void start(int dt) {
		if(gameThread != null) gameThread.stop();
		gameThread = new Thread(() -> run(dt));
		gameThread.start();
	}

	private void run(int dt) {
		long now;
		long last = System.nanoTime();
		double delta = 0;
		double deltaTime = 1e9 / dt;

		field = new Field(this);

		while (true) {
			Graphics g = bs.getDrawGraphics();
			
			field.input();
			
			now = System.nanoTime();
			delta += (now - last) / deltaTime;
			last = now;
			if (delta >= 1) {
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

				field.render(g);
				field.newGeneration();
				
				delta--;
			}
			
			g.dispose();
			bs.show();
		}
	}

	public boolean isLPressed() {
		return lmb;
	}
	
	public boolean isRPressed() {
		return rmb;
	}
	
	public boolean isPressed() {
		return lmb || rmb;
	}
	
	public boolean isPaused() {
		return paused;
	}

	public Point getMouseLocation() {
		return canvas.getMousePosition();
	}
	
	public JFrame getFrame() {
		return frame;
	}
	
	public Canvas getCanvas() {
		return canvas;
	}
}
