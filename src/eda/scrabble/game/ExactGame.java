package eda.scrabble.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import eda.scrabble.boards.logic.Board;
import eda.scrabble.boards.logic.Grid;
import eda.scrabble.boards.visual.BoardDrawer;
import eda.scrabble.elements.Coordinate;
import eda.scrabble.elements.Direction;
import eda.scrabble.elements.Letter;
import eda.scrabble.elements.Vector;
import eda.scrabble.elements.Word;

public class ExactGame extends Game {
	
	Board bestGrid;
	
	Set<String> visitedBoards;
	
	int n = 0;
	
	public ExactGame(GameParameters params) {
		super(params);
		
		visitedBoards = new HashSet<String>();
	}
	
	/**
	 * Metodo recursivo que corre el algoritmo exacto que sigue la siguiente logica:
	 * Podas iniciales:
	 * 	1. Si la suma de los caracteres disponibles es 0 => se utilizaron todas las palabras
	 * 		=> el tablero es el mejor posible
	 *  2. Si la suma de los caracters restantes y el puntaje del tablero actual no llega
	 *  	a ser maxScore => con ese tablero no se puede alcanzar maxScore => Cortar esa rama
	 *  
	 * El procedimiento sigue de la siguiente manera:
	 * 	1. Por cada una de las letras donde se puede insertar una palabra se le pide al
	 * 		diccionario las palabras canditatas a ser insertadas
	 * 	2. Por cada una de estas palabras se trata de insertar en todas las posiciones
	 * 		disponibles
	 * 	3. Una vez que se inserto una palabra se repite el procedimiento recien mencionado
	 * 		agregando a las letras a visitar el letras recientemente agregadas
	 * 
	 * @param willVisitLetters Los caracteres a ser visitados para buscar palabras a insertar
	 * @param score El puntaje del tablero actual
	 * @return el exitCode de la operacion para cortar cuando se llega a un maximo o no se
	 *	puede seguir mas
	 */
	private int exactSolution(List<Letter> willVisitLetters, int score) {
		
		String aux = null;
		Word toAdd = null;
		
		Letter currentLetter = null;
		
		int sum = 0;
		for (Entry<Character, Integer> e : grid.getAvailableCharacters().entrySet()) {
			if (e.getValue() > 0) {
				sum += e.getValue()*CHARACTER_VALUES.get(e.getKey());
			}
		}
		
		// Signinfica que no me quedan mas letras
		// Podamos
		if (sum == 0) {
			return 1;
		}
		
		
		if (sum+score <= maxScore) {
			return 2;
		}
		
		// Backup used letters for restauration later
		List<Letter> backup = new ArrayList<Letter>(80);
		for (Letter l : willVisitLetters) {
			backup.add(l);
		}
		
		// Loop through @{used}. It contains the letters that will be analyzed
		// Basically Letters\IntersectedLetters
		// Recorremos todas las letras a visitar
		for (int i = 0; i < willVisitLetters.size(); i++) {
		
			currentLetter = willVisitLetters.get(i);
			
			
			// Por cada una de las letras buscamos todas las palabras posibles
			do {
				
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
				aux = grid.getDictionary().bestLimitedOptionAfter(grid.getAvailableCharacters(), MAX_LENGTH_WORD, currentLetter.getCharacter(), aux);
				
				// Si aux == null ==> con este caracter no hay mas palabras para buscar
				if (aux != null) {
				
				
					// Hacemos la matematica para que en la grilla se coloque todo prolijo
					// y en su lugar
					int letterIndex = currentLetter.getPosition();
					int intersectionIndex = aux.indexOf(currentLetter.getCharacter());
					
					
					// Probamos todas las posiciones posibles de la palabra
					while (intersectionIndex >= 0) {
					
//						Board nextBoard = new Board(board);
						
						
						// Marcamos a la interseccion como lugar ocupado
						grid.markIntersection(currentLetter.getWord().getVector().getPosition(), currentLetter.getWord().getVector().getDirection(), letterIndex);
						
						// Creamos la palabra a agregar
						if (currentLetter.getWord().getVector().getDirection().isHorizontal()) {
							
							toAdd = new Word(aux, new Vector(
									new Coordinate(
											currentLetter.getWord().getVector().getPosition().getX()+letterIndex,
											currentLetter.getWord().getVector().getPosition().getY()-intersectionIndex),
											currentLetter.getWord().getVector().getDirection().getOpposite()),
								intersectionIndex);
							
						} else {
							
							toAdd = new Word(aux, new Vector(
									new Coordinate(
											currentLetter.getWord().getVector().getPosition().getX()-intersectionIndex,
											currentLetter.getWord().getVector().getPosition().getY()+letterIndex),
											currentLetter.getWord().getVector().getDirection().getOpposite()),
								intersectionIndex);
							
						}
						
						// Nos fijamos si se agrego
						if (!BoardDrawer.getCurrentBoardDrawer().addWord(toAdd, grid)) {
							
						// Marcamos el lugar que habiamos marcado como ocupado
							// como libre porque no agregamos la palabra
							grid.clearIntersection(currentLetter.getWord().getVector().getPosition(), currentLetter.getWord().getVector().getDirection(), letterIndex);
							
							// Seguimos y probamos si la proxima palabra calza en este lugar
							intersectionIndex = aux.indexOf(currentLetter.getCharacter(), intersectionIndex+1);
							
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
							if (params.isVisual()) {
								BoardDrawer.getCurrentBoardDrawer().printUnstyled(grid);
							}
							
//							grid.printSimple();
							
							// Agregamos los caracteres que en la recursiva se van a usar para buscar
							// mas palabras
							for (int j = 0; j < aux.length(); j++) {
								Character c = (Character)aux.charAt(j);
								if (j != intersectionIndex) {
									
									// Consumimos el caracter
									grid.removeCharacter(c);
									
									// Lo ponemos en la lista de a visitar
									if (toAdd.getVector().getDirection().isHorizontal()) {
										if (grid.get(toAdd.getVector().getPosition().getX()+j, toAdd.getVector().getPosition().getY()+1) == Grid.EMPTY_SPACE &&
												grid.get(toAdd.getVector().getPosition().getX()+j, toAdd.getVector().getPosition().getY()-1) == Grid.EMPTY_SPACE) {
											willVisitLetters.add(new Letter(toAdd, c, j));
										}
									} else {
										if (grid.get(toAdd.getVector().getPosition().getX()+1, toAdd.getVector().getPosition().getY()+j) == Grid.EMPTY_SPACE &&
												grid.get(toAdd.getVector().getPosition().getX()-1, toAdd.getVector().getPosition().getY()+j) == Grid.EMPTY_SPACE) {											
											willVisitLetters.add(new Letter(toAdd, c, j));
										}
									}
								}
							}
							
							if (!visitedBoards.contains(grid.toString())) {
								visitedBoards.add(grid.toString());
								
								int exitCode = exactSolution(willVisitLetters, score);
								if (exitCode == 1) {
									return 1;
								}
							}
							
							
							/*
							 * #################
							 * --- Cleanup Stage
							 * #################
							 */
							
							intersectionIndex = aux.indexOf(currentLetter.getCharacter(), intersectionIndex+1);
							
							// Borramos la palabra que acabamos de agregar al tablero
							BoardDrawer.getCurrentBoardDrawer().removeWord(toAdd, grid);
							
							// Marcamos la interseccion como disponible porque ya que sacamos la palabra
							// no hay mas interseccion
							grid.clearIntersection(
									currentLetter.getWord().getVector().getPosition(),
									currentLetter.getWord().getVector().getDirection(),
									letterIndex);
							
							// Restauramos la informacion para que el proximo ciclo haga su trabajo
							willVisitLetters.clear();
							for (Letter l: backup) {
								willVisitLetters.add(l);
							}
						}
					}
						
				}
				
			} while (aux != null);
			
		}
		
		return 0;
		
	}
	
	/**
	 * Este metodo saca todas las palabras que quedaron en el tablero
	 * Y limpia las intersecciones que quedaron marcadas
	 * Para que pueda ser reutilizado
	 */
	private void cleanBoard() {
	// Clean board
		while (grid.getWords().size() > 0) {
			Word willBeRemovedWord = grid.getLastlyAdded();
			BoardDrawer.getCurrentBoardDrawer().removeWord(willBeRemovedWord, grid);
			if (willBeRemovedWord.getVector().getDirection().isHorizontal()) {
				grid.clearIntersection(
						willBeRemovedWord.getVector().getPosition().getX()+willBeRemovedWord.getIntersectedIndex(),
						willBeRemovedWord.getVector().getPosition().getY());
			} else {
				grid.clearIntersection(
						willBeRemovedWord.getVector().getPosition().getX(),
						willBeRemovedWord.getVector().getPosition().getY()+willBeRemovedWord.getIntersectedIndex());
			}
		}
	}
	
	/**
	 * Implementacion del metodo pedido por Game
	 * Es el punto de comienzo del algoritmo exacto
	 */
	@Override
	public void solve() {
		
		maxScore = 0;
		
		bestGrid = new Board(grid.getAvailableCharacters());
		
		List<String> allWords = grid.getDictionary().getWords();
		
		// Reservo a lo sumo grid.size()^2 de letras
		List<Letter> willVisitLetters = new ArrayList<Letter>(80);
		
		Word tmp = null;
		
		int score;
		
		boolean solutionFound = false;
		
		// Ponemos todas las palabras en el diccionario
		for (String w : allWords) {
			
			int x = grid.size()/2-w.length()+1;
			int y = grid.size()/2;
			
			// Probamos todas las posiciones posibles en el eje x
			for (int i = x; i <= grid.size()/2; i++) {
				
				// Limpiamos el tablero
				cleanBoard();
				
				// Creamos la palabra a colocar
				tmp = new Word(w, new Vector(new Coordinate(i, y), Direction.HORIZONTAL), -1);
				
				BoardDrawer.getCurrentBoardDrawer().addWord(tmp, grid);
				
				// Nos fijamos si este es mejor tablero que el anterior mejor
				// De ser asi actualizamos
				score = grid.getScore();
				if (score > maxScore) {
					maxScore = score;
					bestGrid = new Board(grid);
				}
				
				for (int j = 0; j < w.length(); j++) {
					// Mark characters as used
					grid.removeCharacter((Character)w.charAt(j));
					willVisitLetters.add(new Letter(tmp, (Character)w.charAt(j), j));
				}
				
				if (params.isVisual()) {
					BoardDrawer.getCurrentBoardDrawer().printUnstyled(grid);
				}
				
				if (exactSolution(willVisitLetters, 0) == 1) {
					solutionFound = true;
					break;
				}
				
				// Limipiamos
				willVisitLetters.clear();
			}
			
			if (solutionFound) {
				break;
			}
			// Hacemos esto porque sino la memoria crece muy rapido y se achancha el 
			// programa. Es una cuestion de implementacion en Java no del algoritmo
			visitedBoards.clear();
		}
		
		if (params.isVisual()) {
			BoardDrawer.getCurrentBoardDrawer().printStyled(bestGrid);
			System.out.println("Max. Score: " + bestGrid.getScore());
		}
		
		try {
			BoardDrawer.getCurrentBoardDrawer().printStyledDump(bestGrid, params.outputFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
