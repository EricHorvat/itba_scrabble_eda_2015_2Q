package eda.scrabble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import eda.scrabble.Game.Direction;
import eda.scrabble.Game.GameParameters;
import eda.scrabble.Game.LetterXY;
import eda.scrabble.Game.WordXY;
import eda.scrabble.file.InputData;

public class ExactGame extends Game {
	
	Grid bestGrid;
	
	int numberOfLetters;
	
	Map<Integer, Boolean> visitedGrids; 
	
	public ExactGame(GameParameters params) {
		super(params);
		
		visitedGrids = new HashMap<Integer, Boolean>();
	}
	
	private void possibleSolution1(List<LetterXY> used, int score) {
		
		String aux = null;
		WordXY toAdd = null;
		
		LetterXY letter = null;
		
		if (DEBUG) printUsed(used);
		
		int sum = 0;
		for (Entry<Character, Integer> e : grid.characters.entrySet()) {
			if (e.getValue() > 0)
				sum += e.getValue()*VALUE_MAP.get(e.getKey());
		}
		
		// Signinfica que no me quedan mas letras
		// Podamos
		if (sum == 0)
			return;
		
		// Backup used letters for restauration later
		List<LetterXY> backup = new ArrayList<LetterXY>(grid.size()*grid.size());
		for (LetterXY l : used)
			backup.add(l);
		
		// Loop through @{used}. It contains the letters that will be analyzed
		// Basically Letters\IntersectedLetters
		for (int i = 0; i < used.size(); i++) {
		
			letter = used.get(i);
			
			do {
			
			// Lookup all words available for inserting with letter=used.get(i)
//			while (true) {
				
				// Show available chars
				if (DEBUG) {
					List<Character> l = getAvailableChars();
					System.out.println("("+l.size()+"/"+numberOfLetters+"/"+(numberOfLetters-l.size())+"): "+l + " " + grid.characters);
				}
				
				// Character in the intersection should be added to available chars
				// since it isnt available but next word will contain this letter
				//grid.addCharacter(letter.c);
				
				if (DEBUG) System.out.println("Adding " + letter.c + " for search");
				
				// SELECT word FROM dictionary WHERE
				//     word.toCharArray().isContainedIn(@{characters})
				// AND length <= @{MAX_LENGTH_WORD}
				// AND word.indexOf(@{letter.c}) != -1
				// AND word > aux -- Viene despues en el Trie
				//
				// Busca la proxima palabra despues de aux que me alcancen los caracteres y tenga el caracter
				// letter.c
				//
				// Si encuentra la palabra, me remueve sus caracters de characters
				aux = grid.getDictionary().bestLimitedOptionAfter(grid.characters, MAX_LENGTH_WORD, letter.c, aux);
				
				Coordinate letterIndex2 = letter.word.pos;//letter.word.word.indexOf(letter.c);
				
				if (DEBUG) {
					if (letter.word.direction == Direction.HORIZONTAL)
						System.out.println("/SEARCH/"+letter.word.word+"/"+ letter.c + "/"+aux+"("+(letterIndex2.x+letter.pos)+","+(letterIndex2.y)+")");
					else
						System.out.println("/SEARCH/"+letter.word.word+"/"+ letter.c + "/"+aux+"("+letterIndex2.x+","+(letterIndex2.y+letter.pos)+")");
				}
				if (DEBUG) System.out.println("Removing " + letter.c + ". Already searched");
				
				// Since we added letter.c we should remove it
				//grid.removeCharacter(letter.c);
				
				// Si aux == null ==> con este caracter no hay mas palabras para buscar
				if (aux != null) {
					
					// No hay mas palabras con esta letra (letter.c)
					// ==> pasamos a la proxima
//					break;
//				}
				
				
					// Hacemos la matematica para que en la grilla se coloque todo prolijo
					// y en su lugar
					int intersectionIndex = aux.indexOf(letter.c);
					int letterIndex = letter.pos;
					//letter.word.word.indexOf(letter.c);
					
					// Marcamos a la interseccion como lugar ocupado
					if (letter.word.direction == Direction.HORIZONTAL) {
						grid.markOccupied(letter.word.pos.x+letterIndex, letter.word.pos.y);
					} else {
						grid.markOccupied(letter.word.pos.x, letter.word.pos.y+letterIndex);
					}
					
					// Attempt to add word
					try {
						if (letter.word.direction == Direction.HORIZONTAL) {
							toAdd = addWord(letter.word.pos.x+letterIndex, letter.word.pos.y-intersectionIndex, Direction.VERTICAL, aux);
						} else {
							toAdd = addWord(letter.word.pos.x-intersectionIndex, letter.word.pos.y+letterIndex, Direction.HORIZONTAL, aux);
						}
						
					} catch (AddWordException ex) {
						// Reseteamos porque la palabra no se pudo agregar
						if (DEBUG) System.out.println(aux + " was not added. " + ex.getMessage());
						
						// Marcamos el lugar que habiamos marcado como ocupado
						// como libre porque no agregamos la palabra
						if (letter.word.direction == Direction.HORIZONTAL) {
							grid.markAvailable(letter.word.pos.x+letterIndex, letter.word.pos.y);
						} else {
							grid.markAvailable(letter.word.pos.x, letter.word.pos.y+letterIndex);
						}
						
						grid.removeCharacter(letter.c);
						
						// Seguimos y probamos si la proxima palabra calza en este lugar
						continue;
					}
					
	//				removeCharacter(letter.c);
					
					// Sacamos a @{letter} de @{used} para que cuando entre en la recursiva
					// No se ponga a buscar palabras para calzar en la interseccion
					used.remove(letter);
					
					
					// Nos fijamos si este es mejor tablero que el anterior mejor
					// De ser asi actualizamos
					score = grid.getScore();
					if (score > maxScore) {
						maxScore = score;
						bestGrid = new Grid(grid);
					}
					
					//TODO: Aca cuando implementemos Ant y toda la bola habria que preguntar
					//     por la opcion --display 
					// Mostramos el tablero
					if (DEBUG) {
						grid.print();
					} else {
						if (ANT && params.isVisual())
							grid.printSimple();
					}
					
					
					// Agregamos los caracteres que en la recursiva se van a usar para buscar
					// mas palabras
					for (int j = 0; j < aux.length(); j++) {
						Character c = (Character)aux.charAt(j);
						if (j != intersectionIndex) {
							if (toAdd.direction == Direction.HORIZONTAL) {
								used.add(new LetterXY(toAdd, c, j));
							} else {
								used.add(new LetterXY(toAdd, c, j));
							}
						}
					}
					
					int hc = grid.hashCode();
					Boolean visited = visitedGrids.get(hc); 
					
					if (visited == null || visited == false) {
					
						visitedGrids.put(hc, true);
						
						// Perform recursive call to same method
						possibleSolution1(used, score);
					} else {
						
						if (DEBUG) System.out.println("already visited");
						
					}
					
					
					
					/*
					 * #################
					 * --- Cleanup Stage
					 * #################
					 */
					
					
					// Borramos la palabra que acabamos de agregar al tablero
					removeWord(toAdd);
					
					// Marcamos la interseccion como disponible porque ya que sacamos la palabra
					// no hay mas interseccion
					if (letter.word.direction == Direction.HORIZONTAL) {
						grid.markAvailable(letter.word.pos.x+letterIndex, letter.word.pos.y);
					} else {
						grid.markAvailable(letter.word.pos.x, letter.word.pos.y+letterIndex);
					}
					
					
					// Restauramos la informacion para que el proximo ciclo haga su trabajo
					used.clear();
					for (LetterXY l: backup)
						used.add(l);
					
					if (DEBUG) {
						System.out.println("After rec call");
						grid.print();
					}
				}
			} while (aux != null);
			
			// No deberia suceder, pero chequeamos igual por si las moscas
			if (letter.c != used.get(i).c) {
				System.err.println("######################\n---------------Mismatch\n#################");
				throw new IllegalAccessError(letter.c + " should be equal to " + used.get(i).c);
			}
			
		}
		
	}
	
	private void cleanBoard() {
	// Clean board
		while (words.size() > 0)
			removeWord(words.get(words.size()-1));
	}
	
	/**
	 * Places each of the words in all posible starting positions
	 * And calls the solver methods.
	 */
	@Override
	public void solve() {
		maxScore = 0;
		
		bestGrid = new Grid(grid.characters);
		
		List<String> allWords = grid.getDictionary().getWords();
		
		// Reservo a lo sumo grid.size()^2 de letras
		List<LetterXY> l = new ArrayList<LetterXY>(grid.size()*grid.size());
		
		WordXY tmp = null;
		
		numberOfLetters = getAvailableChars().size();
		
		if (DEBUG) {
			System.out.println("Initial Letters: " + numberOfLetters);
		}
		
		for (String w : allWords) {
			
			int x = grid.size()/2-w.length()+1;
			int y = grid.size()/2;
			
			// Probamos todas las posiciones posibles en el eje x
			for (int i = x; i <= grid.size()/2+1; i++) {
				
				cleanBoard();
				
				if (DEBUG) {
					System.out.println("########################################");
					System.out.println("------------ Starting from scratch");
					System.out.println("########################################");
				}
				if (DEBUG) {
					List<Character> li = getAvailableChars();
					System.out.println("Available chars("+li.size()+"): " + li);
					
					if (numberOfLetters != li.size()) {
						System.out.println("Inconsistency. Quiting now");
						return;
					}
					
				}
				
				// 	Throws Exception but board should be empty
				try {
					tmp = addWord(i, y, Direction.HORIZONTAL, w);
				} catch (AddWordException e1) {
					// No llega nunca el tablero esta siempre vacio
				}
				
				if (DEBUG) System.out.println("Printing initial board");
				if (DEBUG) grid.print();
				
				for (int j = 0; j < w.length(); j++) {
					// Mark characters as used
					grid.removeCharacter((Character)w.charAt(j));
					l.add(new LetterXY(tmp, (Character)w.charAt(j), j));
				}
				
				possibleSolution1(l, 0);
				
				
				l.clear();
			}
			
		}
		
//		if (DEBUG) System.out.println("Max Score is: " + maxScore);
		System.out.println("Max Score is: " + maxScore);
		bestGrid.print();
	}

}
