package eda.scrabble.boards.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eda.scrabble.elements.Coordinate;
import eda.scrabble.elements.Direction;
import eda.scrabble.elements.Word;
import eda.scrabble.storage.Dictionary;

/**
 * Descripcion de un tablero para este problema.
 * @author martin
 *
 */
public class Board extends Grid {

	/**
	 * El diccionario que usa este tablero.
	 * Tenemos mas de un diccionario posible
	 * No es siempre el mismo
	 */
	private Dictionary dictionary;
	
	/**
	 * Tenemos un set de coordenadas que nos
	 * indican cuales son las coordenadas
	 * donde hay cruce de palabras
	 */
	protected Set<Coordinate> intersections;
	/**
	 * Los caracteres disponibles en este tablero
	 */
	protected Map<Character, Integer> characters;
	/**
	 * Las palabras que contiene este tablero
	 */
	private List<Word> words = new ArrayList<Word>();
	
	
	public Board(Map<Character, Integer> characters) {
		super();
		
		this.intersections = new HashSet<Coordinate>();
		this.characters = new HashMap<Character, Integer>(characters);
	}

	public Board(Board board) {
		super(board);
		// Nos quedamos con una referencia al diccionario
		this.dictionary = board.dictionary;
		this.intersections = new HashSet<Coordinate>(board.intersections);
		this.characters = new HashMap<Character, Integer>(board.characters);
		this.words = new ArrayList<Word>(board.words);
	}

	public Dictionary getDictionary() {
		return dictionary;
	}

	public Board setDictionary(Dictionary dictionary) {
		this.dictionary = dictionary;
		return this;
	}

	public List<Word> getWords() {
		return words;
	}
	
	public Board setWords(List<Word> words) {
		this.words = words;
		return this;
	}
	
	public Map<Character, Integer> getAvailableCharacters() {
		return characters;
	}
	
	/**
	 * Me dice si hay una interseccion en la coordenada (x,y)
	 * @param x 
	 * @param y
	 * @return true <==> si hay un cruce de palabras en esa coordenada
	 */
	public boolean isIntersection(int x, int y) {
		return intersections.contains(new Coordinate(x, y));
	}
	
	/**.
	 * Marca que hay un cruce de palabras en (x+offset,y) o en (x, y+offset)
	 * dependiendo de la direccion
	 * @param x
	 * @param y
	 * @param d
	 * @param offset
	 */
	public void markIntersection(int x, int y, Direction d, int offset) {
		if (d.isHorizontal()) {
			markIntersection(x+offset, y);
		} else {
			markIntersection(x, y+offset);
		}
	}
	
	/**
	 * Wrapper del metodo de arriba
	 * @param pos
	 * @param d
	 * @param offset
	 */
	public void markIntersection(Coordinate pos, Direction d, int offset) {
		markIntersection(pos.getX(), pos.getY(), d, offset);
	}
	
	/**
	 * Wrapper del metodo de abajo
	 * @param x
	 * @param y
	 */
	public void markIntersection(int x, int y) {
		
		markIntersection(new Coordinate(x, y));
	}
	
	/**
	 * Marca una interseccion en el cruce de palabras
	 * en la coordenada pos
	 * @param pos la coordenada que tiene un cruce
	 */
	public void markIntersection(Coordinate pos) {
		
		intersections.add(pos);
	}
	
	/**
	 * Borra una interseccion en la coordenanda dada
	 * @param x
	 * @param y
	 * @param d
	 * @param offset
	 */
	public void clearIntersection(int x, int y, Direction d, int offset) {
		if (d.isHorizontal()) {
			clearIntersection(x+offset, y);
		} else {
			clearIntersection(x, y+offset);
		}
	}
	
	/**
	 * Borra una interseccion en la coordenanda dada
	 * @param pos
	 * @param d
	 * @param offset
	 */
	public void clearIntersection(Coordinate pos, Direction d, int offset) {
		clearIntersection(pos.getX(), pos.getY(), d, offset);
	}
	
	/**
	 * Borra una interseccion en la coordenanda dada
	 * @param x
	 * @param y
	 */
	public void clearIntersection(int x, int y) {
		
		clearIntersection(new Coordinate(x, y));
	}
	
	/**
	 * Borra una interseccion en la coordenanda dada
	 * @param pos
	 */
	public void clearIntersection(Coordinate pos) {
		
		intersections.remove(pos);
	}
	
	
	/**
	 * Agrega un caracter a mis caracteres disponibles
	 * @param c
	 */
	public void addCharacter(Character c) {
		
		characters.put(c, characters.get(c) + 1);
	}
	
	/**
	 * Saca un caracter de mis caracteres disponibles
	 * @param c
	 */
	public void removeCharacter(Character c) {
		
		characters.put(c, characters.get(c) - 1);
	}

	
	public Grid setCharacters(Map<Character, Integer> _characters) {
		this.characters = _characters;
		return this;
	}
	
	/**
	 * Cheque si la palabra esta dentro de los rangos del tablero
	 * @param word La palabra a validar
	 * @param pos El punto de insercion
	 * @param direction La direccion de insercion
	 * @return true <==> la palabra entra en el tablero
	 */
	public boolean canHaveWord(String word, Coordinate pos, Direction direction) {
		
		
		if (pos.getX() < 0 || pos.getY() < 0) {
			return false;
		}
		if (pos.getX() >= GRID_SIZE || pos.getY() >= GRID_SIZE) {
			return false;
		}
		if (direction == Direction.HORIZONTAL) {
			if (pos.getX()+word.length() > GRID_SIZE) {
				return false;
			}
		} else {
			if (pos.getY()+word.length() > GRID_SIZE) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Wrapper del metodo de arriba
	 * @param word
	 * @return
	 */
	public boolean canHaveWord(Word word) {
		return canHaveWord(word.getWord(), word.getVector().getPosition(), word.getVector().getDirection());
	}
	
	/**
	 * Agrega una palabra a las palabras en este tablero (no visualmente)
	 * @param word
	 */
	public void addWord(Word word) {
		words.add(word);
	}
	
	/**
	 * Saca una palabra de las palabras de este tablero (no visualmente)
	 * @param word
	 * @return
	 */
	public Word removeWord(Word word) {
		words.remove(word);
		return word;
	}
	
	/**
	 * Obtiene la palabra que se inserto ultima
	 * @return
	 */
	public Word getLastlyAdded() {
		return words.get(words.size()-1);
	}
	
}
