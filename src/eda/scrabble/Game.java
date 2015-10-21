package eda.scrabble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eda.scrabble.file.InputData;

public class Game {
	
	private class Coordinate{
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
		
	}
	
	private class WordXY {
		
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
		
	}
	
	private final static String DICTIONARY_FILENAME = "dic2.txt";
	private final static String LETTERS_FILENAME = "l2.txt";
	private final static String CHAR_VALUE_FILENAME = "charValue.txt";
	
	public final static Map<Character,Integer> VALUE_MAP = InputData.fillValueMap(CHAR_VALUE_FILENAME);
	
	private final static int MAX_LENGTH_WORD = 7;
	
	private Grid grid;
	private static Game self = null;
	
	private Dictionary dictionary;
	private List<Character> characters;
	private Map<Coordinate,Boolean> used = new HashMap<Coordinate,Boolean>();
	
	private List<WordXY> words = new ArrayList<WordXY>();
	
	public enum Direction {
		HORIZONTAL,
		VERTICAL
	};
	
	private Game() {
		grid = new Grid();
		dictionary = InputData.fillDictionary(DICTIONARY_FILENAME,
				InputData.DictionaryFillStrategy.HIGHEST_VALUE);
		System.out.println(dictionary.toString());
		characters = InputData.getGameChars(LETTERS_FILENAME);
	}
	
	//(Martin v7) TODO: El dictionary tendria que hacer una integracion con esto
	private void addWord(int x, int y, Direction d, String word) throws IllegalArgumentException {
		
		if (x < 0 || y < 0)
			throw new IllegalArgumentException();
		if (x >= grid.size() || y >= grid.size())
			throw new IllegalArgumentException();
		if (word == null)
			throw new IllegalArgumentException();
		System.out.println("inserting "+word+" at x:"+x+" y:"+y+" "+d);
		switch (d) {
			case HORIZONTAL:
				if (grid.get(x-1, y) != Grid.EMPTY_SPACE)
					throw new IllegalArgumentException("Horizontal failed left");
				if (grid.get(x+word.length(), y) != Grid.EMPTY_SPACE)
					throw new IllegalArgumentException("horizontal failed right");
				for (int i = x; i < x+word.length(); i++) {
					Boolean b = used.get(new Coordinate(i, y));
					if (grid.get(i, y+1) != Grid.EMPTY_SPACE && (b == null || b == false)) {
					// Armamos el string a eliminar
						String s = ""+word.charAt(i-x);
						int j = 0;
						while (grid.get(i-x, y+j) != Grid.EMPTY_SPACE) {
							s += grid.get(i-x, y+j);
							j++;
						}
						if (!dictionary.contains(s)) {
							// Lo sacamos
							for (j = i; j >= x; j--) {
								Boolean bb = used.get(new Coordinate(j, y));
								if (bb != null && bb == true) continue;
								grid.set(j, y, Grid.EMPTY_SPACE);
							}
							throw new IllegalArgumentException("horizontal failed middle top");
						}
					}
					if (grid.get(i, y-1) != Grid.EMPTY_SPACE && (b == null || b == false)) {
					// Armamos el string a eliminar
						String s = ""+word.charAt(i-x);
						int j = 0;
						while (grid.get(i-x, y-j) != Grid.EMPTY_SPACE) {
							s += grid.get(i-x, y-j);
							j++;
						}
						if (!dictionary.contains(s)) {
						// Lo sacamos
							for (j = i; j >= x; j--) {
								Boolean bb = used.get(new Coordinate(j, y));
								if (bb != null && bb == true) continue;
								grid.set(j, y, Grid.EMPTY_SPACE);
							}
							throw new IllegalArgumentException("horizontal failed middle bottom");
						}
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
						int j = 0;
						while (grid.get(x+j,i-y) != Grid.EMPTY_SPACE) {
							s += grid.get(x+j,i-y);
							j++;
						}
						if (!dictionary.contains(s)) {
						// Sacar del tablero lo que quedo
							for (j = i-1; j >= y; j--) {
								Boolean bb = used.get(new Coordinate(x, j));
								System.out.println("bb:"+bb+" x:"+x+" y:"+j);
								//if (bb == null || bb == false)
									grid.set(x, j, Grid.EMPTY_SPACE);
							}
							throw new IllegalArgumentException("vertical failed middle right");
						}
					}
					if (grid.get(x-1, i) != Grid.EMPTY_SPACE && !isOccupied) {
						// Armamos el string a eliminar
						String s = ""+word.charAt(i-y);
						int j = 0;
						while (grid.get(x-j,i-y) != Grid.EMPTY_SPACE) {
							s += grid.get(x-j,i-y);
							j++;
						}
						if (!dictionary.contains(s)) {
							// Sacar del tablero lo que quedo
							for (j = i-1; j >= y; j--) {
								Boolean bb = used.get(new Coordinate(x, j));
								System.out.println("bb:"+bb+" x:"+x+" y:"+j);
								//if (bb == null || bb == false)
									grid.set(x, j, Grid.EMPTY_SPACE);
							}
							throw new IllegalArgumentException("vertical failed middle left");
						}
					}
					grid.set(x, i, word.charAt(i-y));
				} 
				break;
		}
		
		this.words.add(new WordXY(word, new Coordinate(x, y), d));
		
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
					grid.set(word.pos.x+i, word.pos.y, ' ');
				}
			}
		} else {
			for (int i = word.pos.y; i < word.word.length()+word.pos.y; i++) {
				if (!isOccupied(word.pos.x, i)) {
					grid.set(word.pos.x, word.pos.y+i, ' ');
				}
			}
		}
		
	}
	
	private boolean isOccupied(int x, int y) {
		Boolean b = used.get(new Coordinate(x,y));
		return b != null && b == true;
	}
	
	private void markOccupied(int x, int y) {
		System.out.println("Mark Ocuppied ("+x+","+y+")");
		used.put(new Coordinate(x, y), true);
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
					characters.add(c);
					aux = dictionary.bestLimitedOptionAfter(characters, MAX_LENGTH_WORD, c, null);
					
					System.out.println(characters.size());
					System.out.println(characters);
					
					System.out.println("ack "+aux);
					if (aux == null) {
						characters.remove(c);
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
		
		
		System.out.println("hillclimbing with lastIndex: "+ lastIndex);
		
		words.remove(words.size()-1);
		
		WordXY lastWord = words.get(words.size()-1);
		
		
		
		if (lastWord != null) {
			System.out.println(lastWord.word);
			for (int i = 0; i < lastWord.word.length(); i++) {
				
				String aux = dictionary.bestLimitedOptionAfter(characters, MAX_LENGTH_WORD, (Character)lastWord.word.charAt(i), null);
				
				System.out.println(aux);
				
			}
		} else {
			// Habia una sola palabra en el tablero
		}
		
		
		
	}
	
	private void exact() {
		
		String bestFirstOption = dictionary.bestFirstOption(characters, 7);
		
		int x = grid.size()/2-bestFirstOption.length()+1;
		int y = grid.size()/2;
		
		addWord(x, y, Direction.HORIZONTAL, bestFirstOption);
		
		grid.print();
		
		for (int i = x; i <= grid.size()/2; i++) {
			
		}
		
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
