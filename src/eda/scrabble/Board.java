package eda.scrabble;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

import eda.scrabble.Game.Coordinate;
import eda.scrabble.Game.WordXY;

public class Board extends Grid {

	private Dictionary dictionary;
	private Deque<WordXY> words;
		
	private void __init() {
		
		words = new LinkedList<WordXY>();
		
	}
	
	public Board(Map<Character, Integer> map) {
		super(map);
		__init();
	}

	public Board(Board board) {
		super(board);
		this.dictionary = board.dictionary;
		this.words = new LinkedList<WordXY>(board.words);
//		__init();
	}


	/**
	 * @return the dictionary
	 */
	public Dictionary getDictionary() {
		return dictionary;
	}

	/**
	 * @param dictionary the dictionary to set
	 */
	public Board setDictionary(Dictionary dictionary) {
		this.dictionary = dictionary;
		return this;
	}

	/**
	 * @return the _words
	 */
	public Deque<WordXY> getWords() {
		return words;
	}

	/**
	 * @param _words the _words to set
	 */
	public Board setWords(Deque<WordXY> words) {
		this.words = words;
		return this;
	}
	
}
