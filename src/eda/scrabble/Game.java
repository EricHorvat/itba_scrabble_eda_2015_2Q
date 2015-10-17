package eda.scrabble;

import java.util.List;

import eda.scrabble.file.InputData;

public class Game {
	
	
	private final static String DICTIONARY_FILENAME = "diccionario.txt";
	private final static String LETTERS_FILENAME = "letras.txt";
	
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
		dictionary = InputData.fillDictionary(DICTIONARY_FILENAME);
		characters = InputData.getGameChars(LETTERS_FILENAME);
		/*TODO ACA SE EJECUTA MEJOR OPCION*/System.out.println(dictionary.bestOption(characters, 7));
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
	}
	
	public static Game getInstance() {
		if (self == null) {
			self = new Game();
		}
		return self;
	}

}
