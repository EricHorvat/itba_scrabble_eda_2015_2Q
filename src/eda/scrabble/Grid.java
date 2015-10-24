package eda.scrabble;

public class Grid {

	public final static int GRID_SIZE = 15;
	public final static char EMPTY_SPACE = ' ';
	
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m\u001B[44m";
	
	public final static boolean COLORIZE = false;
	
	public final static boolean DEBUG = true;
	
	char[][] grid = new char[GRID_SIZE][GRID_SIZE];
	
	public Grid() {
		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {
				grid[i][j] = EMPTY_SPACE;
			}
		}
	}
	
	public Grid(Grid grid) {
		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {
				this.grid[i][j] = grid.get(i, j);
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
		for (int i = 0; i < GRID_SIZE; i++) {
			int h = i %10; 
			System.out.print(h+"|");
		}
		System.out.println('+');
		System.out.print(" +");
		for (int i = 0; i < GRID_SIZE*2-1; i++)
			System.out.print('-');
		System.out.println('+');
		for (int i = 0; i < GRID_SIZE; i++) {
			int h = i%10;
			System.out.print(String.valueOf(h)+'|');;
			for (int j = 0; j < GRID_SIZE; j++) {
				if (DEBUG)
//					if (COLORIZE && Game.getInstance().isOccupied(j, i))
//						System.out.print(ANSI_WHITE + grid[i][j]+ ANSI_RESET +  "|");
//					else
						System.out.print(grid[i][j]+"|");
				else
					System.out.print(grid[i][j]+"|");
			}
			System.out.println();
			System.out.print(" |");
			for (int j = 0; j < GRID_SIZE*2-1; j++) {
				System.out.print("-");
			}
			System.out.println("+");
		}
		System.out.print(" +");
		for (int i = 0; i < GRID_SIZE; i++) {
			int h = i %10; 
			System.out.print(h+"|");
		}
		System.out.println('+');
	}
	
	public void printSimple() {
		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {
				if (DEBUG)
//					if (COLORIZE && Game.getInstance().isOccupied(j, i))
//						System.out.print(ANSI_WHITE + grid[i][j]+ ANSI_RESET);
//					else
						System.out.print(grid[i][j]);
				else
					System.out.print(grid[i][j]);
			}
			System.out.println();
		}
	}
	
	public int size() {
		return GRID_SIZE;
	}
	
}
