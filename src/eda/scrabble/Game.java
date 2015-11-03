package eda.scrabble;

import java.util.ArrayList;
import java.util.HashMap;
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
	
	public enum Direction {
		HORIZONTAL,
		VERTICAL
	};
	
	protected final static String CHAR_VALUE_FILENAME = "charValue.txt";
	
	protected final static int MAX_LENGTH_WORD = 7;
	
	public final static Map<Character,Integer> VALUE_MAP = InputData.fillValueMap(CHAR_VALUE_FILENAME);
	
	/**
	 * Nuestro tablero por defecto
	 */
	protected Board grid;
	
	protected int maxScore = 0;
	
	protected GameParameters params;
	
	protected double eta = System.nanoTime()+5*1E9;
	
	public Game(GameParameters params) {
		this.params = params;
		long start = System.nanoTime();
		Map<Character, Integer> map = new HashMap<Character, Integer>();
		Dictionary dictionary = null;
		map = InputData.getGameChars(params.lettersFileName);
		dictionary = InputData.fillDictionary(
				params.dictionaryFileName,
				InputData.DictionaryFillStrategy.HIGHEST_VALUE,
				map);
		if (params.getMaxTime() > 0) {
			this.eta = System.nanoTime()+params.getMaxTime()*1E9;
		}
		grid = new Board(map);
		grid.setDictionary(dictionary);
		long end = System.nanoTime() - start;
	}
	
	/**
	 * Obtiene un listado de caracteres disponibles a partir de un mapa de caracteres
	 * @param characters
	 * @return un listado de caracteres disponibles
	 */
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
	
	/**
	 * Obtiene los caracteres disponibles en el tablero por defecto
	 * @return un listado de caracteres disponibles
	 */
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
	
	private static String reverse(String s) {
		String reverse = "";
		for (int k = s.length() -1; k >= 0; k--)
			reverse += s.charAt(k);
		return reverse;
	}
	
	protected boolean addWord(Word word, Board grid) {
		
		// Chequeo que este en los limites del tablero
		if (!grid.canHaveWord(word)) {
			return false;
		}
		
		if (word.vec.dir == Direction.HORIZONTAL) {
			
			// Chequeo a mi izquierda
			if (grid.get(word.vec.pos.x-1, word.vec.pos.y) != Grid.EMPTY_SPACE) {				
				return false;
			}
			
			// Chequeo a mi derecha
			if (grid.get(word.vec.pos.x+word.word.length(), word.vec.pos.y) != Grid.EMPTY_SPACE) {
				return false;
			}
			int mask = 0;
			for (int i = word.vec.pos.x; i < word.vec.pos.x+word.word.length(); i++) {
				if (i - word.vec.pos.x != word.intersected) { 
				
					boolean occupied = grid.isOccupied(i, word.vec.pos.y);
					boolean needsRemoval = false;
					
					boolean masked = false;
					
					if (word.word.charAt(i-word.vec.pos.x) == grid.get(i, word.vec.pos.y)) {
//						mask |= 1 << (i - word.vec.pos.x);
//						masked = true;
					}
					
					// Chequeamos que no estemos pisando nada
					if (/*word.word.charAt(i-word.vec.pos.x) != grid.get(i, word.vec.pos.y) && */grid.get(i, word.vec.pos.y) != Grid.EMPTY_SPACE) {
						// Marcamos para eliminar
						needsRemoval = true;
					}
					
					if (!needsRemoval && grid.get(i, word.vec.pos.y+1) != Grid.EMPTY_SPACE && !occupied) {
						// Armamos el string que se acaba de formar
						String s = "";
						int j = 0;
						while (grid.get(i, word.vec.pos.y+j) != Grid.EMPTY_SPACE) {
							s += grid.get(i, word.vec.pos.y+j);
							j++;
						}
						// verificamos que este en el diccionario
						if (!grid.getDictionary().contains(reverse(s))) {
							// Marcamos para eliminar
							needsRemoval = true;
						}
					}
					if (!needsRemoval && grid.get(i, word.vec.pos.y-1) != Grid.EMPTY_SPACE && !occupied) {
						// Armamos el string que se acaba de formar
						String s = "";
						int j = 0;
						while (grid.get(i, word.vec.pos.y-j) != Grid.EMPTY_SPACE) {
							s += grid.get(i, word.vec.pos.y-j);
							j++;
						}
						// verificamos que este en el diccionario
						if (!grid.getDictionary().contains(s)) {
							// Marcamos para eliminar

							needsRemoval = true;
						}
					}
					
					
					if (needsRemoval) {
						for (int j = word.vec.pos.x; j < i; j++){
							if (!grid.isOccupied(j, word.vec.pos.y)/* && ((mask & (1 << (j - word.vec.pos.x))) == 0 )*/) {
								grid.set(j, word.vec.pos.y, Grid.EMPTY_SPACE);
							}
//							else if ( ((mask & (1 << (j - word.vec.pos.x))) != 0 ) ) {
//								grid.removeCharacter((Character)word.word.charAt(j - word.vec.pos.x));
//							}
						}
						return false;
					}
					
//					if (masked) {
//						grid.addCharacter((Character)word.word.charAt(i-word.vec.pos.x));
//					}
					
					grid.set(i, word.vec.pos.y, word.word.charAt(i-word.vec.pos.x));
				}
			}
			
		} else { // Vertical
			if (grid.get(word.vec.pos.x, word.vec.pos.y-1) != Grid.EMPTY_SPACE) {
				return false;
			}
			if (grid.get(word.vec.pos.x, word.word.length()+word.vec.pos.y) != Grid.EMPTY_SPACE) {
				return false;
			}
			int mask = 0;
			for (int i = word.vec.pos.y; i < word.vec.pos.y+word.word.length(); i++) {
				if (i - word.vec.pos.y != word.intersected) {
					boolean isOccupied = grid.isOccupied(word.vec.pos.x, i);
					boolean needsRemoval = false;
					
					boolean masked = false;
					
					if (word.word.charAt(i-word.vec.pos.y) == grid.get(word.vec.pos.x, i)) {
//						mask |= 1 << (i - word.vec.pos.y);
//						masked = true;
					}
					
					if (/*word.word.charAt(i-word.vec.pos.y) != grid.get(word.vec.pos.x, i) && */grid.get(word.vec.pos.x, i) != Grid.EMPTY_SPACE) {
						needsRemoval = true;
					}
					
					if (!needsRemoval && grid.get(word.vec.pos.x+1, i) != Grid.EMPTY_SPACE && !isOccupied) {
					// Armamos el string a eliminar
						String s = "";//+word.charAt(i-y);
						int j = 0;
						while (grid.get(word.vec.pos.x+j,i) != Grid.EMPTY_SPACE) {
							s += grid.get(word.vec.pos.x+j,i);
							j++;
						}
						if (!grid.getDictionary().contains(s)) {
							// Sacar del tablero lo que quedo
							needsRemoval = true;
						}
					}
					
					if (!needsRemoval && grid.get(word.vec.pos.x-1, i) != Grid.EMPTY_SPACE && !isOccupied) {
						// Armamos el string a eliminar
						String s = "";
						int j = 0;
						while (grid.get(word.vec.pos.x-j,i) != Grid.EMPTY_SPACE) {
							s += grid.get(word.vec.pos.x-j,i);
							j++;
						}
						if (!grid.getDictionary().contains(reverse(s))) {
							needsRemoval = true;
						}
					}
					
					
					if (needsRemoval) {
						for (int j = word.vec.pos.y; j < i; j++) {
							if (!grid.isOccupied(word.vec.pos.x, j)/* && ((mask & (1 << (j - word.vec.pos.y))) == 0 )*/) {
								grid.set(word.vec.pos.x, j, Grid.EMPTY_SPACE);
							}
//							else if ( ((mask & (1 << (j - word.vec.pos.y))) != 0 ) ) {
//								grid.removeCharacter((Character)word.word.charAt(j - word.vec.pos.y));
//							}
						}
						return false;
					}
					
//					if (masked) {
//						grid.addCharacter((Character)word.word.charAt(i-word.vec.pos.y));
//					}
					
					grid.set(word.vec.pos.x, i, word.word.charAt(i-word.vec.pos.y));
				}
			}
		}
		
		grid.addWord(word);
		
		return true;
	}
	
	protected Word removeWord(Word word) {
		return removeWord(word, grid);
	}
	
	protected Word removeWord(Word word, Board grid) {
		Word w = grid.removeWord(word);
		removeWordVisually(w, grid);
		return w;
	}
	
	private void removeWordVisually(Word word, Board grid) {
		if (word.vec.dir == Direction.HORIZONTAL) {
			for (int i = word.vec.pos.x; i < word.word.length()+word.vec.pos.x; i++) {
				if (i - word.vec.pos.x != word.intersected && !grid.isOccupied(i, word.vec.pos.y)) {
					
					if (grid.get(i, word.vec.pos.y+1) == Grid.EMPTY_SPACE && grid.get(i, word.vec.pos.y-1) == Grid.EMPTY_SPACE) {
						
						grid.addCharacter((Character) word.word.charAt(i - word.vec.pos.x));
						grid.set(i, word.vec.pos.y, Grid.EMPTY_SPACE);
					}
				}
			}
		} else {
			for (int i = word.vec.pos.y; i < word.word.length()+word.vec.pos.y; i++) {
				if (i - word.vec.pos.y != word.intersected && !grid.isOccupied(word.vec.pos.x, i)) {
					
					if (grid.get(word.vec.pos.x+1, i) == Grid.EMPTY_SPACE && grid.get(word.vec.pos.x-1, i) == Grid.EMPTY_SPACE) {
						grid.addCharacter((Character) word.word.charAt(i - word.vec.pos.y));
						grid.set(word.vec.pos.x, i, Grid.EMPTY_SPACE);
					}
				}
			}
		}
		
	}
	
	
	public abstract void solve();
	
	public void start() {
		
		solve();
		
	}
	

}
