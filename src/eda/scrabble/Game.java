package eda.scrabble;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eda.scrabble.file.InputData;

public class Game {
	
	private class Coordinate{
		public int x;
		public int y;

		public Coordinate(int x, int y) {
			// TODO Auto-generated constructor stubç
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
	
	private final static String DICTIONARY_FILENAME = "diccionario.txt";
	private final static String LETTERS_FILENAME = "letras.txt";
	
	private final static int MAX_LENGTH_WORD = 7;
	
	private Grid grid;
	private static Game self = null;
	
	private Dictionary dictionary;
	private List<Character> characters;
	private Map<Coordinate,Boolean> used = new HashMap<Coordinate,Boolean>();
	
	public enum Direction {
		HORIZONTAL,
		VERTICAL
	};
	
	private Game() {
		grid = new Grid();
		dictionary = InputData.fillDictionary(DICTIONARY_FILENAME,
				InputData.DictionaryFillStrategy.LOWEST_OCURRENCY);
		System.out.println(dictionary.toString());
		characters = InputData.getGameChars(LETTERS_FILENAME);
	}
	
	//(Martin v7) TODO: El dictionary tendria que hacer una integracion con esto
	private void addWord(int x, int y, Direction d, String word) {
		
		if (x < 0 || y < 0)
			throw new IllegalArgumentException();
		if (x >= grid.size() || y >= grid.size())
			throw new IllegalArgumentException();
		if (word == null)
			throw new IllegalArgumentException();
		
		switch (d) {
			case HORIZONTAL:
				for (int i = x; i < x+word.length(); i++)
					grid.set(i, y, word.charAt(i-x));
				break;
			case VERTICAL:
				for (int i = y; i < y+word.length(); i++)
					grid.set(x, i, word.charAt(i-y));
				break;
		}
		
	}
	
	public void start() {
		//(Martin v7)TODO: Llamaria a un metodo getNext o algo asi
		/*TODO ACA SE EJECUTA MEJOR OPCION*/
		
		
		String s = dictionary.bestFirstOption(characters, 7);

		System.out.println(s);
		int x = (grid.GRID_SIZE-s.length())/2;
		int y = grid.GRID_SIZE/2;
		addWord(x, y, Direction.HORIZONTAL, s);
		grid.print();
		int i = 0;
		Direction j = Direction.HORIZONTAL;
		
		System.out.println(s + "("+x+","+y+")");
		
		String aux  = null;
		while (s != null ) {
			aux = null;
			for (i = 0; i < s.length() && aux == null; i++) {
				boolean cont=false;
				Character c = (Character)s.charAt(i);
				if (j == Direction.VERTICAL) {
					Boolean b = used.get(new Coordinate(x,y+i));
					if (b != null && b == true)
						cont = true;
				}
				else {
					Boolean b = used.get(new Coordinate(x+i,y));
					if (b != null && b == true)
						cont = true;
				}
				if (!cont)
					aux = dictionary.bestLimitedOptionAfter(characters, MAX_LENGTH_WORD, c, null);
			}
			if (aux == null)
				break;
			if (j == Direction.VERTICAL) {
				used.put(new Coordinate(x, y+i-1), true);
				System.out.println("Setting ("+(x)+","+(y+i-1)+")");
			} else {
				used.put(new Coordinate(x+i-1, y), true);
				System.out.println("Setting ("+(x+i-1)+","+(y)+")");
			}
			int p = aux.indexOf(s.charAt(i-1));
			s = aux;
			if (j == Direction.VERTICAL) {
				addWord(x, (grid.GRID_SIZE-s.length())/2, Direction.HORIZONTAL, s);
			} else {
				addWord(x+i-1, y-p, Direction.VERTICAL, s);
				x = x+i-1;
				j = Direction.VERTICAL;
				y = y - p;
				System.out.println(s + "("+x+","+y+")");
			}
			grid.print();
			System.out.println(s);
		}
		
	//	System.out.println(dictionary.bestFirstOption(characters, MAX_LENGTH_WORD));
		//System.out.println(dictionary.bestFirstLimitedOption(characters, MAX_LENGTH_WORD,'D'));
		//System.out.println(dictionary.bestLimitedOptionAfter(characters, MAX_LENGTH_WORD,'A', "NADIE"));
	}
	
	public static Game getInstance() {
		if (self == null) {
			self = new Game();
		}
		return self;
	}

}
