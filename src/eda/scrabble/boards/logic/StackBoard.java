/**
 * 
 */
package eda.scrabble.boards.logic;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

import eda.scrabble.elements.Word;

/**
 * @author martin
 *
 */
public class StackBoard extends Board {
	
	private Deque<Word> wordsStack = new LinkedList<Word>();
	
	/**
	 * @param map
	 */
	public StackBoard(Map<Character, Integer> map) {
		super(map);
	}

	public StackBoard(Board board) {
		super(board);
	}
	
	/**
	 * @param board
	 */
	public StackBoard(StackBoard board) {
		super(board);
		this.wordsStack = new LinkedList<Word>(board.wordsStack);
	}

	/**
	 * @return the words
	 */
	public Deque<Word> getWordsStack() {
		return this.wordsStack;
	}

	/**
	 * @param words the words to set
	 */
	public void setWordsStack(Deque<Word> words) {
		this.wordsStack = words;
	}
	
	public void addWord(Word word) {
		this.wordsStack.push(word);
	}
	
	public Word removeWord(Word word) {
		return this.wordsStack.pop();
	}

}
