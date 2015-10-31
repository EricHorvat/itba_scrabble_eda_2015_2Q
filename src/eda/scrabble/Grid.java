package eda.scrabble;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import eda.scrabble.Game.Coordinate;

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
	
	public final static boolean DEBUG = false;
	
	private char[][] grid = new char[GRID_SIZE][GRID_SIZE];
	
	private static int[][] zorbistTable = new int[GRID_SIZE*GRID_SIZE][27];
	
	private static boolean HASH_INITIALIZED = false;
	
	protected Map<Coordinate, Boolean> intersections;
	protected Map<Character, Integer> characters;
	
	private static void __init_zorbist() {
		
		if (!HASH_INITIALIZED) {
			
			HASH_INITIALIZED = true;
			
			Random random = new Random();
			
			for (int i = 0; i < GRID_SIZE*GRID_SIZE; i++) {
				for (int j = 0; j < 27; j++) {
					zorbistTable[i][j] = random.nextInt();
				}
			}
			
		}
		
	}
	
	public Grid(Map<Character, Integer> characters) {

		intersections = new HashMap<Game.Coordinate, Boolean>();
		this.characters = new HashMap<Character, Integer>(characters); 
		
		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {
				grid[i][j] = EMPTY_SPACE;
			}
		}
		
		__init_zorbist();
	}
	
	public Grid(Grid grid) {
		intersections = new HashMap<Coordinate, Boolean>(grid.intersections);
		characters = new HashMap<Character, Integer>(grid.characters);
		
		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {
				this.grid[i][j] = grid.get(j, i);
			}
		}
		
		__init_zorbist();
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
		int n = 0;
		int score = 0;
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
			System.out.print(String.valueOf(h)+'|');
			for (int j = 0; j < GRID_SIZE; j++) {
				if (grid[i][j] != EMPTY_SPACE) {
					n++;
					score += Game.VALUE_MAP.get(grid[i][j]);
				}
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
		System.out.println("used characters: " + n + "/" + score);
	}
	
	public void printSimple() {
		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {
				System.out.print(grid[i][j]);
			}
			System.out.println();
		}
	}
	
	public void printSimpleDump(String outputFile) throws IOException {
		
		File fout = new File(outputFile);
		
		FileOutputStream fos = new FileOutputStream(fout);
		
		BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fos));
		
		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {
				bufferedWriter.write(grid[i][j]);			
			}
			bufferedWriter.newLine();
		}
		
		bufferedWriter.close();
	}
	
	public boolean isOccupied(int x, int y) {
		Boolean b = intersections.get(new Coordinate(x,y));
		return b != null && b == true;
	}
	
	public boolean isIntersection(int x, int y) {
		return isOccupied(x, y);
	}
	
	public void markOccupied(int x, int y) {
		if (DEBUG) System.out.println("Mark Ocuppied ("+x+","+y+")");
		
		markIntersection(x, y);
	}
	
	public void markAvailable(int x, int y) {
		if (DEBUG) System.out.println("Mark Available ("+x+","+y+")");
		
		clearIntersection(x, y);
	}
	
	// Aliases
	public void markIntersection(int x, int y) {
		
		
		
		markIntersection(new Coordinate(x, y));
	}
	
	public void markIntersection(Coordinate pos) {
		
		intersections.put(pos, true);
	}
	
	public void clearIntersection(int x, int y) {
		
		clearIntersection(new Coordinate(x, y));
	}
	
	public void clearIntersection(Coordinate pos) {
		
		intersections.put(pos, false);
	}
	
	public void addCharacter(Character c) {
		
		characters.put(c, characters.get(c) + 1);
	}
	
	public void removeCharacter(Character c) {
		
		characters.put(c, characters.get(c) - 1);
	}
	
	/**
	 * @return the _characters
	 */
	public Map<Character, Integer> getCharacters() {
		return characters;
	}

	/**
	 * @param _characters the _characters to set
	 */
	public Grid setCharacters(Map<Character, Integer> _characters) {
		this.characters = _characters;
		return this;
	}
	
	public int size() {
		return GRID_SIZE;
	}
	
	int[] primes = {2,3,5,7,11,13,17,23,29,31,37};
	
	@Override
	public int hashCode() {
		
//		StringBuffer sb = new StringBuffer(GRID_SIZE*GRID_SIZE);

//		int seed = 0;
		
//		int hash = 0;
		
//		for (int i = 0; i < GRID_SIZE; i++) {
//			for (int j = 1; j < GRID_SIZE; j++) {
//				if (grid[i][j] != Grid.EMPTY_SPACE)
//					hash = 28 * hash + ( (int)grid[i][j] - 64 );
//				else
//					hash = 28 * hash;
//				if (grid[i][j] != Grid.EMPTY_SPACE)
//					sb.append((int)grid[i][j] - 64);
//				else
//					sb.append((int)0);
//				seed ^= (int)grid[i][j] - 65 + 0x9e3779b9 + (seed << 6) + (seed << 2);
//			}
//		}
		
		int h = 0;
		
		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {
				if (grid[i][j] != Grid.EMPTY_SPACE) {
					int k = (int)grid[i][j] - 65;
					h = h ^ zorbistTable[i*GRID_SIZE+j][k];
				}
			}
		}
		
		return h;

//	int h = 0;
		
//	for (int i = 0; i < GRID_SIZE; i++) {
//		for (int j = 0; j < GRID_SIZE; j++) {
//			if (grid[i][j] != Grid.EMPTY_SPACE) {
//				int k = (int)grid[i][j] - 65;
//				h = (h) ^ ( (int)Math.pow(5, i) + (int)Math.pow(7,j) ) * ( (int)grid[i][j] - 64 );
//			} else {
//				h = (h) ^ 0;
//			}
//		}
//	}
	
//	return h;
		
		
//		return Arrays.deepHashCode(grid);
		
//		int hash = 0;
//		for (Map.Entry<Character, Integer> e : characters.entrySet()) {
//			hash ^= (int)Math.pow((int)(char)e.getKey()-64, e.getValue());
//		}
		
//		return hash^intersections.hashCode();
		
//		return sb.toString().hashCode();
//		return seed;
//		return hash;
	}

	/**
	 * @return the intersections
	 */
	public Map<Coordinate, Boolean> getIntersections() {
		HashMap<Coordinate, Boolean> hm = new HashMap<Coordinate, Boolean>();
		for (Map.Entry<Coordinate, Boolean> e : intersections.entrySet()) {
			if (e.getValue() != null && e.getValue() == true) {
				hm.put(e.getKey(), e.getValue());
			}
		}
		return hm;
	}
	
	public int getUsed() {
		int r = 0;
		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {
				if (grid[i][j] != EMPTY_SPACE)
					r++;
			}
		}
		return r;
	}
	
}
