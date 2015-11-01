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

	private List<StackBoard> boards;
	
	private final static double T = 6.5;
	
	private final static boolean STOCHASTIC = false;
	private final static boolean DEBUG = false;
	
	private StackBoard bestestBoard;
	
	public LimitedTimeGame(GameParameters params) {
		super(params);
		
		boards = new ArrayList<StackBoard>(5);
		
		// Default Dictionary from Game class
		/** @see @{Game}  */
		boards.add((StackBoard) grid);
		
		for (int i = 0; i < 4; i++) {
			boards.add(new StackBoard(grid.characters));
		}
		
		boards.get(1).setDictionary(InputData.fillDictionary(
				DICTIONARY_FILENAME,
				InputData.DictionaryFillStrategy.NONE,
				grid.characters));
		
		boards.get(2)
		.setDictionary(InputData.fillDictionary(
				DICTIONARY_FILENAME,
				InputData.DictionaryFillStrategy.LOWEST_VALUE,
				grid.characters));
		
		boards.get(3)
		.setDictionary(InputData.fillDictionary(
				DICTIONARY_FILENAME,
				InputData.DictionaryFillStrategy.LOWEST_OCURRENCY,
				grid.characters));
		
		boards.get(4)
		.setDictionary(InputData.fillDictionary(
				DICTIONARY_FILENAME,
				InputData.DictionaryFillStrategy.HIGHEST_OCURRENCY,
				grid.characters));
		
	}
	
	private void lettersBuildUpForBoard(List<LetterXY> letters, StackBoard board) {
		
		Iterator<WordXY> it = board.getWordsStack().descendingIterator();
		while (it.hasNext()) {
			WordXY next = it.next();
			
			if (next.direction == Direction.HORIZONTAL) {
				for (int j = next.pos.x; j < next.pos.x+next.word.length(); j++) {
					
					if (!board.isIntersection(j, next.pos.y)) {
						letters.add(new LetterXY(next, (Character)next.word.charAt(j-next.pos.x), j-next.pos.x));
					}
				}
			} else {
				for (int j = next.pos.y; j < next.pos.y+next.word.length(); j++) {
					
					if (!board.isIntersection(next.pos.x, j)) {
						letters.add(new LetterXY(next, (Character)next.word.charAt(j-next.pos.y), j-next.pos.y));
					}
				}
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
			
			
			
			if (localMaxScore > maxScore) {
				maxScore = localMaxScore;
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
	
	private void hillClimbStochastic(StackBoard board) {
		
	// Armamos Letters
		List<LetterXY> letters = new ArrayList<LetterXY>(Grid.GRID_SIZE*Grid.GRID_SIZE);
		
		Iterator<WordXY> it = board.getWordsStack().descendingIterator();	
		while (it.hasNext()) {
			WordXY next = it.next();
			
			if (next.direction == Direction.HORIZONTAL) {
				for (int j = next.pos.x; j < next.pos.x+next.word.length(); j++) {
					
					if (!board.isIntersection(j, next.pos.y)) {
						letters.add(new LetterXY(next, (Character)next.word.charAt(j-next.pos.x), j-next.pos.x));
					}
				}
			} else {
				for (int j = next.pos.y; j < next.pos.y+next.word.length(); j++) {
					
					if (!board.isIntersection(next.pos.x, j)) {
						letters.add(new LetterXY(next, (Character)next.word.charAt(j-next.pos.y), j-next.pos.y));
					}
				}
			}
			
		}
		
//		System.out.println("Hill Climb Stochastic");
		
		StackBoard bestBoard = board;
		WordXY wordToAdd = null; 
		LetterXY bestLetter = null;
		
		do {
			
			if (DEBUG) board.print();
		
			bestBoard = board;
			wordToAdd = null;
			bestLetter = null;
			
			int initScore = board.getScore();
			
			String aux = null;
			
			boolean found = false;
			
			for (LetterXY letter : letters) {
				
				do {
					
					aux = board.getDictionary().bestLimitedOptionAfter(board.getCharacters(), MAX_LENGTH_WORD, letter.c, aux);
					
					if (DEBUG) System.out.println("/SEARCH/"+letter.word.word+"/"+letter.c+"/"+aux);
					
					if (aux != null) {
					
						WordXY toAdd;
						
						int intersectionIndex = aux.indexOf(letter.c);
						
						if (letter.word.direction == Direction.HORIZONTAL) {
							board.markIntersection(letter.word.pos.x+letter.pos, letter.word.pos.y);
						} else {
							board.markIntersection(letter.word.pos.x, letter.word.pos.y+letter.pos);
						}
						
						AddWordResult result;
						
						StringBuilder sb = new StringBuilder(aux);
						sb.setCharAt(intersectionIndex, ' ');
						
						if (letter.word.direction == Direction.HORIZONTAL) {
							result = addWord(letter.word.pos.x+letter.pos, letter.word.pos.y-intersectionIndex, Direction.VERTICAL, sb.toString(), board);
						} else {
							result = addWord(letter.word.pos.x-intersectionIndex, letter.word.pos.y+letter.pos, Direction.HORIZONTAL, sb.toString(), board);
						}
						
						if (!result.success) {
							
							if (DEBUG) System.out.println("Failed. "+ result.msg);
							
							if (letter.word.direction == Direction.HORIZONTAL) {
								board.clearIntersection(letter.word.pos.x+letter.pos, letter.word.pos.y);
							} else {
								board.clearIntersection(letter.word.pos.x, letter.word.pos.y+letter.pos);
							}
							
							continue;
							
						} else {
							
							toAdd = result.word;
							
							int score = board.getScore();
							
							double p = 1 / ( 1 + Math.pow(Math.E, (initScore - score) / T ) );
							
							double rnd = Math.random();
							if ( p > rnd) {
								
								bestBoard = new StackBoard(board);
								wordToAdd = toAdd;
								bestLetter = letter;
								
								wordToAdd.word = aux;
								
								found = true;
								
								for (int k = 0; k < aux.length(); k++) {
									if (k != intersectionIndex)
										board.removeCharacter((Character)aux.charAt(k));
								}
								
								aux = null;
								
							} else {
							
								removeWord(result.word, board);
							
								if (letter.word.direction == Direction.HORIZONTAL) {
									board.clearIntersection(letter.word.pos.x+letter.pos, letter.word.pos.y);
								} else {
									board.clearIntersection(letter.word.pos.x, letter.word.pos.y+letter.pos);
								}
							}
						}
						
					}
				} while (aux != null);
				
				if (found)
					break;
				
			}
			
			if (bestBoard.getScore() > maxScore) {
				bestestBoard = bestBoard;
				maxScore = bestBoard.getScore();
			}
			
			if (ANT && this.params.isVisual())
				bestBoard.printSimple();
			
//			bestBoard.printSimple();
			
			
			if (wordToAdd == null) {
				if (DEBUG) {
					System.out.println("wordToAdd: null");
					board.print();
				}
				try {
					// Si no hay mas palabras me tira una excepcion
					WordXY removed = removeWord(wordToAdd, board);
					
					if (DEBUG) printUsed(letters);
					if (removed.direction == Direction.HORIZONTAL) {
						for (int j = removed.pos.x; j < removed.pos.x+removed.word.length(); j++) {
							if (!board.isIntersection(j, removed.pos.y)) {
								letters.remove(new LetterXY(removed, (Character)removed.word.charAt(j-removed.pos.x), j-removed.pos.x));
							} else {
								board.clearIntersection(j, removed.pos.y);
							}
						}
					} else {
						for (int j = removed.pos.y; j < removed.pos.y+removed.word.length(); j++) {
							if (!board.isIntersection(removed.pos.x, j)) {
								letters.remove(new LetterXY(removed, (Character)removed.word.charAt(j-removed.pos.y), j-removed.pos.y));
							} else {
								board.clearIntersection(removed.pos.x, j);
							}
						}
					}
					if (DEBUG) printUsed(letters);
				} catch (NoSuchElementException ex) {
					
					// Elegimos una palabra al azar del diccionario y empezamos a escalar a partir
					// de ahi
					
					List<String> dictionaryWords = board.getDictionary().getWords(); 
					String emergency = dictionaryWords.get( ((int)(Math.random() * 1000)) % dictionaryWords.size() );
					if (DEBUG) System.out.println("remain chars ("+getAvailableChars(board.characters).size()+")"+ getAvailableChars(board.characters));
					WordXY w = null;
					AddWordResult result = addWord((board.size()-emergency.length())/2, grid.size()/2, Direction.HORIZONTAL, emergency, board);
					
					if (!result.success) {
						if (DEBUG) System.out.println(result.msg);
					} else {
					
						if (DEBUG) {
							List<Character> lj = getAvailableChars(board.characters);
							System.out.println("("+lj.size()+"): "+lj + " " + board.characters);
						}
						
						w = result.word;
						
						letters.clear();
						for (int j = 0; j < w.word.length(); j++) {
							board.removeCharacter((Character)w.word.charAt(j));
							letters.add(new LetterXY(w, (Character)w.word.charAt(j), j));
						}
						
						if (DEBUG) {
							List<Character> lj = getAvailableChars(board.characters);
							System.out.println("("+lj.size()+"): "+lj + " " + board.characters);
						}
					}
				}
				
			} else {
			
				boolean first = false;
				if (DEBUG) System.out.println("2");
				if (DEBUG) printUsed(letters);
				for (int j = 0; j < wordToAdd.word.length(); j++) {
					if (!first && wordToAdd.word.charAt(j) == (char)bestLetter.c) {
						first = true;
						letters.remove(bestLetter);
					} else {
						letters.add(new LetterXY(wordToAdd, (Character)wordToAdd.word.charAt(j), j));
					}
				}
				if (DEBUG) printUsed(letters);
			}
			
			
//			hillClimbStochastic(bestBoard, letters);
			
		} while ( System.nanoTime() < this.eta );
		
		System.out.println("Max Score: " + bestestBoard.getScore());
		
		board.print();
		
		if (ANT) {
			
			
			try {
				board.printSimpleDump(params.outputFileName);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	/**
	 * Va agregando en profundidad por la primera palabra que puede conseguir
	 * @param word La palabra en la cual arrancar
	 */
	private void firstDepth(StackBoard board) {
			//Dictionary dictionary, WordXY word, Grid grid, Deque<WordXY> words) {
		WordXY word = board
				.getWordsStack()
				.peek();
		
		if (word == null) return;
		
		String aux = null;
		
		if (DEBUG) System.out.println(word);
		
		for (int i = 0; i < word.word.length(); i++) {
			
			if (word.direction == Direction.HORIZONTAL) {
				if (board.isIntersection(word.pos.x+i, word.pos.y)) {
					continue;
				}
			} else {
				if (board.isIntersection(word.pos.x, word.pos.y+i)) {
					continue;
				}
			}
			
			Character intersectionChar = (Character)word.word.charAt(i);
			
			aux = board.getDictionary().bestFirstLimitedOption(board.getCharacters(), MAX_LENGTH_WORD, intersectionChar);
			
			if (DEBUG) System.out.println("SEARCH/"+word.word+"/"+intersectionChar+"/"+aux);
			
			if (aux == null) {
			
				continue;
			}
			
			int intersectionIndex = aux.indexOf(intersectionChar);
			
			if (word.direction == Direction.HORIZONTAL) {
				board.markIntersection(word.pos.x+i, word.pos.y);
			} else {
				board.markIntersection(word.pos.x, word.pos.y+i);
			}
			
			AddWordResult result;
			
			
			StringBuilder sb = new StringBuilder(aux);
			sb.setCharAt(intersectionIndex, ' ');
			
			if (word.direction == Direction.HORIZONTAL) {
				result = addWord(word.pos.x+i, word.pos.y-intersectionIndex, Direction.VERTICAL, sb.toString(), board);
			} else {
				result = addWord(word.pos.x-intersectionIndex, word.pos.y+i, Direction.HORIZONTAL, sb.toString(), board);
			}
			
			if (!result.success) {
				
//				System.out.println("Failed. " + result.msg);
				
				if (word.direction == Direction.HORIZONTAL) {
					board.clearIntersection(word.pos.x+i, word.pos.y);
				} else {
					board.clearIntersection(word.pos.x, word.pos.y+i);
				}
				
				continue;
				
			} else {
				
				int score = board.getScore();
				if (score > maxScore)
					maxScore = score;
				
				for (int j = 0; j < aux.length(); j++) {
					if (j != intersectionIndex) {
						board.removeCharacter((Character)aux.charAt(j));
					}
				}
				
				firstDepth(board);
				
				// Cortamos no me importa seguir mas
//				return;
			}
		}
		
//		board.print();
		
	}
	
	@Override
	public void solve() {
		
		long start = System.nanoTime();
		
		maxScore = 0;
		
		int best = 0, i = 0;
		
		int oldScore = 0;
		
		for (StackBoard board : boards) {
			
			// Cant be null. Otherwise dictionary would be empty 
			String firstWord = board.getDictionary().bestFirstOption(board.getCharacters(), MAX_LENGTH_WORD);
			
			if (firstWord == null) {
				continue;
			}
			
			int x = (board.size()-firstWord.length())/2;
			int y = board.size()/2;
			
			WordXY word = null;
			
			AddWordResult result = addWord(x, y, Direction.HORIZONTAL, firstWord, board); 
			
			if (!result.success) {
				
			} else {
				
				if (DEBUG) {
					List<Character> lj = getAvailableChars(board.characters);
					System.out.println("("+lj.size()+"): "+lj + " " + board.characters);
				}
				
				for (int j = 0; j < firstWord.length(); j++) {
					board.removeCharacter((Character)firstWord.charAt(j));
				}
				
				if (DEBUG) {
					List<Character> lj = getAvailableChars(board.characters);
					System.out.println("("+lj.size()+"): "+lj + " " + board.characters);
				}
				
//				board.print();
				
				firstDepth(board);
				
				i++;
			}
			
		}
		
		Collections.sort(this.boards, new Comparator<Board>() {
			@Override
			public int compare(Board o1, Board o2) {
				return o2.getScore()-o1.getScore();
			}
		});
		
		if (ANT && this.params.isVisual())
			this.boards.get(0).printSimple();
		
		if (STOCHASTIC) {
			hillClimbStochastic(boards.get(0));
		} else {
			hillClimb();
		}
		
//		preHillClimb(boards.get(0), 1);
				//this.boards.get(best).getDictionary(), this.boards.get(best), this._words.get(best));
		
		long end = System.nanoTime() - start; 
		System.out.println("Run Time: " + end/1000000.0 + "ms");
		
	}

}
