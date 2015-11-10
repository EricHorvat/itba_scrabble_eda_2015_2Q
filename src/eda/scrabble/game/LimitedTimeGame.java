package eda.scrabble.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import eda.scrabble.boards.logic.Grid;
import eda.scrabble.boards.logic.StackBoard;
import eda.scrabble.boards.visual.BoardDrawer;
import eda.scrabble.elements.Coordinate;
import eda.scrabble.elements.Direction;
import eda.scrabble.elements.Letter;
import eda.scrabble.elements.Vector;
import eda.scrabble.elements.Word;

public class LimitedTimeGame extends Game {

	private StackBoard board;
	
	/**
	 * Constante T obtenida empiricamente
	 */
	private final static double T = 6.5;
	
	private StackBoard bestestBoard;
	
	private List<Letter> lettersToVisit = new ArrayList<Letter>(Grid.GRID_SIZE*Grid.GRID_SIZE);
	
	private List<String> dictionaryWords;
	
	public LimitedTimeGame(GameParameters params) {
		super(params);
		
		board = new StackBoard(grid);
		dictionaryWords = board.getDictionary().getWords();
		bestestBoard = new StackBoard(grid);
	}
	
	/**
	 * Agrega una palabra aleatoria del diccionario al tablero
	 */
	private void addRandomWordToBoard() {
		// Buscamos una palabra aleatoria
		String randomWord = dictionaryWords.get( ((int)(Math.random() * 1000)) % dictionaryWords.size() );
		
		// La colocamos en el tablero
		Word w = new Word(randomWord, new Vector(new Coordinate((board.size()-randomWord.length())/2, board.size()/2), Direction.HORIZONTAL), -1);
		BoardDrawer.getCurrentBoardDrawer().addWord(w, board);
		
		lettersToVisit.clear();
		for (int j = 0; j < w.getWord().length(); j++) {
			board.removeCharacter((Character)w.getWord().charAt(j));
			lettersToVisit.add(new Letter(w, (Character)w.getWord().charAt(j), j));
		}
	}
	
	/**
	 * Implementacion del Hill Climber (normal)
	 */
	private void hillClimb() {
		
		// Emepezamos con un tablero al azar
		addRandomWordToBoard();
		
		StackBoard bestBoard = board;
		Word wordToAdd = null; 
		Letter bestLetter = null;
		
		// Armamos Letters
//		List<Letter> letters = new ArrayList<Letter>(Grid.GRID_SIZE*Grid.GRID_SIZE);
		
//		lettersBuildUpForBoard(letters, board);
		
		String aux = null;
		
		do { // Mientras tenga tiempo
			
			int localMaxScore = board.getScore();
			
			bestBoard = board;
			
			bestLetter = null;
			wordToAdd = null;
			
//			bestBoard.print();
			
			for (Letter letter : lettersToVisit) { // Recorro cada una de las letras disponibles
				
				do { // Pruebo con cada palabra que me matchee
					
					
					// Busco la palabra correspondiente en mi diccionario
					aux = board.getDictionary().bestLimitedOptionAfter(board.getAvailableCharacters(), MAX_LENGTH_WORD, letter.getCharacter(), aux);
					
					if ( aux != null) {
						
						// Agregar la palabra
						int intersectionIndex = aux.indexOf(letter.getCharacter());
						
						board.markIntersection(letter.getWord().getVector().getPosition(), letter.getWord().getVector().getDirection(), letter.getPosition());
						
						Word addWord;
						
						if (letter.getWord().getVector().getDirection().isHorizontal()) {
							
							addWord = new Word(aux,
									new Vector(
											new Coordinate(
													letter.getWord().getVector().getPosition().getX()+letter.getPosition(),
													letter.getWord().getVector().getPosition().getY()-intersectionIndex),
													letter.getWord().getVector().getDirection().getOpposite()),
									intersectionIndex);
						} else {
							
							addWord = new Word(aux,
									new Vector(
											new Coordinate(
													letter.getWord().getVector().getPosition().getX()-intersectionIndex,
													letter.getWord().getVector().getPosition().getY()+letter.getPosition()),
													letter.getWord().getVector().getDirection().getOpposite()),
									intersectionIndex);
						}
						
						if (!BoardDrawer.getCurrentBoardDrawer().addWord(addWord, board)) {
							
							board.clearIntersection(letter.getWord().getVector().getPosition(), letter.getWord().getVector().getDirection(), letter.getPosition());
							
						} else {
							int score = board.getScore();
							
							// Consumimos los caracteres
							for (int k = 0; k < aux.length(); k++) {
								if (k != intersectionIndex) {
									board.removeCharacter((Character)aux.charAt(k));
								}
							}
							
							
							// Actualizamos
							if (score > localMaxScore) {
								localMaxScore = score;
								bestBoard = new StackBoard(board);
								wordToAdd = addWord;
								bestLetter = letter;
							}
							
							// Sacamos la palabra que acabamos de agregar
							// Devolvemos los caracteres
							BoardDrawer.getCurrentBoardDrawer().removeWord(addWord, board);
							
							board.clearIntersection(letter.getWord().getVector().getPosition(), letter.getWord().getVector().getDirection(), letter.getPosition());
						}
					}
				} while (aux != null);
				
			}
			
			// Actualizamos
			if (localMaxScore > maxScore) {
				maxScore = localMaxScore;
				bestestBoard = new StackBoard(bestBoard);
			}
			
			if (params.isVisual()) {
				BoardDrawer.getCurrentBoardDrawer().printUnstyled(bestBoard);
			}
			
			// Me fijo si me atore
				// Paso, la posta al proximo tablero
				// Si no hay mas tableros, hago uno random y que empiece a crecer
			if ( board != bestBoard ) {
				
				board = bestBoard;
				
				// Actualizo letters y characters, y sigo
				int intersectionIndex = wordToAdd.getIntersectedIndex();
				
				for (int i = 0; i < wordToAdd.getWord().length(); i++) {
					if (i == intersectionIndex) {
						lettersToVisit.remove(bestLetter);
					} else {
						lettersToVisit.add(new Letter(wordToAdd, (Character)wordToAdd.getWord().charAt(i), i));
					}
				}
				
			} else {
				
				
				
				// Maximo local
				//  Borramos todo y empezamos de 0
				while (!board.getWordsStack().isEmpty()) {
					Word removing = board.getWordsStack().peek(); 
					
					BoardDrawer.getCurrentBoardDrawer().removeWord(removing, board);
					
					board.clearIntersection(removing.getVector().getPosition(), removing.getVector().getDirection(), removing.getIntersectedIndex());
				}
				
				addRandomWordToBoard();
				
			}
			
		} while( System.nanoTime() < this.eta );
		
		if (params.isVisual()) {
			BoardDrawer.getCurrentBoardDrawer().printUnstyled(bestestBoard);
			System.out.println("Max. Score: " + bestestBoard.getScore());
		}
		
		try {
			BoardDrawer.getCurrentBoardDrawer().printUnstyledDump(bestestBoard, params.outputFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Implementacion del Hill Climber estocastico
	 */
	private void hillClimbStochastic() {
		
		Deque<List<Letter>> backup = new LinkedList<List<Letter>>();
		
		backup.push(new ArrayList<Letter>());
		
		// Tiramos una palabra al azar en el tablero
		// y creamos el array de letras a visitar
		// tambien consumimos los caracteres utilizados
		addRandomWordToBoard();
		
//		System.out.println("Hill Climb Stochastic");
		
		StackBoard bestBoard = board;
		Word wordToAdd = null; 
		Letter bestLetter = null;
		String aux = null;
		boolean found = false;
		
		do { // Mientras tengamos tiempo
		
			bestBoard = board;
			wordToAdd = null;
			bestLetter = null;
			aux = null;
			found = false;
			
			
			int initScore = board.getScore();
				
			int intersectionIndex = -1;
				
			for (Letter letter : lettersToVisit) { // En todas las letras donde podamos agregar palabras
				
				do { // Probamos todas las palabras que se pueden agregar en esa letra
					
					// obtenemos esa palabra
					aux = board.getDictionary().bestLimitedOptionAfter(board.getAvailableCharacters(), MAX_LENGTH_WORD, letter.getCharacter(), aux);
					
//					if (DEBUG) System.out.println("/SEARCH/"+letter.word.word+"/"+letter.c+"/"+aux);
					
					// Si todavia me quedan palabras
					if (aux != null) {
					
						Word toAdd;
						
						intersectionIndex = aux.indexOf(letter.getCharacter());
						
						board.markIntersection(letter.getWord().getVector().getPosition(), letter.getWord().getVector().getDirection(), letter.getPosition());
						
						// La agreguamos
						if (letter.getWord().getVector().getDirection().isHorizontal()) {
							
							toAdd = new Word(aux,
									new Vector(
											new Coordinate(
													letter.getWord().getVector().getPosition().getX()+letter.getPosition(),
													letter.getWord().getVector().getPosition().getY()-intersectionIndex),
													letter.getWord().getVector().getDirection().getOpposite()),
									intersectionIndex);
						} else {
							
							toAdd = new Word(aux,
									new Vector(
											new Coordinate(
													letter.getWord().getVector().getPosition().getX()-intersectionIndex,
													letter.getWord().getVector().getPosition().getY()+letter.getPosition()),
													letter.getWord().getVector().getDirection().getOpposite()),
									intersectionIndex);
						}
						
						// Si Fallo el agregado
						if (!BoardDrawer.getCurrentBoardDrawer().addWord(toAdd, board)) {
							
							board.clearIntersection(letter.getWord().getVector().getPosition(), letter.getWord().getVector().getDirection(), letter.getPosition());
							
							// Seguimos a buscar la proxima palabra que se pueda colocar en esta posicion
							continue;
							
						} else {
							
							int score = board.getScore();
							
							// Calculamos la probabilidad
							double p = 1 / ( 1 + Math.pow(Math.E, (initScore - score) / T ) );
							
							double rnd = Math.random();
							
							for (int k = 0; k < aux.length(); k++) {
								if (k != intersectionIndex) {
									board.removeCharacter((Character)aux.charAt(k));
								}
							}
							
							if ( p > rnd) {
								
								// Aca guardamos en bestBoard el tablero que elegimos
								// estocasticamente para la proxima corrida
								
								bestBoard = new StackBoard(board);
								wordToAdd = toAdd;
								bestLetter = letter;
								
								found = true;
								
								// Con esto nos vamos
								aux = null;
								
							} else {
								
								// Sacamos la palabra que acabamos de agregar
								// porque no cabia en el tablero
								BoardDrawer.getCurrentBoardDrawer().removeWord(toAdd, board);
							
								board.clearIntersection(letter.getWord().getVector().getPosition(), letter.getWord().getVector().getDirection(), letter.getPosition());
								
							}
						}
						
					}
				} while (aux != null);
				
				if (found)
					break;
				
			}
			
			
			
			int score = bestBoard.getScore();
			
			// Si supero el puntaje de mi maximo
			if (score > maxScore) {
				bestestBoard = new StackBoard(bestBoard);
				maxScore = score;
			}
			
			if (this.params.isVisual()) {
				BoardDrawer.getCurrentBoardDrawer().printUnstyled(bestBoard);
//				bestBoard.printSimple();
			}
			
			
			// Si no eligio ninguna palabra
			// sacamos la ultima que agregamos
			// para tener mas posibilidades de agregar otra
			// y ademas nos alejamos de un maximo local
			if (wordToAdd == null) {
				
//				Word removed = removeWord(wordToAdd, board);
				BoardDrawer.getCurrentBoardDrawer().removeWord(wordToAdd, board);
				
				lettersToVisit = backup.pop();
				
				if (board.getWordsStack().isEmpty()) {
					// Si no hay mas palabras me tira una excepcion
					
					addRandomWordToBoard();
					
					backup.push(new ArrayList<Letter>());
					
				}
				
			} else {
				// Si agregamos una palabra actualizamos letters
				// para recorrer la proxima pasada
//				boolean first = false;
				
				backup.push(new ArrayList<Letter>(lettersToVisit));
				
				
				for (int j = 0; j < wordToAdd.getWord().length(); j++) {
					if (j == intersectionIndex) {
//						first = true;
						lettersToVisit.remove(bestLetter);
					} else {
						lettersToVisit.add(new Letter(wordToAdd, (Character)wordToAdd.getWord().charAt(j), j));
					}
				}
				
				board = bestBoard;
			}
			
			
		} while ( System.nanoTime() < this.eta );
		
		if (params.isVisual()) {
			BoardDrawer.getCurrentBoardDrawer().printStyled(bestestBoard);
			System.out.println("Max. Score: " + bestestBoard.getScore());
		}
		
		try {
			BoardDrawer.getCurrentBoardDrawer().printStyledDump(bestestBoard, params.outputFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Implementacion del metodo pedido por Game
	 * Es el punto de comienzo del algoritmo aproximado
	 */
	@Override
	public void solve() {
		
		maxScore = 0;
		
		if (params.isStochastic()) {
			hillClimbStochastic();
		} else {
			hillClimb();
		}
		
	}

}
