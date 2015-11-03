package eda.scrabble;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
	
	/**
	 * @deprecated
	 */
	private void buildUpLetters() {
		
		Iterator<Word> it = board.getWordsStack().descendingIterator();
		while (it.hasNext()) {
			Word next = it.next();
			
			if (next.vec.dir== Direction.HORIZONTAL) {
				for (int j = next.vec.pos.x; j < next.vec.pos.x+next.word.length(); j++) {
					
					if (!board.isIntersection(j, next.vec.pos.y)) {
						lettersToVisit.add(new Letter(next, (Character)next.word.charAt(j-next.vec.pos.x), j-next.vec.pos.x));
					}
				}
			} else {
				for (int j = next.vec.pos.y; j < next.vec.pos.y+next.word.length(); j++) {
					
					if (!board.isIntersection(next.vec.pos.x, j)) {
						lettersToVisit.add(new Letter(next, (Character)next.word.charAt(j-next.vec.pos.y), j-next.vec.pos.y));
					}
				}
			}
			
		}
		
	}
	
	private void addRandomWordToBoard() {
	// Empezamos con una palabra aleatoria
		String randomWord = dictionaryWords.get( ((int)(Math.random() * 1000)) % dictionaryWords.size() );
		Word w = new Word(randomWord, new Vector(new Coordinate((board.size()-randomWord.length())/2, board.size()/2), Direction.HORIZONTAL), -1);
		
		addWord(w, board);
		
		lettersToVisit.clear();
		for (int j = 0; j < w.word.length(); j++) {
			board.removeCharacter((Character)w.word.charAt(j));
			lettersToVisit.add(new Letter(w, (Character)w.word.charAt(j), j));
		}
	}
	
	private void hillClimb() {
		
		if (DEBUG) {
			List<Character> lj = getAvailableChars(board.characters);
			System.out.println("("+lj.size()+"): "+lj);
		}
		
		addRandomWordToBoard();
		
		if (DEBUG) {
			List<Character> lj = getAvailableChars(board.characters);
			System.out.println("("+lj.size()+"): "+lj);
		}
		
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
			
			bestBoard.print();
			
			for (Letter letter : lettersToVisit) { // Recorro cada una de las letras disponibles
				
				if (DEBUG) {
					List<Character> lj = getAvailableChars(board.characters);
					System.out.println("("+lj.size()+"): "+lj);
				}
				
				do { // Pruebo con cada palabra que me matchee
					
					
					// Busco la palabra correspondiente en mi diccionario
					aux = board.getDictionary().bestLimitedOptionAfter(board.getCharacters(), MAX_LENGTH_WORD, letter.c, aux);
					
					if ( aux != null) {
						
						// Agregar la palabra
						int intersectionIndex = aux.indexOf(letter.c);
						
						board.markIntersection(letter.word.vec.pos, letter.word.vec.dir, letter.pos);
						
						Word addWord;
						
						if (letter.word.vec.dir == Direction.HORIZONTAL) {
							
							addWord = new Word(aux,
									new Vector(
											new Coordinate(
													letter.word.vec.pos.x+letter.pos,
													letter.word.vec.pos.y-intersectionIndex),
											Direction.VERTICAL),
									intersectionIndex);
						} else {
							
							addWord = new Word(aux,
									new Vector(
											new Coordinate(
													letter.word.vec.pos.x-intersectionIndex,
													letter.word.vec.pos.y+letter.pos),
											Direction.HORIZONTAL),
									intersectionIndex);
						}
						
						if (!addWord(addWord, board)) {
							
							board.clearIntersection(letter.word.vec.pos, letter.word.vec.dir, letter.pos);
							
						} else {
							int score = board.getScore();
							
							// Consumimos los caracteres
							for (int k = 0; k < aux.length(); k++) {
								if (k != intersectionIndex) {
									board.removeCharacter((Character)aux.charAt(k));
								}
							}
							
							if (score > localMaxScore) {
								localMaxScore = score;
								bestBoard = new StackBoard(board);
								wordToAdd = addWord;
								bestLetter = letter;
							}
							
							// Sacamos la palabra que acabamos de agregar
							// Devolvemos los caracteres
							removeWord(addWord, board);
							
							board.clearIntersection(letter.word.vec.pos, letter.word.vec.dir, letter.pos);
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
			
//			bestBoard.print();
			
			if (DEBUG) {
				List<Character> lj = getAvailableChars(board.characters);
				System.out.println("("+lj.size()+"): "+lj);
			}
			
			// Me fijo si me atore
				// Paso, la posta al proximo tablero
				// Si no hay mas tableros, hago uno random y que empiece a crecer
			if ( board != bestBoard ) {
				
				board = bestBoard;
				
				// Actualizo letters y characters, y sigo
				
				int intersectionIndex = wordToAdd.intersected;
				
				for (int i = 0; i < wordToAdd.word.length(); i++) {
					if (i == intersectionIndex) {
						lettersToVisit.remove(bestLetter);
					} else {
//						board.removeCharacter((Character)wordToAdd.word.charAt(i));
						lettersToVisit.add(new Letter(wordToAdd, (Character)wordToAdd.word.charAt(i), i));
					}
				}
				
			} else {
				
				
				
				// Maximo local
				//  Borramos todo y empezamos de 0
				while (!board.getWordsStack().isEmpty()) {
					Word removing = board.getWordsStack().peek(); 
					
					removeWord(removing, board);
					
					board.clearIntersection(removing.vec.pos, removing.vec.dir, removing.intersected);
				}
				
				addRandomWordToBoard();
				
			}
			
			if (DEBUG) {
				List<Character> lj = getAvailableChars(board.characters);
				System.out.println("("+lj.size()+"): "+lj + " " + board.characters);
			}
			
		} while( System.nanoTime() < this.eta );
		
		
		System.out.println("bestest " + bestestBoard.getScore());
		bestestBoard.print();
		
	}
	
	private void hillClimbStochastic() {
		
		if (DEBUG) {
			List<Character> llj = getAvailableChars(board.characters);
			System.out.println("1 ("+llj.size()+"): "+llj);
		}
		
		Deque<List<Letter>> backup = new LinkedList<List<Letter>>();
		
		backup.push(new ArrayList<Letter>());
		
		// Tiramos una palabra al azar en el tablero
		// y creamos el array de letras a visitar
		// tambien consumimos los caracteres utilizados
		addRandomWordToBoard();
		
		if (DEBUG) {
			List<Character> llj = getAvailableChars(board.characters);
			System.out.println("2 ("+llj.size()+"): "+llj);
		}
		
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
			found = false;
			
			
			int initScore = board.getScore();
			
			if (DEBUG) {
				List<Character> lj = getAvailableChars(board.characters);
				System.out.println("3 ("+lj.size()+"): "+lj);
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
						
						board.markIntersection(letter.word.vec.pos, letter.word.vec.dir, letter.pos);
						
						// La agreguamos
						if (letter.word.vec.dir == Direction.HORIZONTAL) {
							
							toAdd = new Word(aux,
									new Vector(
											new Coordinate(
													letter.word.vec.pos.x+letter.pos,
													letter.word.vec.pos.y-intersectionIndex),
											Direction.VERTICAL),
									intersectionIndex);
						} else {
							
							toAdd = new Word(aux,
									new Vector(
											new Coordinate(
													letter.word.vec.pos.x-intersectionIndex,
													letter.word.vec.pos.y+letter.pos),
											Direction.HORIZONTAL),
									intersectionIndex);
						}
						
						// Si Fallo el agregado
						if (!addWord(toAdd, board)) {
							
							board.clearIntersection(letter.word.vec.pos, letter.word.vec.dir, letter.pos);
							
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
								
								if (DEBUG)
									System.out.println("chosen");
								
								// Aca guardamos en bestBoard el tablero que elegimos
								// estocasticamente para la proxima corrida
								
								bestBoard = new StackBoard(board);
								wordToAdd = toAdd;
								bestLetter = letter;
								
								found = true;
								
								if (DEBUG) {
									List<Character> llj = getAvailableChars(board.characters);
									System.out.println("4 ("+llj.size()+"): "+llj);
								}
								
								
								// Con esto nos vamos
								aux = null;
								
							} else {
							
								if (DEBUG)
									System.out.println("not chosen");
								
								if (DEBUG) {
									List<Character> llj = getAvailableChars(board.characters);
									System.out.println("5 ("+llj.size()+"): "+llj + " " + board.characters);
								}
								
								// Sacamos la palabra que acabamos de agregar
								// porque no cabia en el tablero
								removeWord(toAdd, board);
								
								if (DEBUG) {
									List<Character> llj = getAvailableChars(board.characters);
									System.out.println("6 ("+llj.size()+"): "+llj + " " + board.characters);
								}
							
								board.clearIntersection(letter.word.vec.pos, letter.word.vec.dir, letter.pos);
								
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
				
				Word removed = removeWord(wordToAdd, board);
				
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
