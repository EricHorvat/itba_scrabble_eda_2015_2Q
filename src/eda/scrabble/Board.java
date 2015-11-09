package eda.scrabble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eda.scrabble.Game.Direction;

public class Board extends Grid {

	private Dictionary dictionary;
	
	protected Map<Coordinate, Boolean> intersections;
	protected Map<Character, Integer> characters;
	private List<Word> words = new ArrayList<Word>();
	
	
	public Board(Map<Character, Integer> characters) {
		super();
		
		this.intersections = new HashMap<Coordinate, Boolean>();
		this.characters = new HashMap<Character, Integer>(characters);
	}

	public Board(Board board) {
		super(board);
		// Nos quedamos con una referencia al diccionario
		this.dictionary = board.dictionary;
		this.intersections = new HashMap<Coordinate, Boolean>(board.intersections);
		this.characters = new HashMap<Character, Integer>(board.characters);
		this.words = new ArrayList<Word>(board.words);
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
	public List<Word> getWords() {
		return words;
	}

	/**
	 * @param _words the _words to set
	 */
	public Board setWords(List<Word> words) {
		this.words = words;
		return this;
	}
	
	public boolean isIntersection(int x, int y) {
		Boolean b = intersections.get(new Coordinate(x,y));
		return b != null && b == true;
	}
	
	public void markIntersection(int x, int y, Direction d, int offset) {
		if (d == Direction.HORIZONTAL) {
			markIntersection(x+offset, y);
		} else {
			markIntersection(x, y+offset);
		}
	}
	
	public void markIntersection(Coordinate pos, Direction d, int offset) {
		markIntersection(pos.x, pos.y, d, offset);
	}
	
	public void markIntersection(int x, int y) {
		
		markIntersection(new Coordinate(x, y));
	}
	
	public void markIntersection(Coordinate pos) {
		
		intersections.put(pos, true);
	}
	
	public void clearIntersection(int x, int y, Direction d, int offset) {
		if (d == Direction.HORIZONTAL) {
			clearIntersection(x+offset, y);
		} else {
			clearIntersection(x, y+offset);
		}
	}
	
	public void clearIntersection(Coordinate pos, Direction d, int offset) {
		clearIntersection(pos.x, pos.y, d, offset);
	}
	
	public void clearIntersection(int x, int y) {
		
		clearIntersection(new Coordinate(x, y));
	}
	
	public void clearIntersection(Coordinate pos) {
		
		intersections.put(pos, false);
	}
	
	
	
	public void addCharacter(Character c) {
		
		characters.put(c, characters.get(c) + 1);
	}
	
	public void removeCharacter(Character c) {
		
		characters.put(c, characters.get(c) - 1);
	}
	
	/**
	 * @return the _characters
	 */
	public Map<Character, Integer> getCharacters() {
		return characters;
	}

	/**
	 * @param _characters the _characters to set
	 */
	public Grid setCharacters(Map<Character, Integer> _characters) {
		this.characters = _characters;
		return this;
	}
	
	public boolean canHaveWord(String word, Coordinate pos, Direction direction) {
		
		
		if (pos.x < 0 || pos.y < 0) {
			return false;
		}
		if (pos.x >= GRID_SIZE || pos.y >= GRID_SIZE) {
			return false;
		}
		if (direction == Direction.HORIZONTAL) {
			if (pos.x+word.length() > GRID_SIZE) {
				return false;
			}
		} else {
			if (pos.y+word.length() > GRID_SIZE) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean canHaveWord(Word word) {
		return canHaveWord(word.word, word.vec.pos, word.vec.dir);
	}
	
	public void addWord(Word word) {
		words.add(word);
	}
	
	public Word removeWord(Word word) {
		words.remove(word);
		return word;
	}
	
}
