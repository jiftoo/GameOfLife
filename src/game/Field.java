package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

public class Field {
	
	private  	  int CELLSX = 200, CELLSY = 200;
	private final int CELLSIZE = 10;
	private		  int OFFSETX = 0,//(Game.SW / 2) - ((CELLSX * CELLSIZE) / 2),
							 OFFSETY = 0;//(Game.SH / 2) - ((CELLSY * CELLSIZE) / 2);
	
	private 	  int WIDTH  = -1,
					  HEIGHT = -1;
	
	private boolean[][] present, future;
	
	private final Game game;
	
	private boolean drawGrid = false;
	
	public Field(Game game) {
		present 	= new boolean[CELLSX][CELLSY];
		future 		= new boolean[CELLSX][CELLSY];
		
		this.game = game;
		
		WIDTH = game.getCanvas().getWidth();
		HEIGHT = game.getCanvas().getHeight();
	}
	
	public void newGeneration() {

		/*
		 * 
		 * 1. Any live cell with fewer than two live neighbors dies
		 * 
		 * 2. Any live cell with two or three live neighbors lives
		 * 
		 * 3. Any live cell with more than three live neighbors dies
		 * 
		 * 4. Any dead cell with exactly three live neighbors becomes a live cell
		 * 
		 */
		
		if (!game.isPaused()) {
			for (int x = 0; x < CELLSX; x++) {
				for (int y = 0; y < CELLSY; y++) {
					int n = checkNeighbours(present, x, y);

					if (n == 3 && !present[x][y]) {
						future[x][y] = true;
					} else if (n < 2 && present[x][y]) {
						future[x][y] = false;
					} else if (n > 3 && present[x][y]) {
						future[x][y] = false;
					} else {
						future[x][y] = present[x][y];
					}
				}
			}
			for (int i = 0; i < CELLSX; i++) {
				for (int j = 0; j < CELLSY; j++) {
					present[i][j] = future[i][j];
					future[i][j] = false;
				}
			} 
		}
		
		if(CELLSX != game.getCanvas().getWidth() / CELLSIZE || CELLSY != game.getCanvas().getHeight() / CELLSIZE) {
			WIDTH = game.getCanvas().getWidth();
			HEIGHT = game.getCanvas().getHeight();
			
			CELLSX = WIDTH / CELLSIZE;
			CELLSY = HEIGHT / CELLSIZE;
			
			OFFSETX = (WIDTH / 2) - ((CELLSX * CELLSIZE) / 2);
			OFFSETY = (HEIGHT / 2) - ((CELLSY * CELLSIZE) / 2);
			
			clear();
		}
	}
	
	public void input() {
		if (game.isPressed()) {
			Point click = game.getMouseLocation();
			if (click != null) {
				int indX = (click.x - OFFSETX) / CELLSIZE;
				int indY = (click.y - OFFSETY) / CELLSIZE;

				if (!(indX < 0 || indY < 0 || indX > CELLSX - 1 || indY > CELLSY - 1)) {
					if(game.isLPressed())
						present[indX][indY] = true;
					else if(game.isRPressed())
						present[indX][indY] = false;
				}
			}
		}
	}

	public void render(Graphics g) {
		for (int i = 0; i < CELLSX; i++) {
			for (int j = 0; j < CELLSY; j++) {
				if(present[i][j]) {
					g.setColor(Color.YELLOW);
					g.fillRect(i * CELLSIZE + OFFSETX, j * CELLSIZE + OFFSETY, CELLSIZE, CELLSIZE);
					if(!(CELLSIZE < 3) && !drawGrid) {
						g.setColor(Color.BLACK);
						g.drawRect(i * CELLSIZE + OFFSETX, j * CELLSIZE + OFFSETY, CELLSIZE, CELLSIZE);
					}
				}
			}
		}
		
		if(drawGrid) {
			g.setColor(game.isPaused() ? Color.DARK_GRAY : Color.DARK_GRAY.darker());
			for (int i = 0; i < CELLSX; i++) {
				for (int j = 0; j < CELLSY; j++) {
					g.drawLine(i * CELLSIZE, 0, i * CELLSIZE, HEIGHT);
					g.drawLine(0, j * CELLSIZE, WIDTH, j * CELLSIZE);
				}
			}
		}
		
		drawBorder(g);
		
		Point click = game.getMouseLocation();
		if(click != null && click.x > OFFSETX && click.x < CELLSIZE*CELLSX+OFFSETX &&
				click.y > OFFSETY && click.y < CELLSIZE*CELLSY+OFFSETY)
			g.drawRect((click.x / CELLSIZE) * CELLSIZE, (click.y / CELLSIZE) * CELLSIZE, CELLSIZE, CELLSIZE);
		
		drawTip(g);
	}

	private int checkNeighbours(boolean[][] arr, int x, int y) {
		int neighbours = 0;

		for (int colNum = y - 1; colNum <= (y + 1); colNum += 1) {
			for (int rowNum = x - 1; rowNum <= (x + 1); rowNum += 1) {
				if (!((colNum == y) && (rowNum == x))) {
					if (withinGrid(colNum, rowNum)) {
						if(arr[rowNum][colNum]) {
							neighbours++;
						}
					}
				}
			}
		}
		
		return neighbours;
	}
	
	private boolean withinGrid(int colNum, int rowNum) {
	    if((colNum < 0) || (rowNum < 0) ) {
	        return false;
	    }
	    if((colNum >= CELLSY) || (rowNum >= CELLSX)) {
	        return false;
	    }
	    return true;
	}
	
	public void drawBorder(Graphics g) {
		g.setColor(game.isPaused() ? Color.LIGHT_GRAY : Color.WHITE);
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setStroke(new BasicStroke(3f));
		g2.drawRect(OFFSETX+1, OFFSETY+1, CELLSIZE * CELLSX-2, CELLSIZE * CELLSY-2);
	}
	
	public void clear() {
		present = new boolean[CELLSX][CELLSY];
		future = new boolean[CELLSX][CELLSY];
	}
	
	public void toggleGrid() {
		drawGrid ^= true;
	}
	
	private float tipFreeTicks = 0;
	private float tipTicks = 0;
	private void drawTip(Graphics g) {
		if(tipFreeTicks > 120) {
			if(tipTicks > 0.999f) return;
			tipTicks += 0.01;
		}
		tipFreeTicks++;
		
		g.setColor(Color.getHSBColor(0, 0, 1f - tipTicks));
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
		final String s1 = "Welcome to Conway's Game of life!";
		final String s2 = "Controls:";
		final String s3 = "G - Show grid";
		final String s4 = "N - Clear field";
		final String s5 = "ESC - Quit";
		final int w1 = g.getFontMetrics().stringWidth(s1) / 2;
		final int w2 = g.getFontMetrics().stringWidth(s2) / 2;
		final int w3 = g.getFontMetrics().stringWidth(s3) / 2;
		final int w4 = g.getFontMetrics().stringWidth(s4) / 2;
		final int w5 = g.getFontMetrics().stringWidth(s5) / 2;
		g.drawString(s1, (WIDTH / 2) - w1, (HEIGHT / 2) - (16 / 2) - 45);
		g.drawString(s2, (WIDTH / 2) - w2, (HEIGHT / 2) - (16 / 2) - 20);
		g.drawString(s3, (WIDTH / 2) - w3, (HEIGHT / 2) - (16 / 2));
		g.drawString(s4, (WIDTH / 2) - w4, (HEIGHT / 2) - (16 / 2) + 20);
		g.drawString(s5, (WIDTH / 2) - w5, (HEIGHT / 2) - (16 / 2) + 40);
	}
}
