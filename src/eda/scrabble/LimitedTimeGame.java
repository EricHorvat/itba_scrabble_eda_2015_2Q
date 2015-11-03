package eda.scrabble;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import eda.scrabble.Game.Direction;
import eda.scrabble.Game.GameParameters;
import eda.scrabble.Game.WordXY;
import eda.scrabble.file.InputData;

public class LimitedTimeGame extends Game {

	private StackBoard board;
	
	private final static double T = 6.5;
	
	private final static boolean STOCHASTIC = true;
	private final static boolean DEBUG = true;
	
	private StackBoard bestestBoard;
	
	private List<Letter> lettersToVisit = new ArrayList<Letter>(Grid.GRID_SIZE*Grid.GRID_SIZE);
	
	private List<String> dictionaryWords;
	
	public LimitedTimeGame(GameParameters params) {
		super(params);
		
		board = new StackBoard(grid);
		dictionaryWords = board.getDictionary().getWords();
	}
	
	private void buildUpLetters() {
		
		Iterator<Word> it = board.getWordsStack().descendingIterator();
		while (it.hasNext()) {
			Word next = it.next();
			
			if (next.direction == Direction.HORIZONTAL) {
				for (int j = next.pos.x; j < next.pos.x+next.word.length(); j++) {
					
					if (!board.isIntersection(j, next.pos.y)) {
						lettersToVisit.add(new Letter(next, (Character)next.word.charAt(j-next.pos.x), j-next.pos.x));
					}
				}
			} else {
				for (int j = next.pos.y; j < next.pos.y+next.word.length(); j++) {
					
					if (!board.isIntersection(next.pos.x, j)) {
						lettersToVisit.add(new Letter(next, (Character)next.word.charAt(j-next.pos.y), j-next.pos.y));
					}
				}
			}
			
		}
		
	}
	
	private void addRandomWordToBoard() {
	// Empezamos con una palabra aleatoria
		String randomWord = dictionaryWords.get( ((int)(Math.random() * 1000)) % dictionaryWords.size() );
		Word w = null;
		
		AddWordResult result = addWord(
				(board.size()-randomWord.length())/2,
				board.size()/2,
				Direction.HORIZONTAL,
				randomWord,
				board
		);
		
		if (!result.success) {
			// Bastante imposible que suceda esto. Agregar una palabra valida en un tablero vacio
			if (DEBUG) System.out.println(result.msg);
		} else {
			
			w = result.word;
			
			lettersToVisit.clear();
			for (int j = 0; j < w.word.length(); j++) {
				board.removeCharacter((Character)w.word.charAt(j));
				lettersToVisit.add(new Letter(w, (Character)w.word.charAt(j), j));
			}
		}
	}
	
	private void hillClimb() {
		
		int currentBoard = 0;
		StackBoard board = boards.get(currentBoard);
		
		StackBoard bestBoard = board;
		WordXY wordToAdd = null; 
		LetterXY bestLetter = null;
		
		// Armamos Letters
		List<LetterXY> letters = new ArrayList<LetterXY>(Grid.GRID_SIZE*Grid.GRID_SIZE);
		
		lettersBuildUpForBoard(letters, board);
		
		String aux = null;
		
		do { // Mientras tenga tiempo
			
			int localMaxScore = board.getScore();
			
			bestLetter = null;
			wordToAdd = null;
			
			bestBoard.print();
			
			for (LetterXY letter : letters) { // Recorro cada una de las letras disponibles
				
				do { // Pruebo con cada palabra que me matchee
					
					
					// Busco la palabra correspondiente en mi diccionario
					aux = board.getDictionary().bestLimitedOptionAfter(board.getCharacters(), MAX_LENGTH_WORD, letter.c, aux);
					
					if ( aux != null) {
						
						// Agregar la palabra
						int intersectionIndex = aux.indexOf(letter.c);
						
						if (letter.word.direction == Direction.HORIZONTAL) {
							board.markIntersection(letter.word.pos.x+letter.pos, letter.word.pos.y);
						} else {
							board.markIntersection(letter.word.pos.x, letter.word.pos.y+letter.pos);
						}
						
						AddWordResult result;
						
						// Bug Fix
						StringBuilder sb = new StringBuilder(aux);
						sb.setCharAt(intersectionIndex, ' ');
						
						if (letter.word.direction == Direction.HORIZONTAL) {
							result = addWord(letter.word.pos.x+letter.pos, letter.word.pos.y-intersectionIndex, Direction.VERTICAL, sb.toString(), board);
						} else {
							result = addWord(letter.word.pos.x-intersectionIndex, letter.word.pos.y+letter.pos, Direction.HORIZONTAL, sb.toString(), board);
						}
						
						if (!result.success) {
							
							// Si no se agrego reseteamos
							if (letter.word.direction == Direction.HORIZONTAL) {
								board.clearIntersection(letter.word.pos.x+letter.pos, letter.word.pos.y);
							} else {
								board.clearIntersection(letter.word.pos.x, letter.word.pos.y+letter.pos);
							}
							
						} else {
							
							// Bug Fix
							result.word.word = aux;
							
							int score = board.getScore();
							
							if (score > localMaxScore) {
								localMaxScore = score;
								bestBoard = new StackBoard(board);
								wordToAdd = result.word;
								bestLetter = letter;
							}
							
							// Sacamos la palabra que acabamos de agregar y empezamos de cero
							removeWord(result.word, board);
							
							if (letter.word.direction == Direction.HORIZONTAL) {
								board.clearIntersection(letter.word.pos.x+letter.pos, letter.word.pos.y);
							} else {
								board.clearIntersection(letter.word.pos.x, letter.word.pos.y+letter.pos);
							}
						}
						
					}
					
				} while (aux != null);
				
			}
			
			System.out.println(localMaxScore + " " + maxScore);
			
			if (localMaxScore > maxScore) {
				maxScore = localMaxScore;
				System.out.println("nuevo bestest");
				bestestBoard = new StackBoard(bestBoard);
			}
			
			if (ANT && params.isVisual()) {
				bestBoard.printSimple();
			}
			
			if (DEBUG) {
				bestBoard.print();
			}
			
			bestBoard.print();
			
			
			
			// Me fijo si me atore
				// Paso, la posta al proximo tablero
				// Si no hay mas tableros, hago uno random y que empiece a crecer
			if ( board != bestBoard ) {
				
				board = bestBoard;
				
				// Actualizo letters y characters, y sigo
				
				int intersectionIndex = wordToAdd.word.indexOf(bestLetter.c);
				
				for (int i = 0; i < wordToAdd.word.length(); i++) {
					if (i == intersectionIndex) {
						letters.remove(bestLetter);
					} else {
						board.removeCharacter((Character)wordToAdd.word.charAt(i));
						letters.add(new LetterXY(wordToAdd, (Character)wordToAdd.word.charAt(i), i));
					}
				}
				
			} else {
				
				
				// Maximo local
				
				// Pasamos al proximo tablero
				if (currentBoard+1 < boards.size()) {
					currentBoard++;
					
					System.out.println("paso posta");
					
					board = boards.get(currentBoard);
					bestBoard = board;
					
					// Rearmamos letters
					
					letters.clear();
					
					lettersBuildUpForBoard(letters, board);
					
				}
				// O generamos uno random
				else {
//					if (true) return;
					System.out.println("random");
					
					// Generamos tablero random
					
					// Como vamos a vaciar las intersecciones borramos 2 veces los caracters de las intersecciones
					// sino se reponen 2 veces
					for (Map.Entry<Coordinate, Boolean> e: board.intersections.entrySet()) {
						
						if (e.getValue() != null && e.getValue() == true) {
							board.removeCharacter((Character)board.get(e.getKey().x, e.getKey().y));
							board.removeCharacter((Character)board.get(e.getKey().x, e.getKey().y));
						}
						
					}
					
					// Vaciamos el tablero
					board.intersections.clear();
					
					while (board.getWordsStack().size() > 0) {
						removeWord(board.getWordsStack().peek(), board);
					}
					
					// Empezamos con una palabra aleatoria
					List<String> dictionaryWords = board.getDictionary().getWords(); 
					String emergency = dictionaryWords.get( ((int)(Math.random() * 1000)) % dictionaryWords.size() );
//					System.out.println("remain chars ("+getAvailableChars(board.characters).size()+")"+ getAvailableChars(board.characters));
					WordXY w = null;
					
					AddWordResult result = addWord(
							(board.size()-emergency.length())/2,
							board.size()/2,
							Direction.HORIZONTAL,
							emergency,
							board
					);
					
					if (!result.success) {
						// Bastante imposible que suceda esto. Agregar una palabra valida en un tablero vacio
						if (DEBUG) System.out.println(result.msg);
					} else {
						
						w = result.word;
						
						letters.clear();
						for (int j = 0; j < w.word.length(); j++) {
							board.removeCharacter((Character)w.word.charAt(j));
							letters.add(new LetterXY(w, (Character)w.word.charAt(j), j));
						}
					}
					bestBoard = board;
//					board = bestBoard;
				}
				
				
				
			}
			
		} while( System.nanoTime() < this.eta );
		
		
		System.out.println("bestest");
		bestestBoard.print();
		
	}
	
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
			
			if (DEBUG) board.print();
		
			bestBoard = board;
			wordToAdd = null;
			bestLetter = null;
			aux = null;
			
			int initScore = board.getScore();
			
			found = false;
			
			if (DEBUG) {
				List<Character> lj = getAvailableChars(board.characters);
				System.out.println("("+lj.size()+"): "+lj + " " + board.characters);
			}
				
			int intersectionIndex = -1;
				
			for (Letter letter : lettersToVisit) { // En todas las letras donde podamos agregar palabras
				
				do { // Probamos todas las palabras que se pueden agregar en esa letra
					
					// obtenemos esa palabra
					aux = board.getDictionary().bestLimitedOptionAfter(board.getCharacters(), MAX_LENGTH_WORD, letter.c, aux);
					
//					if (DEBUG) System.out.println("/SEARCH/"+letter.word.word+"/"+letter.c+"/"+aux);
					
					// Si todavia me quedan palabras
					if (aux != null) {
					
						Word toAdd;
						
						intersectionIndex = aux.indexOf(letter.c);
						
						board.markIntersection(letter.word.pos, letter.word.direction, letter.pos);
						
						AddWordResult result;
						
						StringBuilder sb = new StringBuilder(aux);
						sb.setCharAt(intersectionIndex, ' ');
						
						// La agreguamos
						if (letter.word.direction == Direction.HORIZONTAL) {
							result = addWord(letter.word.pos.x+letter.pos, letter.word.pos.y-intersectionIndex, Direction.VERTICAL, sb.toString(), board);
						} else {
							result = addWord(letter.word.pos.x-intersectionIndex, letter.word.pos.y+letter.pos, Direction.HORIZONTAL, sb.toString(), board);
						}
						
						// Si Fallo el agregado
						if (!result.success) {
							
							if (DEBUG) System.out.println("Failed. "+ result.msg);
							
							board.clearIntersection(letter.word.pos, letter.word.direction, letter.pos);
							
							// Seguimos a buscar la proxima palabra que se pueda colocar en esta posicion
							continue;
							
						} else {
							
							toAdd = result.word;
							toAdd.word = aux;
							
							int score = board.getScore();
							
							// Calculamos la probabilidad
							double p = 1 / ( 1 + Math.pow(Math.E, (initScore - score) / T ) );
							
							double rnd = Math.random();
							if ( p > rnd) {
								
								if (DEBUG)
									System.out.println("chosen");
								
								// Aca guardamos en bestBoard el tablero que elegimos
								// estocasticamente para la proxima corrida
								
								bestBoard = new StackBoard(board);
								wordToAdd = toAdd;
								bestLetter = letter;
								
								wordToAdd.word = aux;
								
								found = true;
								
								if (DEBUG) {
									List<Character> llj = getAvailableChars(board.characters);
									System.out.println("("+llj.size()+"): "+llj + " " + board.characters);
								}
								
								for (int k = 0; k < aux.length(); k++) {
									if (k != intersectionIndex) {
										bestBoard.removeCharacter((Character)aux.charAt(k));
										board.removeCharacter((Character)aux.charAt(k));
									}
								}
								
								if (DEBUG) {
									List<Character> llj = getAvailableChars(board.characters);
									System.out.println("("+llj.size()+"): "+llj + " " + board.characters);
								}
								
								
								// Con esto nos vamos
								aux = null;
								
							} else {
							
								if (DEBUG)
									System.out.println("not chosen");
								
								if (DEBUG) {
									List<Character> llj = getAvailableChars(board.characters);
									System.out.println("("+llj.size()+"): "+llj + " " + board.characters);
								}
								
								// Sacamos la palabra que acabamos de agregar
								// porque no cabia en el tablero
								removeWord(result.word, board);
								
								if (DEBUG) {
									List<Character> llj = getAvailableChars(board.characters);
									System.out.println("("+llj.size()+"): "+llj + " " + board.characters);
								}
							
								board.clearIntersection(letter.word.pos, letter.word.direction, letter.pos);
								
							}
						}
						
					}
				} while (aux != null);
				
				if (found)
					break;
				
			}
			
			
			
			int score = bestBoard.getScore();
			
			System.out.println(score);
			
			// Si supero el puntaje de mi maximo
			if (score > maxScore) {
				bestestBoard = new StackBoard(bestBoard);
				maxScore = score;
			}
			
			if (ANT && this.params.isVisual())
				bestBoard.printSimple();
			
			bestBoard.printSimple();
			
			
			// Si no eligio ninguna palabra
			// sacamos la ultima que agregamos
			// para tener mas posibilidades de agregar otra
			// y ademas nos alejamos de un maximo local
			if (wordToAdd == null) {
				if (DEBUG) {
					System.out.println("wordToAdd: null");
					board.print();
				}
				if (!board.getWordsStack().isEmpty()) {
					// Si no hay mas palabras me tira una excepcion
					Word removed = removeWord(wordToAdd, board);
					
					lettersToVisit = backup.pop();
					
					
				} else {
					// Elegimos una palabra al azar del diccionario y empezamos a escalar a partir
					// de ahi
					
					addRandomWordToBoard();
					
				}
				
			} else {
				// Si agregamos una palabra actualizamos letters
				// para recorrer la proxima pasada
//				boolean first = false;
				if (DEBUG) System.out.println("2");
				if (DEBUG) printUsed(lettersToVisit);
				for (int j = 0; j < wordToAdd.word.length(); j++) {
					if (j == intersectionIndex) {
//						first = true;
						lettersToVisit.remove(bestLetter);
					} else {
						lettersToVisit.add(new Letter(wordToAdd, (Character)wordToAdd.word.charAt(j), j));
					}
				}
				if (DEBUG) printUsed(lettersToVisit);
				
				board = bestBoard;
			}
			
			
//			hillClimbStochastic(bestBoard, letters);
			
		} while ( System.nanoTime() < this.eta );
		
		System.out.println("Max Score: " + bestestBoard.getScore());
		
		bestestBoard.print();
		
		if (ANT) {
			
			
			try {
				board.printSimpleDump(params.outputFileName);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	@Override
	public void solve() {
		
		long start = System.nanoTime();
		
		maxScore = 0;
		
		int best = 0, i = 0;
		
		int oldScore = 0;
		
		if (STOCHASTIC) {
			hillClimbStochastic();
		} else {
			hillClimb();
		}
		
//		preHillClimb(boards.get(0), 1);
				//this.boards.get(best).getDictionary(), this.boards.get(best), this._words.get(best));
		
		long end = System.nanoTime() - start; 
		System.out.println("Run Time: " + end/1000000.0 + "ms");
		
	}

}
