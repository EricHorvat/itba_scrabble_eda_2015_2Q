package eda.scrabble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import eda.scrabble.file.InputData;

public abstract class Game {
	
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
		
		public LetterXY(WordXY word, Character c) {
			this.word = word;
			this.c = c;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (!(obj instanceof LetterXY)) return false;
			LetterXY other = (LetterXY) obj;
			if (!this.word.equals(other.word)) return false;
			if (this.c != other.c) return false;
			return true;
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
	
	protected final static String DICTIONARY_FILENAME = "dic7.txt";
	protected final static String LETTERS_FILENAME = "l7.txt";
	protected final static String CHAR_VALUE_FILENAME = "charValue.txt";
	
	protected final static int MAX_LENGTH_WORD = 7;
	
	protected final static boolean DEBUG = false;
	
	public final static Map<Character,Integer> VALUE_MAP = InputData.fillValueMap(CHAR_VALUE_FILENAME);
	
	protected Grid grid;
	
	protected Map<Character, Integer> characters;
	private Map<Coordinate,Boolean> used = new HashMap<Coordinate,Boolean>();
	
	protected Dictionary dictionary;
	
	protected List<WordXY> words = new ArrayList<WordXY>();
	
	protected int maxScore = 0;
	
	public enum Direction {
		HORIZONTAL,
		VERTICAL
	};
	
	public Game() {
		grid = new Grid();
		long start = System.nanoTime();
		characters = InputData.getGameChars(LETTERS_FILENAME);
		dictionary = InputData.fillDictionary(
				DICTIONARY_FILENAME,
				InputData.DictionaryFillStrategy.HIGHEST_VALUE,
				characters);
		long end = System.nanoTime() - start; 
		System.out.println("Load Time: " + end/1000000.0 + "ms");
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
		List<Character> l = new ArrayList<Character>(characters.entrySet().size());
		
		for (Entry<Character,Integer> e : characters.entrySet()) {
			if (e.getValue() > 0) {
				for (int i = 0; i < e.getValue(); i++)
					l.add(e.getKey());
			}
		}
		return l;
	}
	
	//(Martin v7) TODO: El dictionary tendria que hacer una integracion con esto
	protected WordXY addWord(int x, int y, Direction d, String word) throws IllegalArgumentException {
		
		if (x < 0 || y < 0)
			throw new IllegalArgumentException("Pos escapes negative");
		if (x >= grid.size() || y >= grid.size())
			throw new IllegalArgumentException("Init pos escapes positive");
		if (d == Direction.HORIZONTAL) {
			if (x+word.length() >= grid.size()) {
				throw new IllegalArgumentException("Final pos escapes positive "+d + " x+len:"+x+"+"+word.length()+"="+(x+word.length()));
			}
		} else {
			if (y+word.length() >= grid.size()) {
				throw new IllegalArgumentException("Final pos escapes positive " + d);
			}
		}
		
		if (DEBUG)
			System.out.println("inserting "+word+" at x:"+x+" y:"+y+" "+d);
		switch (d) {
			case HORIZONTAL:
				if (grid.get(x-1, y) != Grid.EMPTY_SPACE)
					throw new IllegalArgumentException("Horizontal failed left");
				if (grid.get(x+word.length(), y) != Grid.EMPTY_SPACE)
					throw new IllegalArgumentException("horizontal failed right");
				for (int i = x; i < x+word.length(); i++) {
					Boolean occupied = isOccupied(i, y);
					if (grid.get(i, y+1) != Grid.EMPTY_SPACE && !occupied) {
					// Armamos el string a eliminar
						String s = ""+word.charAt(i-x);
						int j = 1;
						while (grid.get(i-x, y+j) != Grid.EMPTY_SPACE) {
							s += grid.get(i-x, y+j);
							j++;
						}
						if (!dictionary.contains(s)) {
							// Lo sacamos
							for (j = i-1; j >= x; j--) {
								Boolean bb = used.get(new Coordinate(j, y));
								if (bb != null && bb == true) continue;
								grid.set(j, y, Grid.EMPTY_SPACE);
							}
							throw new IllegalArgumentException("horizontal failed middle bottom");
						}
					}
					if (grid.get(i, y-1) != Grid.EMPTY_SPACE && !occupied) {
						// verificamos que este en el diccionario
						String s = ""+word.charAt(i-x);
						int j = 1;
						while (grid.get(i-x, y-j) != Grid.EMPTY_SPACE) {
							s += grid.get(i-x, y-j);
							j++;
						}
						if (!dictionary.contains(s)) {
						// Lo sacamos
							for (j = i-1; j >= x; j--) {
								Boolean bb = used.get(new Coordinate(j, y));
								if (bb != null && bb == true) continue;
								grid.set(j, y, Grid.EMPTY_SPACE);
							}
							throw new IllegalArgumentException("horizontal failed middle top");
						}
					}
					if (grid.get(i, y) != Grid.EMPTY_SPACE && !occupied) {
						
						for (int j = i-1; j >= x; j--) {
							Boolean bb = used.get(new Coordinate(j, y));
							if (bb != null && bb == true) continue;
							grid.set(j, y, Grid.EMPTY_SPACE);
						}
						throw new IllegalArgumentException("Stepping over other word");
					}
					grid.set(i, y, word.charAt(i-x));
				}
				break;
			case VERTICAL:
				if (grid.get(x, y-1) != Grid.EMPTY_SPACE) {
					throw new IllegalArgumentException("vertical failed top found "+grid.get(x, y-1));
				}
				if (grid.get(x,word.length()+y) != Grid.EMPTY_SPACE) {
					throw new IllegalArgumentException("vertical failed bottom found "+grid.get(x, word.length()+y));
				}
				for (int i = y; i < y+word.length(); i++) {
					boolean isOccupied = isOccupied(x, i);
					if (grid.get(x+1, i) != Grid.EMPTY_SPACE && !isOccupied) {
					// Armamos el string a eliminar
						String s = ""+word.charAt(i-y);
						int j = 1;
						while (grid.get(x+j,i-y) != Grid.EMPTY_SPACE) {
							s += grid.get(x+j,i-y);
							j++;
						}
						if (!dictionary.contains(s)) {
						// Sacar del tablero lo que quedo
							for (j = i-1; j >= y; j--) {
								Boolean bb = used.get(new Coordinate(x, j));
								if (DEBUG)
									System.out.println("bb:"+bb+" x:"+x+" y:"+j);
									if (bb != null && bb == true) continue;
								//if (bb == null || bb == false)
									grid.set(x, j, Grid.EMPTY_SPACE);
							}
							throw new IllegalArgumentException("vertical failed middle right");
						}
					}
					if (grid.get(x-1, i) != Grid.EMPTY_SPACE && !isOccupied) {
						// Armamos el string a eliminar
						String s = ""+word.charAt(i-y);
						int j = 1;
						while (grid.get(x-j,i-y) != Grid.EMPTY_SPACE) {
							s += grid.get(x-j,i-y);
							j++;
						}
						if (!dictionary.contains(s)) {
							// Sacar del tablero lo que quedo
							for (j = i-1; j >= y; j--) {
								Boolean bb = used.get(new Coordinate(x, j));
								if (DEBUG)
									System.out.println("bb:"+bb+" x:"+x+" y:"+j);
									if (bb != null && bb == true) continue;
								//if (bb == null || bb == false)
									grid.set(x, j, Grid.EMPTY_SPACE);
							}
							throw new IllegalArgumentException("vertical failed middle left");
						}
					}
					if (grid.get(x, i) != Grid.EMPTY_SPACE && !isOccupied) {
						
						for (int j = i-1; j >= y; j--) {
							Boolean bb = used.get(new Coordinate(x, j));
							if (bb != null && bb == true) continue;
							grid.set(x, j, Grid.EMPTY_SPACE);
						}
						throw new IllegalArgumentException("Stepping over other word");
					}
					grid.set(x, i, word.charAt(i-y));
				} 
				break;
		}
		
		WordXY wordXY = new WordXY(word, new Coordinate(x, y), d);
		
		this.words.add(wordXY);
		
		return wordXY;
		
	}
	
	protected void removeWord(WordXY word) {
		
		words.remove(word);
		removeWordVisually(word);
	}
	
	private void removeWordVisually(WordXY word) {
		
		if (word.direction == Direction.HORIZONTAL) {
			for (int i = word.pos.x; i < word.word.length()+word.pos.x; i++) {
				if (!isOccupied(i, word.pos.y)) {
					if (DEBUG)
						System.out.println("resetting "+(new Coordinate(i, word.pos.y)));
					grid.set(i, word.pos.y, ' ');
				}
			}
		} else {
			for (int i = word.pos.y; i < word.word.length()+word.pos.y; i++) {
				if (!isOccupied(word.pos.x, i)) {
					if (DEBUG)
						System.out.println("resetting "+(new Coordinate(word.pos.x, i)));
					grid.set(word.pos.x, i, ' ');
				}
			}
		}
		
	}
	
	public boolean isOccupied(int x, int y) {
		Boolean b = used.get(new Coordinate(x,y));
		return b != null && b == true;
	}
	
	protected void markOccupied(int x, int y) {
		if (DEBUG)
			System.out.println("Mark Ocuppied ("+x+","+y+")");
		used.put(new Coordinate(x, y), true);
	}
	
	protected void markAvailable(int x, int y) {
		if (DEBUG)
			System.out.println("Mark Available ("+x+","+y+")");
		used.put(new Coordinate(x, y), false);
	}
	
	protected void addCharacter(Character c) {
		characters.put(c, characters.get(c)+1);
	}
	
	protected void removeCharacter(Character c) {
		characters.put(c, characters.get(c)-1);
	}
	
	/**
	 * 
	 * @param gridElem
	 * @param skip
	 * @param accumAdded
	 * @deprecated
	 */
	private void placeWordInWidth(WordXY gridElem, int skip, int accumAdded) {
		
		if (gridElem == null) return;
		
		String aux = null;
		
		for (int i = skip; i < gridElem.word.length(); i++) {
			
			if (DEBUG) System.out.println("HEAD/i:"+skip + " w:"+gridElem.word );
			
			char intersectionChar = gridElem.word.charAt(i);
			
			addCharacter((Character)intersectionChar);
			
			if (DEBUG) {
				List<Character> l = getAvailableChars();
				System.out.println("SEARCH/"+intersectionChar+"_"+gridElem.word+" (" +l.size()+") " +getAvailableChars());
			}
			
//			aux = dictionary.bestFirstLimitedOption(characters, MAX_LENGTH_WORD, (Character)intersectionChar);
			
			if (DEBUG) System.out.println("ACK aux="+aux);
			
			removeCharacter((Character)intersectionChar);
			
			if (aux == null)
				continue;
			
			if (gridElem.direction == Direction.HORIZONTAL) {
				markOccupied(gridElem.pos.x+i, gridElem.pos.y);
			} else {
				markOccupied(gridElem.pos.x, gridElem.pos.y+i);
			}
			
			int intersectionIndex = aux.indexOf(intersectionChar);
			
			WordXY addedWord = null;
			
			try {
				if (gridElem.direction == Direction.HORIZONTAL) {
					addedWord = addWord(gridElem.pos.x + i, gridElem.pos.y - intersectionIndex, Direction.VERTICAL, aux);
				} else {
					addedWord = addWord(gridElem.pos.x - intersectionIndex, gridElem.pos.y + i, Direction.HORIZONTAL, aux);
				}
			}  catch (IllegalArgumentException ex) {
				if (DEBUG) System.out.println(ex.getMessage());
				for (int j = 0; j < aux.length(); j++) {
					addCharacter((Character)aux.charAt(j));
				}
				removeCharacter((Character)intersectionChar);
				
				if (gridElem.direction == Direction.HORIZONTAL) {
					markAvailable(gridElem.pos.x+i, gridElem.pos.y);
				} else {
					markAvailable(gridElem.pos.x, gridElem.pos.y+i);
				}
				continue;
			}
			
			grid.print();
			int score = grid.getScore();
			System.out.println("Score is : " + grid.getScore());
			if (score > maxScore)
				maxScore = score;
			
			System.out.println(accumAdded);
			System.out.println(words);
			
			
			placeWordInDepth(addedWord, score);//  currentScore);(addedWord, 0, accumAdded+1);
//			for (int j = words.size()-1; j > accumAdded; j--) {
//				placeWordInWidth(words.get(j), 0, accumAdded+1);
//			}
			
			if (DEBUG)
				System.out.println("###################\n------ Back to Width\n###################");
			
			
			placeWordInWidth(gridElem, i+1, accumAdded+1);
			
			for (int j = 0; j < aux.length(); j++) {
				addCharacter((Character)aux.charAt(j));
			}
			removeCharacter((Character)intersectionChar);
			
//			if (i == 0) {
//				for (int j = words.size()-1; j >= 1; j--) {
//					placeWordInDepth(words.get(j), score);
//				}
//			}
			
			removeWord(words.get(words.size()-1));
			
			
			if (gridElem.direction == Direction.HORIZONTAL) {
				markAvailable(gridElem.pos.x+i, gridElem.pos.y);
			} else {
				markAvailable(gridElem.pos.x, gridElem.pos.y+i);
			}
			
		}
		
	}
	
	private void placeWordInDepth(WordXY gridElem, int currentScore) {
		if (gridElem == null) return;
		String aux = null;
		
		for (int i = 0; i < gridElem.word.length(); i++) {
			
			int sum = 0;
			for (Entry<Character,Integer> e : characters.entrySet()) {
				if (e.getKey() != (Character)(char)0)
					sum += VALUE_MAP.get(e.getKey())*e.getValue();
			}
			System.out.println("Remaining chars sum: " +sum);
			// Si con los caracteres que me quedan no llego a sumar maxScore
			// podo el arbol
			if (currentScore+sum < maxScore) {
				System.out.println("Leaving cur+sum:"+(currentScore+sum)+" max:"+maxScore);
				continue;
			}
			
			
			if (gridElem.direction == Direction.HORIZONTAL) {
				if (isOccupied(gridElem.pos.x+i, gridElem.pos.y)) {
					if (DEBUG)
						System.out.println("Char is intersection. Cant use, leaving");
					continue;
				}
			} else {
				if (isOccupied(gridElem.pos.x, gridElem.pos.y+i)) {
					if (DEBUG)
						System.out.println("Char is intersection. Cant use, leaving");
					continue;
				}
			}
			char intersectionChar = gridElem.word.charAt(i);
			
			while (true) {
				
				if (DEBUG)
					System.out.println("Trying to find a word with char: " + gridElem.word.charAt(i) + " src: " + gridElem.word);
				if (DEBUG) {
					List<Character> l = getAvailableChars();
					System.out.println("Available chars("+l.size()+"): " + l);
				}
				
				
				
				if (DEBUG)
					System.out.println("Replacing "+ gridElem.word.charAt(i) + " for use in intersection");
				characters.put((Character)intersectionChar, characters.get(intersectionChar)+1);
				
//				aux = dictionary.bestLimitedOptionAfter(characters, MAX_LENGTH_WORD, gridElem.word.charAt(i), aux);
				
				if (DEBUG)
					System.out.println("Found word: " + aux);
				if (DEBUG)
					System.out.println("Removing "+ gridElem.word.charAt(i) + " from characters");
				characters.put((Character)intersectionChar, characters.get(intersectionChar)-1);
				
				if (aux == null)
					break;
				
//				char intersectionChar = gridElem.word.charAt(i);
				int intersectionIndex = aux.indexOf(intersectionChar);
				
				if (gridElem.direction == Direction.HORIZONTAL) {
					if (gridElem.pos.y-intersectionIndex >= grid.size() - 2) {
						System.out.println("Skipping. Word wont fit");
						continue;
					}
					
				} else {
					if (gridElem.pos.x-intersectionIndex >= grid.size() - 2) {
						System.out.println("Skipping. Word wont fit");
						continue;
					}
				}
				
				if (gridElem.direction == Direction.HORIZONTAL) {
					markOccupied(gridElem.pos.x+i, gridElem.pos.y);
				} else {
					markOccupied(gridElem.pos.x, gridElem.pos.y+i);
				}
				
				try {
					if (gridElem.direction == Direction.HORIZONTAL) {
						if (DEBUG)
							System.out.println("Attempting to add " + aux + " at ("+(gridElem.pos.x+i)+","+(gridElem.pos.y-intersectionIndex)+") " + Direction.VERTICAL);
						addWord(gridElem.pos.x + i, gridElem.pos.y - intersectionIndex, Direction.VERTICAL, aux);
					} else {
						if (DEBUG)
							System.out.println("Attempting to add " + aux + " at ("+(gridElem.pos.x-intersectionIndex)+","+(gridElem.pos.y+i)+") " + Direction.HORIZONTAL);
						addWord(gridElem.pos.x - intersectionIndex, gridElem.pos.y + i, Direction.HORIZONTAL, aux);
					}
				}  catch (IllegalArgumentException ex) {
					if (DEBUG)
						System.out.println("Error:" + ex.getMessage());
//					grid.print();
					for (int j = 0; j < aux.length(); j++) {
						characters.put((Character)aux.charAt(j), characters.get((Character)aux.charAt(j))+1);
					}
					characters.put((Character)intersectionChar, characters.get(intersectionChar)-1);
					if (gridElem.direction == Direction.HORIZONTAL) {
						markAvailable(gridElem.pos.x+i, gridElem.pos.y);
					} else {
						markAvailable(gridElem.pos.x, gridElem.pos.y+i);
					}
					continue;
				}
				
				grid.print();
				int score = grid.getScore();
				System.out.println("Score is: " + grid.getScore());
				if (score > maxScore)
					maxScore = score;
				
				placeWordInDepth(words.get(words.size()-1), currentScore);
				
				for (int j = 0; j < aux.length(); j++) {
					characters.put((Character)aux.charAt(j), characters.get((Character)aux.charAt(j))+1);
				}
				characters.put((Character)intersectionChar, characters.get((Character)intersectionChar)-1);
				
				for (int j = words.size()-1; j >= 1; j--) {				
//					placeWordInWidth(words.get(j), 0);
				}
				
				removeWord(words.get(words.size()-1));
				
				if (gridElem.direction == Direction.HORIZONTAL) {
					markAvailable(gridElem.pos.x+i, gridElem.pos.y);
				} else {
					markAvailable(gridElem.pos.x, gridElem.pos.y+i);
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
