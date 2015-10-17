package eda.scrabble;

public class Grid {

	private final static int GRID_SIZE = 15;
	private final static char EMPTY_SPACE = ' ';
	
	char[][] grid = new char[GRID_SIZE][GRID_SIZE];
	
	public Grid() {
		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {
				grid[i][j] = EMPTY_SPACE;
			}
		}
	}
	
	public void set(int x, int y, char l) {
		grid[y][x] = l;
	}
	
	public char get(int x, int y) {
		return grid[y][x];
	}
	
	public void print() {
		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {
				System.out.print(grid[i][j]);
			}
			System.out.println();
		}
	}
	
	public int size() {
		return GRID_SIZE;
	}
	
}
