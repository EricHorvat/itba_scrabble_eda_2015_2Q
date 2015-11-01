/**
 * 
 */
package eda.scrabble;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

import eda.scrabble.Game.WordXY;

/**
 * @author martin
 *
 */
public class StackBoard extends Board {
	
	private Deque<WordXY> wordsStack = new LinkedList<Game.WordXY>();
	
	/**
	 * @param map
	 */
	public StackBoard(Map<Character, Integer> map) {
		super(map);
	}

	/**
	 * @param board
	 */
	public StackBoard(Board board) {
		super(board);
	}

	/**
	 * @return the words
	 */
	public Deque<WordXY> getWordsStack() {
		return this.wordsStack;
	}

	/**
	 * @param words the words to set
	 */
	public void setWordsStack(Deque<WordXY> words) {
		this.wordsStack = words;
	}
	
	public void addWord(WordXY word) {
		this.wordsStack.push(word);
	}
	
	public void removeWord(WordXY word) {
		this.wordsStack.pop();
	}

}
