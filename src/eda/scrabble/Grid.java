package eda.scrabble;

public class Grid {

	public final static int GRID_SIZE = 15;
	public final static char EMPTY_SPACE = ' ';
	
	char[][] grid = new char[GRID_SIZE][GRID_SIZE];
	
	public Grid() {
		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {
				grid[i][j] = EMPTY_SPACE;
			}
		}
	}
	
	public int getScore() {
		int sum = 0;
		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {
				sum += grid[i][j] == EMPTY_SPACE ? 0 : Game.VALUE_MAP.get(grid[i][j]);
			}
		}
		return sum;
	}
	
	public void set(int x, int y, char l) {
		grid[y][x] = l;
	}
	
	public char get(int x, int y) {
		if (x < 0 || y < 0 || x >= GRID_SIZE || y >= GRID_SIZE)
			return EMPTY_SPACE;
		return grid[y][x];
	}
	
	public void print() {
		System.out.print(" +");
		for (int i = 0; i < GRID_SIZE; i++)
			System.out.print(i%10);
		System.out.println('+');
		System.out.print(" +");
		for (int i = 0; i < GRID_SIZE; i++)
			System.out.print('-');
		System.out.println('+');
		for (int i = 0; i < GRID_SIZE; i++) {
			int h = i%10;
			System.out.print(String.valueOf(h)+'|');;
			for (int j = 0; j < GRID_SIZE; j++) {
				System.out.print(grid[i][j]);
			}
			System.out.println('|');
		}
		System.out.print(" +");
		for (int i = 0; i < GRID_SIZE; i++)
			System.out.print('-');
		System.out.println('+');
	}
	
	public int size() {
		return GRID_SIZE;
	}
	
}
