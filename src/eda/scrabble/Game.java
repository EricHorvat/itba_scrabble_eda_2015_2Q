package eda.scrabble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import eda.scrabble.file.InputData;

public class Game {
	
	private static class Coordinate{
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
	
	private static class WordXY {
		
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
	
	private final static String DICTIONARY_FILENAME = "dic12.txt";
	private final static String LETTERS_FILENAME = "l8.txt";
	private final static String CHAR_VALUE_FILENAME = "charValue.txt";
	
	private final static boolean DEBUG = false;
	
	public final static Map<Character,Integer> VALUE_MAP = InputData.fillValueMap(CHAR_VALUE_FILENAME);
	
	private final static int MAX_LENGTH_WORD = 7;
	
	private Grid grid;
	private static Game self = null;
	
	private Dictionary dictionary;
	private Map<Character, Integer> characters;
	private Map<Coordinate,Boolean> used = new HashMap<Coordinate,Boolean>();
	
	private List<WordXY> words = new ArrayList<WordXY>();
	
	private int maxScore = 0;
	
	public enum Direction {
		HORIZONTAL,
		VERTICAL
	};
	
	private Game() {
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
	private WordXY addWord(int x, int y, Direction d, String word) throws IllegalArgumentException {
		
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
	
	
	private void removeWord(WordXY word) {
		
		words.remove(word);
		removeWordVisually(word);
	}
	
	private void removeWordAt(int x, int y) {
		
		int count = 0;
		
		WordXY remove = null;
		
		for (WordXY word : words) {
			
			if (word.has(x, y)) {
				count++;
				remove = word;
			}
			
		}
		if (count >= 2) {
			throw new IllegalStateException();
		}
		
		if (count == 1) {
			removeWordVisually(remove);
			words.remove(remove);
		}
		
	}
	
	private void removeWordAt(Coordinate coord) {
		removeWordAt(coord.x, coord.y);
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
	
	private void markOccupied(int x, int y) {
		if (DEBUG)
			System.out.println("Mark Ocuppied ("+x+","+y+")");
		used.put(new Coordinate(x, y), true);
	}
	
	private void markAvailable(int x, int y) {
		if (DEBUG)
			System.out.println("Mark Available ("+x+","+y+")");
		used.put(new Coordinate(x, y), false);
	}
	
	private void approximate() {
		System.out.println("Aprox solution");
		
		String s = dictionary.bestFirstOption(characters, 7);
		
		System.out.println(s);
		int x = (grid.size()-s.length())/2;
		int y = grid.size()/2;
		addWord(x, y, Direction.HORIZONTAL, s);
		grid.print();
		// La i tiene la info del indice de la ultima palabra buscada
		// para hacer el hill climb sacamos la palabra y seguimos
		// probando a partir de i
		int i = 0;
		Direction j = Direction.HORIZONTAL;
		
		System.out.println(s + "("+x+","+y+")");
		
		String aux  = null;
		while (s != null) {
			aux = null;
			while (i < s.length() && aux == null) {
				System.out.println("starting for with i: "+i + " s: "+s+" aux>: "+aux );
				boolean cont = false;
				Character c = (Character)s.charAt(i);
				if (j == Direction.VERTICAL) {
					if (isOccupied(x, y+1))
						cont = true;
				}
				else {
					if (isOccupied(x+i, y))
						cont = true;
				}
				if (!cont) {
					System.out.println("looking for word with> "+c);
					
					System.out.println(characters.size());
					System.out.println(characters);
					characters.put(c, characters.get(c)+1);
					aux = dictionary.bestLimitedOptionAfter(characters, MAX_LENGTH_WORD, c, null);
					
					System.out.println(characters.size());
					System.out.println(characters);
					
					System.out.println("ack "+aux);
					if (aux == null) {
						characters.put(c, characters.get(c)-1);
						i++;
						if (i == s.length()) {
							s = null;
							break;
						}
						continue;
					}
					if (j == Direction.VERTICAL) {
						markOccupied(x, y+i);
					} else {
						markOccupied(x+i, y);
					}
					
					int p = aux.indexOf(s.charAt(i));
					String back = s;
					s = aux;
					System.out.println("j is "+ j);
					if (j == Direction.VERTICAL) {
						boolean cut = false;
						try {
							addWord(x-p, y+i, Direction.HORIZONTAL, s);
						} catch (IllegalArgumentException ex) {
							System.out.println(ex.getStackTrace().toString());
							System.out.println(ex.getMessage());
							s = back;
							cut = true;
						}
						
						j = Direction.HORIZONTAL;
						x = x-p;
						y = y+i;
						if (!cut)
							i = -1;
					} else {
						boolean cut = false;
						try {
							addWord(x+i, y-p, Direction.VERTICAL, s);
						} catch (IllegalArgumentException ex) {
							System.out.println(ex.getStackTrace().toString());
							System.out.println(ex.getMessage());
							s = back;
							cut = true;
						}
						
						x = x+i;
						j = Direction.VERTICAL;
						y = y - p;
						System.out.println(s + "("+x+","+y+")");
						if (!cut)
							i = -1;
					}
					if (i != -1)
						grid.print();
					System.out.println(s);
				}
				i++;
			}
			
		}
		
	//TODO: Hill Climb Now
			
		hillClimb(i);
	}
	
	private void hillClimb(int lastIndex) {
		
		int score = grid.getScore();
		
		if (DEBUG)
			System.out.println("current score: " + score);
		
		switch (words.size()) {
		case 0:
			break;
		case 1:
				// Buscar la palabra que le sigue
		default:
			// Removemos 1, probamos las siguientes posibilidades
			// Volvemos y seguimos
			
			
			break;
		}
		
		
		if (DEBUG)
			System.out.println("hillclimbing with lastIndex: "+ lastIndex);
		
		words.remove(words.size()-1);
		
		WordXY lastWord = words.get(words.size()-1);
		
		
		
		if (lastWord != null) {
			if (DEBUG)
				System.out.println(lastWord.word);
			for (int i = 0; i < lastWord.word.length(); i++) {
				
				String aux = dictionary.bestLimitedOptionAfter(characters, MAX_LENGTH_WORD, (Character)lastWord.word.charAt(i), null);
				
				if (DEBUG)
					System.out.println(aux);
				
			}
		} else {
			// Habia una sola palabra en el tablero
		}
		
		
		
	}
	
	private void addCharacter(Character c) {
		characters.put(c, characters.get(c)+1);
	}
	
	private void removeCharacter(Character c) {
		characters.put(c, characters.get(c)-1);
	}
	
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
			
			aux = dictionary.bestFirstLimitedOption(characters, MAX_LENGTH_WORD, (Character)intersectionChar);
			
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
				
				aux = dictionary.bestLimitedOptionAfter(characters, MAX_LENGTH_WORD, gridElem.word.charAt(i), aux);
				
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
	
	private static void printUsed(List<LetterXY> used) {
		System.out.print("used: ");
		for (LetterXY l : used)
			System.out.print(l.c);
		System.out.println();
	}
	
	
	private void possibleSolution1(List<LetterXY> used) {
		
		String aux = null;
		WordXY toAdd = null;
		
		LetterXY letter = null;
		
		if (DEBUG) printUsed(used);
		
		// Backup used letters for restauration later
		List<LetterXY> backup = new ArrayList<>(grid.size()*grid.size());
		for (LetterXY l : used)
			backup.add(l);
		
		// Loop through @{used}. It contains the letters that will be analyzed
		// Basically Letters\IntersectedLetters
		for (int i = 0; i < used.size(); i++) {
		
			letter = used.get(i);
			
			// Lookup all words available for inserting with letter=used.get(i)
			while (true) {
				
				// Show available chars
				if (DEBUG) {
					List<Character> l = getAvailableChars();
					System.out.println("("+l.size()+"): "+l);
				}
				
				// Character in the intersection should be added to available chars
				// since it isnt available but next word will contain this letter
				addCharacter(letter.c);
				
				if (DEBUG) System.out.println("Adding " + letter.c + " for search");
				
				// SELECT word FROM dictionary WHERE
				//     word.toCharArray().isContainedIn(@{characters})
				// AND length <= @{MAX_LENGTH_WORD}
				// AND word.indexOf(@{letter.c}) != -1
				// AND word > aux -- Viene despues en el Trie
				//
				// Busca la proxima palabra despues de aux que me alcancen los caracteres y tenga el caracter
				// letter.c
				//
				// Si encuentra la palabra, me remueve sus caracters de characters
				aux = dictionary.bestLimitedOptionAfter(characters, MAX_LENGTH_WORD, letter.c, aux);
				
				if (DEBUG) System.out.println("/SEARCH/"+letter.word.word+"/"+ letter.c + "/"+aux);
				if (DEBUG) System.out.println("Removing " + letter.c + ". Already searched");
				
				// Since we added letter.c we should remove it
				removeCharacter(letter.c);
				
				// Si aux == null ==> con este caracter no hay mas palabras para buscar
				if (aux == null) {
					
					// No hay mas palabras con esta letra (letter.c)
					// ==> pasamos a la proxima
					break;
				}
				
				
				// Hacemos la matematica para que en la grilla se coloque todo prolijo
				// y en su lugar
				int intersectionIndex = aux.indexOf(letter.c);
				int letterIndex = letter.word.word.indexOf(letter.c);
				
				// Marcamos a la interseccion como lugar ocupado
				if (letter.word.direction == Direction.HORIZONTAL) {
					markOccupied(letter.word.pos.x+letterIndex, letter.word.pos.y);
				} else {
					markOccupied(letter.word.pos.x, letter.word.pos.y+letterIndex);
				}
				
				// Attempt to add word
				try {
					if (letter.word.direction == Direction.HORIZONTAL) {
						toAdd = addWord(letter.word.pos.x+letterIndex, letter.word.pos.y-intersectionIndex, Direction.VERTICAL, aux);
					} else {
						toAdd = addWord(letter.word.pos.x-intersectionIndex, letter.word.pos.y+letterIndex, Direction.HORIZONTAL, aux);
					}
					
				} catch (IllegalArgumentException ex) {
					// Reseteamos porque la palabra no se pudo agregar
					if (DEBUG) System.out.println(aux + " was not added. " + ex.getMessage());
					
					// Marcamos el lugar que habiamos marcado como ocupado
					// como libre porque no agregamos la palabra
					if (letter.word.direction == Direction.HORIZONTAL) {
						markAvailable(letter.word.pos.x+letterIndex, letter.word.pos.y);
					} else {
						markAvailable(letter.word.pos.x, letter.word.pos.y+letterIndex);
					}
					
					// Devolvemos los caracteres que no se agregaron al conjunto de caracteres
					// disponibles
					// OjO, menos el de la interseccion porque lo usa la palabra anterior
					for (int j = 0; j < aux.length(); j++) {
						Character c = (Character)aux.charAt(j);
						if (j != intersectionIndex) {
							addCharacter(c);
						}
					}
					
					// Seguimos y probamos si la proxima palabra calza en este lugar
					continue;
				}
				
				
				
				// Sacamos a @{letter} de @{used} para que cuando entre en la recursiva
				// No se ponga a buscar palabras para calzar en la interseccion
				used.remove(letter);
				
				
				// Nos fijamos si este es mejor tablero que el anterior mejor
				// De ser asi actualizamos
				int score = grid.getScore();
				if (score > maxScore)
					maxScore = score;
				
				//TODO: Aca cuando implementemos Ant y toda la bola habria que preguntar
				//     por la opcion --display 
				// Mostramos el tablero
				grid.printSimple();
				
				
				// Agregamos los caracteres que en la recursiva se van a usar para buscar
				// mas palabras
				for (int j = 0; j < aux.length(); j++) {
					Character c = (Character)aux.charAt(j);
					if (j != intersectionIndex) {
						used.add(new LetterXY(toAdd, c));
					}
				}
				
				// Perform recursive call to same method
				possibleSolution1(used);
				
				/*
				 * #################
				 * --- Cleanup Stage
				 * #################
				 */
				
				
				// Borramos la palabra que acabamos de agregar al tablero
				removeWord(toAdd);
				
				// Marcamos la interseccion como disponible porque ya que sacamos la palabra
				// no hay mas interseccion
				if (letter.word.direction == Direction.HORIZONTAL) {
					markAvailable(letter.word.pos.x+letterIndex, letter.word.pos.y);
				} else {
					markAvailable(letter.word.pos.x, letter.word.pos.y+letterIndex);
				}
				
				
				// Restauramos la informacion para que el proximo ciclo haga su trabajo
				used.clear();
				for (LetterXY l: backup)
					used.add(l);
				
				
				// Reponemos los caracteres que acabamos de consumir
				for (int j = 0; j < aux.length(); j++) {
					if (j != intersectionIndex)
						addCharacter((Character)aux.charAt(j));
				}
				
			}
			
			// No deberia suceder, pero chequeamos igual por si las moscas
			if (letter.c != used.get(i).c) {
				System.err.println("######################\n---------------Mismatch\n#################");
				throw new IllegalAccessError(letter.c + " should be equal to " + used.get(i).c);
			}
			
		}
		
	}
	
	private static class LetterXY {
		
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
	
	/**
	 * Places each of the words in all posible starting positions
	 * And calls the solver methods.
	 */
	private void exact() {
		
//		String bestFirstOption = dictionary.bestFirstOption(characters, 7);
		
		maxScore = 0;
		
		List<String> allWords = dictionary.getWords();
		
		// Reservo a lo sumo grid.size()^2 de letras
//		Queue<LetterXY> l = new LinkedList<LetterXY>();
		List<LetterXY> l = new ArrayList<>(grid.size()*grid.size());
		
		WordXY tmp = null;
		
		for (String w : allWords) {
			if (DEBUG) {
				System.out.println("########################################");
				System.out.println("------------ Starting from scratch");
				System.out.println("########################################");
			}
			if (DEBUG) {
				List<Character> li = getAvailableChars();
				System.out.println("Available chars("+li.size()+"): " + li);
			}
			
			int x = grid.size()/2-w.length()+1;
			int y = grid.size()/2;
			
			
			// No problem board should be empty
			tmp = addWord(x, y, Direction.HORIZONTAL, w);
			
			// Mark characters as used
			for (char c : w.toCharArray()) {
				removeCharacter(c);
			}
			
			if (DEBUG)
				System.out.println("Printing initial board");
			grid.print();
			
			for (int i = x+1; i <= grid.size()/2; i++) {
				for (char c : w.toCharArray()) {
					l.add(new LetterXY(tmp, (Character)c));
				}
//				placeWordInDepth(words.get(words.size()-1), 0);
//				placeWordInWidth(words.get(words.size()-1), 0, 0);
				possibleSolution1(l);
				while (words.size() > 0)
					removeWord(words.get(words.size()-1));
				if (DEBUG) {
					grid.print();
				}
				
				tmp = addWord(i, y, Direction.HORIZONTAL, w);
				l.clear();
			}
			
			for (char c : w.toCharArray()) {
				addCharacter((Character)c);
			}
			
			
			
			removeWord(words.get(words.size()-1));
		}
		
		System.out.println("Max Score is: " + maxScore);
		
	}
	
	public void exact2() {
		
	}
	
	public void start(boolean exact) {
		
		if (exact) {
			exact();
		} else {
			approximate();
		}
	}
	
	
	
	public static Game getInstance() {
		if (self == null) {
			self = new Game();
		}
		return self;
	}

}
