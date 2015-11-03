package eda.scrabble;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class LimitedTimeGame extends Game {

	private StackBoard board;
	
	private final static double T = 6.5;
	
	// Es publico para los tests
	public static boolean STOCHASTIC = false;
	
	private StackBoard bestestBoard;
	
	private List<Letter> lettersToVisit = new ArrayList<Letter>(Grid.GRID_SIZE*Grid.GRID_SIZE);
	
	private List<String> dictionaryWords;
	
	public LimitedTimeGame(GameParameters params) {
		super(params);
		
		board = new StackBoard(grid);
		dictionaryWords = board.getDictionary().getWords();
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
			
			if (localMaxScore > maxScore) {
				maxScore = localMaxScore;
				bestestBoard = new StackBoard(bestBoard);
			}
			
			if (params.isVisual()) {
				bestBoard.printSimple();
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
			
		} while( System.nanoTime() < this.eta );
		
		
//		System.out.println("bestest " + bestestBoard.getScore());
		try {
			board.printSimpleDump(params.outputFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
								removeWord(toAdd, board);
							
								board.clearIntersection(letter.word.vec.pos, letter.word.vec.dir, letter.pos);
								
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
				bestBoard.printSimple();
			}
			
			
			// Si no eligio ninguna palabra
			// sacamos la ultima que agregamos
			// para tener mas posibilidades de agregar otra
			// y ademas nos alejamos de un maximo local
			if (wordToAdd == null) {
				
//				Word removed = removeWord(wordToAdd, board);
				removeWord(wordToAdd, board);
				
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
				
				
				for (int j = 0; j < wordToAdd.word.length(); j++) {
					if (j == intersectionIndex) {
//						first = true;
						lettersToVisit.remove(bestLetter);
					} else {
						lettersToVisit.add(new Letter(wordToAdd, (Character)wordToAdd.word.charAt(j), j));
					}
				}
				
				board = bestBoard;
			}
			
			
//			hillClimbStochastic(bestBoard, letters);
			
		} while ( System.nanoTime() < this.eta );
		
//		System.out.println("Max Score: " + bestestBoard.getScore());
		
//		bestestBoard.print();
			
			
		try {
			board.printSimpleDump(params.outputFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void solve() {
		
//		long start = System.nanoTime();
		
		maxScore = 0;
		
		if (STOCHASTIC) {
			hillClimbStochastic();
		} else {
			hillClimb();
		}
		
//		long end = System.nanoTime() - start; 
//		System.out.println("Run Time: " + end/1000000.0 + "ms");
		
	}

}
