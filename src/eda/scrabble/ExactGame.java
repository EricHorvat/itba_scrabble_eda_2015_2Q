package eda.scrabble;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
	
	int n = 0;
	
	public ExactGame(GameParameters params) {
		super(params);
		
		
		visitedGrids = new HashMap<Integer, Boolean>();
	}
	
	private void possibleSolution1(List<LetterXY> willVisitLetters, int score) {
		
		String aux = null;
		WordXY toAdd = null;
		
		LetterXY letter = null;
		
		if (DEBUG) printUsed(willVisitLetters);
		
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
		for (LetterXY l : willVisitLetters)
			backup.add(l);
		
		// Loop through @{used}. It contains the letters that will be analyzed
		// Basically Letters\IntersectedLetters
		// Recorremos todas las letras a visitar
		for (int i = 0; i < willVisitLetters.size(); i++) {
		
			letter = willVisitLetters.get(i);
			
			
			// Por cada una de las letras buscamos todas las palabras posibles
			do {
			
			// Lookup all words available for inserting with letter=used.get(i)
//			while (true) {
				
				// Show available chars
				if (DEBUG) {
					List<Character> lj = getAvailableChars(grid.characters);
					System.out.println("("+lj.size()+"/"+numberOfLetters+"/"+(numberOfLetters-lj.size())+"): "+lj + " " + grid.characters);
					if (grid.getUsed() != (numberOfLetters-lj.size())) {
						grid.print();
						return;
					}
				}
				
				int u = grid.getUsed();
				int s = getAvailableChars(grid.characters).size();
				
				if (u != (numberOfLetters-s)) {
					grid.print();
					System.out.println("Early Mismatch");
					System.out.println("("+s+"/"+numberOfLetters+"/"+(numberOfLetters-s)+"): ");
					return;
				}
				
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
				
				Coordinate letterIndex2 = letter.word.pos;
				
				if (DEBUG) {
					if (letter.word.direction == Direction.HORIZONTAL)
						System.out.println("/SEARCH/"+letter.word.word+"/"+ letter.c + "/"+aux+"("+(letterIndex2.x+letter.pos)+","+(letterIndex2.y)+")");
					else
						System.out.println("/SEARCH/"+letter.word.word+"/"+ letter.c + "/"+aux+"("+letterIndex2.x+","+(letterIndex2.y+letter.pos)+")");
				}
				
				// Si aux == null ==> con este caracter no hay mas palabras para buscar
				if (aux == null) {
					
				} else {
				
				
					// Hacemos la matematica para que en la grilla se coloque todo prolijo
					// y en su lugar
					int letterIndex = letter.pos;
					int intersectionIndex = aux.indexOf(letter.c);
					
					
					// Probamos todas las posiciones posibles de la palabra
					while (intersectionIndex >= 0) {
					
						StringBuilder bugFix = new StringBuilder(aux);
						bugFix.setCharAt(intersectionIndex, Grid.EMPTY_SPACE);
						
						//letter.word.word.indexOf(letter.c);
						
						// Marcamos a la interseccion como lugar ocupado
						if (letter.word.direction == Direction.HORIZONTAL) {
							grid.markOccupied(letter.word.pos.x+letterIndex, letter.word.pos.y);
						} else {
							grid.markOccupied(letter.word.pos.x, letter.word.pos.y+letterIndex);
						}
						
						// Attempt to add word
						AddWordResult result;
						if (letter.word.direction == Direction.HORIZONTAL) {
							result = addWord(letter.word.pos.x+letterIndex, letter.word.pos.y-intersectionIndex, Direction.VERTICAL, bugFix.toString());
						} else {
							result = addWord(letter.word.pos.x-intersectionIndex, letter.word.pos.y+letterIndex, Direction.HORIZONTAL, bugFix.toString());
						}
						
						if (!result.success) {
							// Reseteamos porque la palabra no se pudo agregar
							if (DEBUG) System.out.println(aux + " was not added. " + result.msg);
							
							// Marcamos el lugar que habiamos marcado como ocupado
							// como libre porque no agregamos la palabra
							if (letter.word.direction == Direction.HORIZONTAL) {
								grid.markAvailable(letter.word.pos.x+letterIndex, letter.word.pos.y);
							} else {
								grid.markAvailable(letter.word.pos.x, letter.word.pos.y+letterIndex);
							}
							
							// Seguimos y probamos si la proxima palabra calza en este lugar
							
							intersectionIndex = aux.indexOf(letter.c, intersectionIndex+1);
							
							continue;
						} else {
							
							toAdd = result.word;
							
							// Sacamos a @{letter} de @{used} para que cuando entre en la recursiva
							// No se ponga a buscar palabras para calzar en la interseccion
							willVisitLetters.remove(letter);
							
							
							// Nos fijamos si este es mejor tablero que el anterior mejor
							// De ser asi actualizamos
							score = grid.getScore();
							if (score > maxScore) {
								maxScore = score;
								bestGrid = new Grid(grid);
							}
							 
							// Mostramos el tablero
							if (DEBUG) {
								grid.print();
							} else {
								if (ANT && params.isVisual())
									grid.printSimple();
							}
							
							// Agregamos los caracteres que en la recursiva se van a usar para buscar
							// mas palabras
							if (DEBUG) System.out.print("Adding to Letters: ");
							for (int j = 0; j < aux.length(); j++) {
								Character c = (Character)aux.charAt(j);
								if (j != intersectionIndex) {
									if (DEBUG) System.out.print(c + " ");
									grid.removeCharacter(c);
									willVisitLetters.add(new LetterXY(toAdd, c, j));
								}
							}
							if (DEBUG) System.out.println();
							
							n++;
							if (n > 10000) {
								visitedGrids.clear();
								n = 0;
							}
							
							int hc = grid.hashCode();
							Boolean visited = visitedGrids.get(hc);
							
							if (visited == null || visited == false) {
								
								visitedGrids.put(hc, true);
								
								// Perform recursive call to same method
								possibleSolution1(willVisitLetters, score);
							} else {
								if (DEBUG) System.out.println("already visited");
								
//								grid.print();
								
							}
							
							
							
							/*
							 * #################
							 * --- Cleanup Stage
							 * #################
							 */
							
							int intersectionIndex2 = aux.indexOf(letter.c, intersectionIndex+1);
							
							intersectionIndex = intersectionIndex2;
							
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
							willVisitLetters.clear();
							for (LetterXY l: backup)
								willVisitLetters.add(l);
							
							if (DEBUG) {
								System.out.println("After rec call");
								grid.print();
							}
						}
						
					}
				}
				
			} while (aux != null);
			
			// No deberia suceder, pero chequeamos igual por si las moscas
			if (letter.c != willVisitLetters.get(i).c) {
				System.err.println("######################\n---------------Mismatch\n#################");
				throw new IllegalAccessError(letter.c + " should be equal to " + willVisitLetters.get(i).c);
			}
			
		}
		
	}
	
	private void cleanBoard() {
	// Clean board
		while (words.size() > 0) { 
			removeWord(words.get(words.size()-1));
		}
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
		List<LetterXY> willVisitLetters = new ArrayList<LetterXY>(grid.size()*grid.size());
		
		WordXY tmp = null;
		
		numberOfLetters = getAvailableChars().size();
		
		if (DEBUG) {
			System.out.println("Initial Letters: " + numberOfLetters);
		}
		
		for (String w : allWords) {
			
			int x = grid.size()/2-w.length()+1;
			int y = grid.size()/2;
			
			// Probamos todas las posiciones posibles en el eje x
			for (int i = x; i <= grid.size()/2; i++) {
				
				cleanBoard();
				
				if (DEBUG) {
					System.out.println("########################################");
					System.out.println("------------ Starting from scratch");
					System.out.println("########################################");
				}
				List<Character> li = getAvailableChars(grid.characters);
				if (DEBUG) {
					
					System.out.println("Available chars("+li.size()+"): " + li);
					
				}
				
				if (numberOfLetters != li.size()) {
					System.out.println("Inconsistency. Quiting now");
					return;
				}
				
				// 	Throws Exception but board should be empty
				AddWordResult result = addWord(i, y, Direction.HORIZONTAL, w);
				
				if (!result.success) {
					
					// No llega nunca el tablero esta siempre vacio
					grid.print();
					System.out.println(grid.getIntersections());
					System.out.println(result.msg);
					return;
				}
				
				tmp = result.word;
				
				if (DEBUG) System.out.println("Printing initial board");
				if (DEBUG) grid.print();
				
				for (int j = 0; j < w.length(); j++) {
					// Mark characters as used
					grid.removeCharacter((Character)w.charAt(j));
					willVisitLetters.add(new LetterXY(tmp, (Character)w.charAt(j), j));
				}
				
				grid.print();
				
				visitedGrids.clear();
				
				possibleSolution1(willVisitLetters, 0);
				
				
				willVisitLetters.clear();
			}
			
		}
		
//		if (DEBUG) System.out.println("Max Score is: " + maxScore);
		System.out.println("Max Score is: " + maxScore);
		System.out.println("Max Score is: " + bestGrid.getScore());
		bestGrid.print();
//		try {
//			bestGrid.printSimpleDump(params.outputFileName);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

}
