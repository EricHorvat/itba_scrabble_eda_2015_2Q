package eda.scrabble;

import java.util.ArrayList;
import java.util.List;

import eda.scrabble.Game.Direction;
import eda.scrabble.Game.WordXY;
import eda.scrabble.file.InputData;

public class LimitedTimeGame extends Game {

	private List<Dictionary> dictionaries;
	
	private final static int T = 10;
	
	public LimitedTimeGame() {
		super();
		
		dictionaries = new ArrayList<Dictionary>(5); // Hay 5 metodos greedy;
		
		dictionaries.add(dictionary);
		
		dictionaries.add(InputData.fillDictionary(
				DICTIONARY_FILENAME,
				InputData.DictionaryFillStrategy.NONE,
				characters));
		dictionaries.add(InputData.fillDictionary(
				DICTIONARY_FILENAME,
				InputData.DictionaryFillStrategy.LOWEST_VALUE,
				characters));
		dictionaries.add(InputData.fillDictionary(
				DICTIONARY_FILENAME,
				InputData.DictionaryFillStrategy.LOWEST_OCURRENCY,
				characters));
		dictionaries.add(InputData.fillDictionary(
				DICTIONARY_FILENAME,
				InputData.DictionaryFillStrategy.HIGHEST_OCURRENCY,
				characters));
	}
	
	private void approximate(Dictionary dictionary) {
		System.out.println("Aprox solution");
		
		String s = dictionary.bestFirstOption(characters, 7);
		
		if (DEBUG) System.out.println(s);
		
		int x = (grid.size()-s.length())/2;
		int y = grid.size()/2;
		
		// Throws Exception but board should be empty
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
			
//		hillClimb(i);
	}
	
	private void hillClimb2(Dictionary dictionary) {
		
		// Mis vecinos son sacarme y agregar todos los posibles
		// words[word.size-1] tengo la ultima palabra agregada
		// La tendria que sacar y buscar todas las posibles
		
		WordXY lastWord = words.get(words.size()-1);
		
		if (lastWord.direction == Direction.HORIZONTAL) {
			for (int j = lastWord.pos.x; j < lastWord.pos.x+lastWord.word.length(); j++) {
				markAvailable(j, lastWord.pos.y);
			}
		} else {
			for (int j = lastWord.pos.y; j < lastWord.pos.y+lastWord.word.length(); j++) {
				markAvailable(lastWord.pos.y, j);
			}
		}
		
		String aux = null;
		
		removeWord(lastWord);
		
		lastWord = words.get(words.size()-1);
		
		WordXY addWord;
		
		WordXY finalWordToAdd = null;
		
		for (int j = 0; j < lastWord.word.length(); j++) {
		
			Character intersectionChar = (Character)lastWord.word.charAt(j);
			
			while (true) {
				
				addCharacter(intersectionChar);
				
				aux = dictionary.bestLimitedOptionAfter(characters, MAX_LENGTH_WORD, intersectionChar, aux);
				
				removeCharacter(intersectionChar);
				
				if (aux == null) {
					
					
					break;
				}
				
				int intersectionIndex = aux.indexOf(intersectionChar);
				
				if (lastWord.direction == Direction.HORIZONTAL) {
					markOccupied(lastWord.pos.x+j, lastWord.pos.y);
				} else {
					markOccupied(lastWord.pos.x, lastWord.pos.y+j);
				}
				
				try {
					
					if (lastWord.direction == Direction.HORIZONTAL) {
						addWord = addWord(lastWord.pos.x+j, lastWord.pos.y-intersectionIndex, Direction.VERTICAL, aux);
					} else {
						addWord = addWord(lastWord.pos.x-intersectionIndex, lastWord.pos.y+j, Direction.HORIZONTAL, aux);
					}
					
				} catch (IllegalArgumentException ex) {
					
					System.out.println(ex.getMessage());
					
					
					for (int i = 0; i < aux.length(); i++)
						if (i != intersectionIndex)
							addCharacter((Character)aux.charAt(i));
					
					if (lastWord.direction == Direction.HORIZONTAL) {
						markAvailable(lastWord.pos.x+j, lastWord.pos.y);
					} else {
						markAvailable(lastWord.pos.x, lastWord.pos.y+j);
					}
					
					continue;
				}
				
				
				int score = grid.getScore();
				
				if (score > maxScore) {
					maxScore = score;
					finalWordToAdd = addWord;
				}
				
				
				
				removeWord(addWord);
			}
		}
		
		if (finalWordToAdd == null) return;
		
		// Mark Occupied & addBestWord
		
		if (finalWordToAdd.direction == Direction.HORIZONTAL) {
		
			for (int i = finalWordToAdd.pos.x; i < finalWordToAdd.pos.x+finalWordToAdd.word.length(); i++) {
				grid.set(i, finalWordToAdd.pos.y, finalWordToAdd.word.charAt(i-finalWordToAdd.pos.x));
			}
		} else {
			for (int i = finalWordToAdd.pos.y; i < finalWordToAdd.pos.y+finalWordToAdd.word.length(); i++) {
				grid.set(finalWordToAdd.pos.x, i, finalWordToAdd.word.charAt(i-finalWordToAdd.pos.y));
			}
		}
		
		/*
		 * Pseudo codigo
		 * 
		 * t = 0
		 * vc = solution
		 * do
		 * 	elegir un vecino vn con probabilidad 1/( 1 + e^((vc-vn)/T) )
		 * while (t < tmax)
		 * 
		 */
		
	}
	
	/**
	 * Va agregando en profundidad por la primera palabra que puede conseguir
	 * @param word La palabra en la cual arrancar
	 */
	private void firstDepth(Dictionary dictionary, WordXY word) {
		if (word == null) return;
		
		String aux = null;
		
		WordXY toAdd = null;
		
		for (int i = 0; i < word.word.length(); i++) {
			
			Character intersectionChar = (Character)word.word.charAt(i);
			
			addCharacter(intersectionChar);
			
			aux = dictionary.bestFirstLimitedOption(characters, MAX_LENGTH_WORD, intersectionChar);
			
			removeCharacter(intersectionChar);
			
			if (aux == null) {
			
				break;
			}
			
			int intersectionIndex = aux.indexOf(intersectionChar);
			
			if (word.direction == Direction.HORIZONTAL) {
				markOccupied(word.pos.x+i, word.pos.y);
			} else {
				markOccupied(word.pos.x, word.pos.y+i);
			}
			
			try {
				if (word.direction == Direction.HORIZONTAL) {
					toAdd = addWord(word.pos.x+i, word.pos.y-intersectionIndex, Direction.VERTICAL, aux);
				} else {
					toAdd = addWord(word.pos.x-intersectionIndex, word.pos.y+i, Direction.HORIZONTAL, aux);
				}
			} catch (IllegalArgumentException ex) {
				
				for (int j = 0; j < aux.length(); j++) {
					if (j != intersectionIndex)
						addCharacter((Character)aux.charAt(j));
				}
				
				if (word.direction == Direction.HORIZONTAL) {
					markAvailable(word.pos.x+i, word.pos.y);
				} else {
					markAvailable(word.pos.x, word.pos.y+i);
				}
				
				continue;
			}
			
			int score = grid.getScore();
			if (score > maxScore)
				maxScore = score;
			
			grid.print();
			
			firstDepth(dictionary, toAdd);
			
			// Cortamos no me importa seguir mas
			return;
			
		}
		
		System.out.println("Max Score:" + maxScore);
		
	}
	
	@Override
	public void solve() {
		
		long start = System.nanoTime();
		
		maxScore = 0;
		
		int best = 0, i = 0;
		
		for (Dictionary dictionary : dictionaries) {
		
			// Cant be null. Otherwise dictionary would be empty 
			String firstWord = dictionary.bestFirstOption(characters, MAX_LENGTH_WORD);
			
			int x = (grid.size()-firstWord.length())/2;
			int y = grid.size()/2;
			
			WordXY word = addWord(x, y, Direction.HORIZONTAL, firstWord);
			
			System.out.println("##############");
			System.out.println("--- " + dictionary.getDictionaryFillStrategy());
			System.out.println("##############");
			
			int score = maxScore;
			
			firstDepth(dictionary, word);
			
			hillClimb2(dictionary);
			
			if (maxScore > score)
				best = i;
			
			while (words.size() > 0) {
				removeWord(words.get(words.size()-1));
			}
			i++;
		}
		
		long end = System.nanoTime() - start; 
		System.out.println("Run Time: " + end/1000000.0 + "ms");
		
	}

}
