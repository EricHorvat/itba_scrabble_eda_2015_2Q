package eda.scrabble;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import eda.scrabble.file.InputData;

public abstract class Game {
	
	public static class GameParameters {
		
		protected String dictionaryFileName;
		protected String lettersFileName;
		protected String outputFileName;
		protected boolean visual = false;
		protected double maxTime = 0;

		/**
		 * @param dictionaryFileName the dictionaryFileName to set
		 */
		public void setDictionaryFileName(String dictionaryFileName) {
			this.dictionaryFileName = dictionaryFileName;
		}

		/**
		 * @param lettersFileName the lettersFileName to set
		 */
		public void setLettersFileName(String lettersFileName) {
			this.lettersFileName = lettersFileName;
		}

		/**
		 * @param outputFileName the outputFileName to set
		 */
		public void setOutputFileName(String outputFileName) {
			this.outputFileName = outputFileName;
		}

		/**
		 * @param visual the visual to set
		 */
		public void setVisual(boolean visual) {
			this.visual = visual;
		}

		/**
		 * @param maxTime the maxTime to set
		 */
		public void setMaxTime(double maxTime) {
			this.maxTime = maxTime;
		}

		/**
		 * @return the visual
		 */
		public boolean isVisual() {
			return visual;
		}

		/**
		 * @return the maxTime
		 */
		public double getMaxTime() {
			return maxTime;
		}
		
	}
	
	protected static class Coordinate{
		public int x;
		public int y;

		public Coordinate(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Coordinate other = (Coordinate) obj;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return "("+x+","+y+")";
		}
		
	}
	
	protected static class LetterXY {
		
		WordXY word;
		Character c;
		int pos;
		
		public LetterXY(WordXY word, Character c, int pos) {
			this.word = word;
			this.c = c;
			this.pos = pos;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (!(obj instanceof LetterXY)) return false;
			LetterXY other = (LetterXY) obj;
			if (!this.word.equals(other.word)) return false;
			if (this.pos != other.pos) return false;
			if (this.c != other.c) return false;
			return true;
		}
		
		@Override
		public String toString() {
			return word.toString()+" " + c + "["+pos+"]";
		}
	}
	
	protected static class WordXY {
		
		public String word;
		public Coordinate pos;
		public Direction direction;
		
		public WordXY(String word, Coordinate pos, Direction d) {
			this.word = word;
			this.pos = pos;
			this.direction = d;
		}
		
		public boolean has(int x, int y) {
			if (this.direction == Direction.HORIZONTAL) {
				return this.pos.y == y && this.pos.x <= x && x <= this.pos.x + word.length();
			} else {
				return this.pos.x == x && this.pos.y <= y && y <= this.pos.y + word.length();
			}
		}
		
		public boolean has(Coordinate coord) {
			return has(coord.x, coord.y);
		}
		
		@Override
		public String toString() {
			return word+"("+pos.x+","+pos.y+")"+direction;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (!(obj instanceof WordXY)) return false;
			WordXY other = (WordXY) obj;
			if (!this.word.equals(other.word)) return false;
			if (!this.pos.equals(other.pos)) return false;
			if (this.direction != other.direction) return false;
			return true;
			
		}
		
	}
	
	protected final static String DICTIONARY_FILENAME = "words2.txt";
	protected final static String LETTERS_FILENAME = "letters2.txt";
	protected final static String CHAR_VALUE_FILENAME = "charValue.txt";
	
	protected final static int MAX_LENGTH_WORD = 7;
	
	protected final static boolean DEBUG = true;
	protected final static boolean ANT = false;
	
	public final static Map<Character,Integer> VALUE_MAP = InputData.fillValueMap(CHAR_VALUE_FILENAME);
	
	protected Board grid;
	
	protected List<WordXY> words = new ArrayList<WordXY>();
	
	protected int maxScore = 0;
	
	protected GameParameters params;
	
	protected double eta = System.nanoTime()+20*Math.pow(10, 9);
	
	public enum Direction {
		HORIZONTAL,
		VERTICAL
	};
	
	public Game(GameParameters params) {
		this.params = params;
		long start = System.nanoTime();
		Map<Character, Integer> map = new HashMap<Character, Integer>();
		Dictionary dictionary = null;
		if (ANT) {
			map = InputData.getGameChars(params.lettersFileName);
			dictionary = InputData.fillDictionary(
					params.dictionaryFileName,
					InputData.DictionaryFillStrategy.HIGHEST_VALUE,
					map);
			if (params.getMaxTime() > 0) {
				this.eta = System.nanoTime()+params.getMaxTime()*Math.pow(10, 9);
			}
		} else {
			map = InputData.getGameChars(LETTERS_FILENAME);
			dictionary = InputData.fillDictionary(
					DICTIONARY_FILENAME,
					InputData.DictionaryFillStrategy.HIGHEST_VALUE,
					map);
		}
		grid = new Board(map);
		grid.setDictionary(dictionary);
		long end = System.nanoTime() - start; 
		if (DEBUG) System.out.println("Load Time: " + end/1000000.0 + "ms");
	}
	
	public static List<Character> getAvailableChars(Map<Character, Integer> characters) {
		List<Character> l = new ArrayList<Character>(characters.entrySet().size());
		
		for (Entry<Character,Integer> e : characters.entrySet()) {
			if (e.getValue() > 0) {
				for (int i = 0; i < e.getValue(); i++)
					l.add(e.getKey());
			}
		}
		return l;
	}
	
	public List<Character> getAvailableChars() {
		List<Character> l = new ArrayList<Character>(grid.characters.entrySet().size());
		
		for (Entry<Character,Integer> e : grid.characters.entrySet()) {
			if (e.getValue() > 0) {
				for (int i = 0; i < e.getValue(); i++)
					l.add(e.getKey());
			}
		}
		return l;
	}
	
	protected WordXY addWord(int x, int y, Direction d, String word) throws AddWordException {
		return addWord(x, y, d, word, grid, null, grid.getDictionary());
	}
	
	protected WordXY addWord(int x, int y, Direction d, String word, Board grid) throws AddWordException {
		return addWord(x, y, d, word, grid, null, grid.getDictionary());
	}
	
	/**
	 * Draws the word on the grid and 
	 * @param x
	 * @param y
	 * @param d
	 * @param word
	 * @return
	 * @throws IllegalArgumentException
	 */
	
	protected WordXY addWord(int x, int y, Direction d, String word, Grid grid, Deque<WordXY> words, Dictionary dictionary) throws AddWordException {
		
		boolean withinBounds = true;
		
		if (x < 0 || y < 0) {
			withinBounds = false;
		}
		if (x >= grid.size() || y >= grid.size()) {
			withinBounds = false;
		}
		if (d == Direction.HORIZONTAL) {
			if (x+word.length() >= grid.size()) {
				withinBounds = false;
			}
		} else {
			if (y+word.length() >= grid.size()) {
				withinBounds = false;
			}
		}
		
		if (!withinBounds) {
			for (int j = 0; j < word.length(); j++) {
//				grid.addCharacter((Character)word.charAt(j));
			}
			throw new AddWordException("Words is out of board");
		}
		
		if (DEBUG) System.out.println("inserting "+word+" at x:"+x+" y:"+y+" "+d);
		
		
		
		switch (d) {
			case HORIZONTAL:
				if (grid.get(x-1, y) != Grid.EMPTY_SPACE) {
					for (int j = x; j < x+word.length(); j++) {
//							grid.addCharacter((Character)word.charAt(j-x));
					}
					throw new AddWordException("Horizontal failed left");
				}
				if (grid.get(x+word.length(), y) != Grid.EMPTY_SPACE) {
					for (int j = x; j < x+word.length(); j++) {
//							grid.addCharacter((Character)word.charAt(j-x));
					}
					throw new AddWordException("horizontal failed right");
				}
				
				for (int i = x; i < x+word.length(); i++) {
					boolean occupied = grid.isOccupied(i, y);
					
					boolean needsRemoval = false;
					
					if (grid.get(i, y+1) != Grid.EMPTY_SPACE && !occupied) {
						// Armamos el string que se acaba de formar
						String s = "";
						int j = 0;
						while (grid.get(i, y+j) != Grid.EMPTY_SPACE) {
							s += grid.get(i, y+j);
							j++;
						}
						String reverse = "";
						for (int k = s.length() -1; k >= 0; k--)
							reverse += s.charAt(k);
						// verificamos que este en el diccionario
						if (!dictionary.contains(reverse)) {
							
							// Marcamos para eliminar
							needsRemoval = true;
						}
					}
					if (!needsRemoval && grid.get(i, y-1) != Grid.EMPTY_SPACE && !occupied) {
						// Armamos el string que se acaba de formar
						String s = ""+word.charAt(i-x);
						int j = 1;
						while (grid.get(i, y-j) != Grid.EMPTY_SPACE) {
							s += grid.get(i, y-j);
							j++;
						}
						// verificamos que este en el diccionario
						if (!dictionary.contains(s)) {
							
							// Marcamos para eliminar
							needsRemoval = true;
						}
					}
					// Chequeamos que no estemos pisando nada
					if (!needsRemoval && grid.get(i, y) != Grid.EMPTY_SPACE && !occupied) {
						
						// Marcamos para eliminar
						needsRemoval = true;
					}
					
					if (needsRemoval) {
						if (DEBUG) System.out.print("Returning: ");
						for (int j = x; j < x+word.length(); j++) {
							boolean isIntersection = grid.isOccupied(j, y);
							if (j <= i - 1 && !isIntersection) {
								
								// Borramos el caracter que habia
								grid.set(j, y, Grid.EMPTY_SPACE);
							}
							if (DEBUG) System.out.print(word.charAt(j-x) + " ");
//							grid.addCharacter((Character)word.charAt(j-x));
						}
						if (DEBUG) System.out.println();
						
						throw new AddWordException("Failed to insert horizontally");
						
					}
					
					grid.set(i, y, word.charAt(i-x));
				}
				break;
				
				
			/*
			 *    Calculos para cuando insertamos verticalmente
			 */
			case VERTICAL:
				if (grid.get(x, y-1) != Grid.EMPTY_SPACE) {
					for (int j = y; j < y+word.length(); j++) {
//							grid.addCharacter((Character)word.charAt(j-y));
					}
					throw new AddWordException("vertical failed top found "+grid.get(x, y-1));
				}
				if (grid.get(x,word.length()+y) != Grid.EMPTY_SPACE) {
					for (int j = y; j < y+word.length(); j++) {
//							grid.addCharacter((Character)word.charAt(j-y));
					}
					throw new AddWordException("vertical failed bottom found "+grid.get(x, word.length()+y));
				}
				for (int i = y; i < y+word.length(); i++) {
					boolean isOccupied = grid.isOccupied(x, i);
					boolean needsRemoval = false;
					
					if (grid.get(x+1, i) != Grid.EMPTY_SPACE && !isOccupied) {
					// Armamos el string a eliminar
						String s = "";//+word.charAt(i-y);
						int j = 0;
						while (grid.get(x+j,i) != Grid.EMPTY_SPACE) {
							s += grid.get(x+j,i);
							j++;
						}
						if (!dictionary.contains(s)) {
							// Sacar del tablero lo que quedo
							needsRemoval = true;
							
						}
					}
					if (grid.get(x-1, i) != Grid.EMPTY_SPACE && !isOccupied) {
						// Armamos el string a eliminar
						String s = "";//+word.charAt(i-y);
						int j = 0;
						while (grid.get(x-j,i) != Grid.EMPTY_SPACE) {
							s += grid.get(x-j,i);
							j++;
						}
						String reverse = "";
						for (int k = s.length() -1; k >= 0; k--)
							reverse += s.charAt(k);
						if (!dictionary.contains(reverse)) {
							// Sacar del tablero lo que quedo
							
							needsRemoval = true;
							
						}
					}
					if (grid.get(x, i) != Grid.EMPTY_SPACE && !isOccupied) {
						
						needsRemoval = true;
					}
					
					if (needsRemoval) {
						if (DEBUG) System.out.print("Returning: ");
						for (int j = y; j < y+word.length(); j++) {
							boolean isIntersection = grid.isOccupied(x, j);// used.get(new Coordinate(x, j));
							if (j <= i - 1 && !isIntersection) {
//								if (DEBUG) System.out.println("Attempting to remove " + word.charAt(j-y));
								if (isIntersection) continue;
								
								// Borramos el caracter que habia
								grid.set(x, j, Grid.EMPTY_SPACE);
							}
							if (DEBUG) System.out.print(word.charAt(j-y) + " ");
//							grid.addCharacter((Character)word.charAt(j-y));
						}
						if (DEBUG) System.out.println();
						
						throw new AddWordException("Failed to insert vertically");
					}
					
					grid.set(x, i, word.charAt(i-y));
				} 
				break;
		}
		
		WordXY wordXY = new WordXY(word, new Coordinate(x, y), d);
		
		if (words != null) {
			words.push(wordXY);
		} else {
			this.words.add(wordXY);
		}
		
		return wordXY;
		
	}
	
	protected WordXY removeWord(WordXY word) {
		return removeWord(word, grid);
	}
	
	protected WordXY removeWord(WordXY word, Grid grid) {
		this.words.remove(word);
		removeWordVisually(word, grid);
		return word;
	}
	
	protected WordXY removeWord(Deque<WordXY> words) {
		return removeWord(words, grid);
	}
	
	protected WordXY removeWord(Deque<WordXY> words, Grid grid) {
		WordXY word = words.pop();
		removeWordVisually(word, grid);
		return word;
		
	}
	
	private void removeWordVisually(WordXY word, Grid grid) {
		
		if (word.direction == Direction.HORIZONTAL) {
			for (int i = word.pos.x; i < word.word.length()+word.pos.x; i++) {
				if (!grid.isOccupied(i, word.pos.y)) {
					if (DEBUG) System.out.println("resetting "+(new Coordinate(i, word.pos.y)) + " " + word.word.charAt(i - word.pos.x));
					grid.addCharacter((Character) word.word.charAt(i - word.pos.x));
					grid.set(i, word.pos.y, Grid.EMPTY_SPACE);
				}
			}
		} else {
			for (int i = word.pos.y; i < word.word.length()+word.pos.y; i++) {
				if (!grid.isOccupied(word.pos.x, i)) {
					if (DEBUG) System.out.println("resetting "+(new Coordinate(word.pos.x, i)));
					grid.addCharacter((Character) word.word.charAt(i - word.pos.y));
					grid.set(word.pos.x, i, Grid.EMPTY_SPACE);
				}
			}
		}
		
	}
	
	
	protected static void printUsed(List<LetterXY> used) {
		System.out.print("used: ");
		for (LetterXY l : used)
			System.out.print(l.c);
		System.out.println();
	}
	
	
	public abstract void solve();
	
	public void start() {
		
		solve();
		
	}
	

}
