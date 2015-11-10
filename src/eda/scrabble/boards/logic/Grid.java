package eda.scrabble.boards.logic;

import java.util.Random;

import eda.scrabble.elements.Word;
import eda.scrabble.game.Game;

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
				sum += grid[i][j] == EMPTY_SPACE ? 0 : Game.CHARACTER_VALUES.get(grid[i][j]);
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
