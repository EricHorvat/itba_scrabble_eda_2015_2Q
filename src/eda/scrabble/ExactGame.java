package eda.scrabble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ExactGame extends Game {
	
	Board bestGrid;
	
	int numberOfLetters;
	
	Map<Integer, Boolean> visitedGrids;
	
	int n = 0;
	
	public ExactGame(GameParameters params) {
		super(params);
		
		
		visitedGrids = new HashMap<Integer, Boolean>();
	}
	
	private void possibleSolution1(List<Letter> willVisitLetters, int score) {
		
		String aux = null;
		Word toAdd = null;
		
		Letter currentLetter = null;
		
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
		List<Letter> backup = new ArrayList<Letter>(grid.size()*grid.size());
		for (Letter l : willVisitLetters)
			backup.add(l);
		
		// Loop through @{used}. It contains the letters that will be analyzed
		// Basically Letters\IntersectedLetters
		// Recorremos todas las letras a visitar
		for (int i = 0; i < willVisitLetters.size(); i++) {
		
			currentLetter = willVisitLetters.get(i);
			
			
			// Por cada una de las letras buscamos todas las palabras posibles
			do {
			
			// Lookup all words available for inserting with letter=used.get(i)
//			while (true) {
				
				// Show available chars
//				if (DEBUG) {
//					List<Character> lj = getAvailableChars(grid.characters);
//					System.out.println("("+lj.size()+"/"+numberOfLetters+"/"+(numberOfLetters-lj.size())+"): ");
//					if (grid.getUsed() != (numberOfLetters-lj.size())) {
//						grid.print();
//						System.out.println();
//						System.exit(0);
//					}
//				}
				
				int u = grid.getUsed();
				int s = getAvailableChars(grid.characters).size();
				
				if (u != (numberOfLetters-s)) {
					List<Character> lj = getAvailableChars(grid.characters);
					System.out.println("("+lj.size()+"/"+numberOfLetters+"/"+(numberOfLetters-lj.size())+"): ");
					grid.print();
					System.out.println("Early Mismatch");
					System.out.println("("+s+"/"+numberOfLetters+"/"+(numberOfLetters-s)+"): ");
					System.exit(0);
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
				aux = grid.getDictionary().bestLimitedOptionAfter(grid.characters, MAX_LENGTH_WORD, currentLetter.c, aux);
				
//				Coordinate letterIndex2 = currentLetter.word.vec.pos;
				
//				if (DEBUG) {
//					if (currentLetter.word.vec.dir== Direction.HORIZONTAL)
//						System.out.println("/SEARCH/"+currentLetter.word.word+"/"+ currentLetter.c + "/"+aux+"("+(letterIndex2.x+currentLetter.pos)+","+(letterIndex2.y)+")");
//					else
//						System.out.println("/SEARCH/"+currentLetter.word.word+"/"+ currentLetter.c + "/"+aux+"("+letterIndex2.x+","+(letterIndex2.y+currentLetter.pos)+")");
//				}
				
				// Si aux == null ==> con este caracter no hay mas palabras para buscar
				if (aux != null) {
				
				
					// Hacemos la matematica para que en la grilla se coloque todo prolijo
					// y en su lugar
					int letterIndex = currentLetter.pos;
					int intersectionIndex = aux.indexOf(currentLetter.c);
					
					
					// Probamos todas las posiciones posibles de la palabra
					while (intersectionIndex >= 0) {
					
//						StringBuilder bugFix = new StringBuilder(aux);
//						bugFix.setCharAt(intersectionIndex, Grid.EMPTY_SPACE);
						
						//letter.word.word.indexOf(letter.c);
						
						// Marcamos a la interseccion como lugar ocupado
						grid.markIntersection(currentLetter.word.vec.pos, currentLetter.word.vec.dir, letterIndex);
						
						if (currentLetter.word.vec.dir == Direction.HORIZONTAL) {
							
							toAdd = new Word(aux, new Vector(
									new Coordinate(
											currentLetter.word.vec.pos.x+letterIndex,
											currentLetter.word.vec.pos.y-intersectionIndex),
									Direction.VERTICAL),
								intersectionIndex);
							
						} else {
							
							toAdd = new Word(aux, new Vector(
									new Coordinate(
											currentLetter.word.vec.pos.x-intersectionIndex,
											currentLetter.word.vec.pos.y+letterIndex),
									Direction.HORIZONTAL),
								intersectionIndex);
							
						}
						
						if (!addWord(toAdd, grid)) {
							
						// Marcamos el lugar que habiamos marcado como ocupado
							// como libre porque no agregamos la palabra
							grid.clearIntersection(currentLetter.word.vec.pos, currentLetter.word.vec.dir, letterIndex);
							
							// Seguimos y probamos si la proxima palabra calza en este lugar
							intersectionIndex = aux.indexOf(currentLetter.c, intersectionIndex+1);
							
							continue;
							
							
						} else {
							
							// Sacamos a @{letter} de @{used} para que cuando entre en la recursiva
							// No se ponga a buscar palabras para calzar en la interseccion
							willVisitLetters.remove(currentLetter);
							
							
							// Nos fijamos si este es mejor tablero que el anterior mejor
							// De ser asi actualizamos
							score = grid.getScore();
							if (score > maxScore) {
								maxScore = score;
								bestGrid = new Board(grid);
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
									
									// Consumimos el caracter
									grid.removeCharacter(c);
									// Lo ponemos en la lista de a visitar
									
									if (toAdd.vec.dir == Direction.HORIZONTAL) {
										if (grid.get(toAdd.vec.pos.x+j, toAdd.vec.pos.y+1) == Grid.EMPTY_SPACE &&
												grid.get(toAdd.vec.pos.x+j, toAdd.vec.pos.y-1) == Grid.EMPTY_SPACE) {
											if (DEBUG) System.out.print(c + " ");
											willVisitLetters.add(new Letter(toAdd, c, j));
										}
									} else {
										if (grid.get(toAdd.vec.pos.x+1, toAdd.vec.pos.y+j) == Grid.EMPTY_SPACE &&
												grid.get(toAdd.vec.pos.x-1, toAdd.vec.pos.y+j) == Grid.EMPTY_SPACE) {
											if (DEBUG) System.out.print(c + " ");
											willVisitLetters.add(new Letter(toAdd, c, j));
										}
									}
								}
							}
							if (DEBUG) System.out.println();
							
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
							
							intersectionIndex = aux.indexOf(currentLetter.c, intersectionIndex+1);
							
							// Borramos la palabra que acabamos de agregar al tablero
							removeWord(toAdd);
							
							// Marcamos la interseccion como disponible porque ya que sacamos la palabra
							// no hay mas interseccion
							grid.clearIntersection(currentLetter.word.vec.pos, currentLetter.word.vec.dir, letterIndex);
							
							// Restauramos la informacion para que el proximo ciclo haga su trabajo
							willVisitLetters.clear();
							for (Letter l: backup)
								willVisitLetters.add(l);
							
							if (DEBUG) {
								System.out.println("After rec call");
//								grid.print();
							}
						}
					}
						
				}
				
			} while (aux != null);
			
			// No deberia suceder, pero chequeamos igual por si las moscas
			if (currentLetter.c != willVisitLetters.get(i).c) {
				System.err.println("######################\n---------------Mismatch\n#################");
				throw new IllegalAccessError(currentLetter.c + " should be equal to " + willVisitLetters.get(i).c);
			}
			
		}
		
	}
	
	private void cleanBoard() {
	// Clean board
		while (grid.getWords().size() > 0) { 
			removeWord(grid.getWords().get(grid.getWords().size()-1));
		}
	}
	
	/**
	 * Places each of the words in all posible starting positions
	 * And calls the solver methods.
	 */
	@Override
	public void solve() {
		
		maxScore = 0;
		
		bestGrid = new Board(grid.characters);
		
		List<String> allWords = grid.getDictionary().getWords();
		
		// Reservo a lo sumo grid.size()^2 de letras
		List<Letter> willVisitLetters = new ArrayList<Letter>(grid.size()*grid.size());
		
		Word tmp = null;
		
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
				
				tmp = new Word(w, new Vector(new Coordinate(i, y), Direction.HORIZONTAL), -1);
				
				addWord(tmp, grid);
				
				// 	Throws Exception but board should be empty
//				AddWordResult result = addWord(i, y, Direction.HORIZONTAL, w);
				
//				if (!result.success) {
					
					// No llega nunca el tablero esta siempre vacio
//					grid.print();
//					System.out.println(result.msg);
//					return;
//				}
				
//				tmp = result.word;
				
				if (DEBUG) System.out.println("Printing initial board");
				if (DEBUG) grid.print();
				
				for (int j = 0; j < w.length(); j++) {
					// Mark characters as used
					grid.removeCharacter((Character)w.charAt(j));
					willVisitLetters.add(new Letter(tmp, (Character)w.charAt(j), j));
				}
				
//				grid.print();
				
//				visitedGrids.clear();
				
				possibleSolution1(willVisitLetters, 0);
				
				
				willVisitLetters.clear();
			}
			
		}
		
//		if (DEBUG) System.out.println("Max Score is: " + maxScore);
//		System.out.println("Max Score is: " + maxScore);
//		System.out.println("Max Score is: " + bestGrid.getScore());
		bestGrid.printSimple();
//		}
//		try {
//			bestGrid.printSimpleDump(params.outputFileName);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

}
