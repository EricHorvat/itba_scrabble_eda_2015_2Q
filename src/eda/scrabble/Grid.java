package eda.scrabble;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Random;

public class Grid {

	public final static int GRID_SIZE = 15;
	public final static char EMPTY_SPACE = ' ';
	
	/**
	 * Nuestro almacenamiento de la grilla
	 */
	private char[][] grid = new char[GRID_SIZE][GRID_SIZE];
	
	/**
	 * Tablas para el hasheo.
	 * Buscar zorbist hashing
	 */
	private static int[][] zorbistTable = new int[GRID_SIZE*GRID_SIZE][27];
	private static boolean HASH_INITIALIZED = false;
	
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
	
	public Grid() {
		
		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {
				grid[i][j] = EMPTY_SPACE;
			}
		}
		
		__init_zorbist();
	}
	
	public Grid(Grid grid) {
		
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
		bufferedWriter.write("Max. Score: " + getScore());
		bufferedWriter.close();
	}
	
	public int size() {
		return GRID_SIZE;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Word)) {
			return false;
		}
		Grid other = (Grid) obj;
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {
				sb1.append(grid[i][j]);
				sb2.append(other.grid[i][j]);
			}
		}
		return sb1.toString().equals(sb2.toString());
	}
	
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
	
	@Override
	public String toString() {
		StringBuilder sb1 = new StringBuilder();
		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {
				sb1.append(grid[i][j]);
			}
		}
		return sb1.toString();
	}
	
}
