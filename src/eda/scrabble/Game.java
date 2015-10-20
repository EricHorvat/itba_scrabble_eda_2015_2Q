package eda.scrabble;

import java.util.List;

import eda.scrabble.file.InputData;

public class Game {
	
	
	private final static String DICTIONARY_FILENAME = "diccionario.txt";
	private final static String LETTERS_FILENAME = "letras.txt";
	
	private final static int MAX_LENGTH_WORD = 7;
	
	private Grid grid;
	private static Game self = null;
	
	private Dictionary dictionary;
	private List<Character> characters;
	
	public enum Direction {
		HORIZONTAL,
		VERTICAL
	};
	
	private Game() {
		grid = new Grid();
		dictionary = InputData.fillDictionary(DICTIONARY_FILENAME,
				InputData.DictionaryFillStrategy.LOWEST_OCURRENCY);
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
				for (int i = y; i < y+word.length(); y++)
					grid.set(x, i, word.charAt(i-y));
				break;
		}
		
	}
	
	public void start() {
		//(Martin v7)TODO: Llamaria a un metodo getNext o algo asi
		/*TODO ACA SE EJECUTA MEJOR OPCION*/
		
		String s = dictionary.bestFirstOption(characters, 7);
		addWord((grid.GRID_SIZE-s.length())/2, (grid.GRID_SIZE-s.length())/2, Direction.HORIZONTAL, s);
		grid.print();
		
	//	System.out.println(dictionary.bestFirstOption(characters, MAX_LENGTH_WORD));
	//	System.out.println(dictionary.bestFirstLimitedOption(characters, MAX_LENGTH_WORD,'P'));
		System.out.println(dictionary.bestLimitedOptionAfter(characters, MAX_LENGTH_WORD,'A', "PEDO"));
	}
	
	public static Game getInstance() {
		if (self == null) {
			self = new Game();
		}
		return self;
	}

}
