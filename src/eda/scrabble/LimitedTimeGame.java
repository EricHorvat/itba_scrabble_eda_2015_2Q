package eda.scrabble;

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

	private List<Board> boards;
	
	private final static double T = 6.5;
	
	private final static boolean STOCHASTIC = true;
	
	public LimitedTimeGame(GameParameters params) {
		super(params);
		
		boards = new ArrayList<Board>(5);
		
		// Default Dictionary from Game class
		/** @see @{Game}  */
		boards.add(grid);
		
		for (int i = 0; i < 4; i++) {
			boards.add(new Board(grid.characters));
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
	
	private void preHillClimb(Board  board, int following) {
	//(Dictionary dictionary, Grid grid, Deque<WordXY> words) {
		
		// Saco la palabra del tablero y de la lista de palabras
		// Ademas me devuelve los caracters que saca
		
		// Vemos
//		WordXY lastWord = removeWord(board.getWords(), board);
		
		List<LetterXY> willUse = new ArrayList<LetterXY>();
		
		Iterator<WordXY> it = board.getWords().descendingIterator();	
		while (it.hasNext()) {
			WordXY next = it.next();
			
			if (next.direction == Direction.HORIZONTAL) {
				for (int j = next.pos.x; j < next.pos.x+next.word.length(); j++) {
					
					if (!board.isIntersection(j, next.pos.y)) {
						willUse.add(new LetterXY(next, (Character)next.word.charAt(j-next.pos.x), j-next.pos.x));
					}
				}
			} else {
				for (int j = next.pos.y; j < next.pos.y+next.word.length(); j++) {
					
					if (!board.isIntersection(next.pos.x, j)) {
						willUse.add(new LetterXY(next, (Character)next.word.charAt(j-next.pos.y), j-next.pos.y));
					}
				}
			}
			
		}
		
		
		System.out.println("Starting Hill Climb with index " + (following-1));
		
		System.out.println("size: " + board.getWords().size());
		
		if (STOCHASTIC) {
			hillClimbStochastic(board, willUse);
		} else {
			hillClimb(board, willUse, following);
		}
		
	}
	
	private void hillClimb(Board board, List<LetterXY> letters, int following) {
		if (board == null) return;
		
		System.out.println("Hill Climb");
		
		Board bestBoard = board;
		WordXY wordToAdd = null; 
		LetterXY bestLetter = null;
		
		int maxScore = board.getScore();
		
		String aux = null;
		
		for (LetterXY letter : letters) {
			
			do {
			
//			while (true) {
				
				board.addCharacter(letter.c);
				
				aux = board.getDictionary().bestLimitedOptionAfter(board.getCharacters(), MAX_LENGTH_WORD, letter.c, aux);
				
				board.removeCharacter(letter.c);
				
				if (aux != null) {
					
					
//					break;
//				}
				
					WordXY toAdd;
					
					int intersectionIndex = aux.indexOf(letter.c);
					
					if (letter.word.direction == Direction.HORIZONTAL) {
						board.markIntersection(letter.word.pos.x+letter.pos, letter.word.pos.y);
					} else {
						board.markIntersection(letter.word.pos.x, letter.word.pos.y+letter.pos);
					}
					
					try {
						if (letter.word.direction == Direction.HORIZONTAL) {
							toAdd = addWord(letter.word.pos.x+letter.pos, letter.word.pos.y-intersectionIndex, Direction.VERTICAL, aux, board, board.getWords(), board.getDictionary());
						} else {
							toAdd = addWord(letter.word.pos.x-intersectionIndex, letter.word.pos.y+letter.pos, Direction.HORIZONTAL, aux, board, board.getWords(), board.getDictionary());
						}
					} catch (AddWordException ex) {
						
						board.removeCharacter(letter.c);
						
						if (letter.word.direction == Direction.HORIZONTAL) {
							board.clearIntersection(letter.word.pos.x+letter.pos, letter.word.pos.y);
						} else {
							board.clearIntersection(letter.word.pos.x, letter.word.pos.y+letter.pos);
						}
						
						continue;
					}
					
					int score = board.getScore();
					
					if (score > maxScore) {
						maxScore = score;
						bestBoard = new Board(board);
						wordToAdd = toAdd;
						bestLetter = letter;
					}
					
					removeWord(board.getWords(), board);
					
					if (letter.word.direction == Direction.HORIZONTAL) {
						board.clearIntersection(letter.word.pos.x+letter.pos, letter.word.pos.y);
					} else {
						board.clearIntersection(letter.word.pos.x, letter.word.pos.y+letter.pos);
					}
				}
				
			} while (aux != null);
			
		}
		
		System.out.println("best board");
		bestBoard.print();
		
		if (bestBoard == board) {
			// Maximo Local
			System.out.println("maximo local");
			System.out.println("bestScore: " + bestBoard.getScore());
			System.out.println("switching to following: " +following+"/"+this.boards.size());
			if (following < this.boards.size()) {
				preHillClimb(this.boards.get(following), following+1);
			}
			return;
		}
		
		boolean first = false;
		
		for (int j = 0; j < wordToAdd.word.length(); j++) {
			if (!first && wordToAdd.word.charAt(j) == (char)bestLetter.c) {
				first = true;
				letters.remove(bestLetter);
			} else {
				letters.add(new LetterXY(wordToAdd, (Character)wordToAdd.word.charAt(j), j));
			}
		}
		
		
		
		hillClimb(bestBoard, letters, following);
		
	}
	
	
	private void hillClimbStochastic(Board board, List<LetterXY> letters) {
		
		if (board == null) return;
		
		System.out.println("Hill Climb Stochastic");
		
		Board bestBoard = board;
		WordXY wordToAdd = null; 
		LetterXY bestLetter = null;
		
		int initScore = board.getScore();
		
		System.out.println("init score: " + initScore);
		
		String aux = null;
		
		boolean found = false;
		
		for (LetterXY letter : letters) {
			
			do {
			
//			while (true) {
				
				board.addCharacter(letter.c);
				
				aux = board.getDictionary().bestLimitedOptionAfter(board.getCharacters(), MAX_LENGTH_WORD, letter.c, aux);
				
				board.removeCharacter(letter.c);
				
				if (aux == null) {
//					break;
//				}
				
					WordXY toAdd;
					
					int intersectionIndex = aux.indexOf(letter.c);
					
					if (letter.word.direction == Direction.HORIZONTAL) {
						board.markIntersection(letter.word.pos.x+letter.pos, letter.word.pos.y);
					} else {
						board.markIntersection(letter.word.pos.x, letter.word.pos.y+letter.pos);
					}
					
					try {
						if (letter.word.direction == Direction.HORIZONTAL) {
							toAdd = addWord(letter.word.pos.x+letter.pos, letter.word.pos.y-intersectionIndex, Direction.VERTICAL, aux, board, board.getWords(), board.getDictionary());
						} else {
							toAdd = addWord(letter.word.pos.x-intersectionIndex, letter.word.pos.y+letter.pos, Direction.HORIZONTAL, aux, board, board.getWords(), board.getDictionary());
						}
					} catch (AddWordException ex) {			
						
	//					System.out.println(ex.getMessage());
						
						board.removeCharacter(letter.c);
						
						if (letter.word.direction == Direction.HORIZONTAL) {
							board.clearIntersection(letter.word.pos.x+letter.pos, letter.word.pos.y);
						} else {
							board.clearIntersection(letter.word.pos.x, letter.word.pos.y+letter.pos);
						}
						
						continue;
					}
					
					
					int score = board.getScore();
					
					
					System.out.println("score: " + score);
					System.out.println("diff: " + (initScore - score));
					
					double p = 1 / ( 1 + Math.pow(Math.E, (initScore - score) / T ) );
					
					double rnd = Math.random();
					System.out.println(p + " " + rnd);
					if ( p > rnd) {
						
						bestBoard = new Board(board);
						wordToAdd = toAdd;
						bestLetter = letter;
						
						found = true;
						
						break;
						
					}
					
					removeWord(board.getWords(), board);
					
					if (letter.word.direction == Direction.HORIZONTAL) {
						board.clearIntersection(letter.word.pos.x+letter.pos, letter.word.pos.y);
					} else {
						board.clearIntersection(letter.word.pos.x, letter.word.pos.y+letter.pos);
					}
				}
			} while (aux != null);
			
			if (found)
				break;
			
		}
		
		System.out.println("best board");
		bestBoard.print();
		System.out.println("bestScore: " + bestBoard.getScore());
		
		if (wordToAdd == null) {
			System.out.println("wordToAdd=null");
			System.out.println(board.getIntersections());
			board.print();
			try {
				WordXY removed = removeWord(board.getWords(), board);
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
			} catch (NoSuchElementException ex) {
				List<String> dictionaryWords = board.getDictionary().getWords(); 
				String emergency = dictionaryWords.get( ((int)(Math.random() * 1000)) % dictionaryWords.size() );
				if (DEBUG) System.out.println("remain chars ("+getAvailableChars(board.characters).size()+")"+ getAvailableChars(board.characters));
				WordXY w = null;
				try {
					w = addWord((board.size()-emergency.length())/2, grid.size()/2, Direction.HORIZONTAL, emergency, board, board.getWords(), board.getDictionary());
				} catch (AddWordException err) {
					if (DEBUG) System.out.println(err.getMessage());
				}
				letters.clear();
				for (int j = 0; j < w.word.length(); j++) {
					letters.add(new LetterXY(w, (Character)w.word.charAt(j), j));
				}
			}
			hillClimbStochastic(board, letters);
			return;
		}
		
		boolean first = false;
		
		for (int j = 0; j < wordToAdd.word.length(); j++) {
			if (!first && wordToAdd.word.charAt(j) == (char)bestLetter.c) {
				first = true;
				letters.remove(bestLetter);
			} else {
				letters.add(new LetterXY(wordToAdd, (Character)wordToAdd.word.charAt(j), j));
			}
		}
		
		
		hillClimbStochastic(bestBoard, letters);
		
	}
	
	/**
	 * Va agregando en profundidad por la primera palabra que puede conseguir
	 * @param word La palabra en la cual arrancar
	 */
	private void firstDepth(Board board) {
			//Dictionary dictionary, WordXY word, Grid grid, Deque<WordXY> words) {
		WordXY word = board
				.getWords()
				.peek();
		
		if (word == null) return;
		
		String aux = null;
		WordXY toAdd = null;
		
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
			
			board.addCharacter(intersectionChar);
			
			aux = board.getDictionary().bestFirstLimitedOption(board.getCharacters(), MAX_LENGTH_WORD, intersectionChar);
			
			board.removeCharacter(intersectionChar);
			
			if (aux == null) {
			
				continue;
			}
			
			int intersectionIndex = aux.indexOf(intersectionChar);
			
			if (word.direction == Direction.HORIZONTAL) {
				board.markIntersection(word.pos.x+i, word.pos.y);
			} else {
				board.markIntersection(word.pos.x, word.pos.y+i);
			}
			
			try {
				if (word.direction == Direction.HORIZONTAL) {
					toAdd = addWord(word.pos.x+i, word.pos.y-intersectionIndex, Direction.VERTICAL, aux, board, board.getWords(), board.getDictionary());
				} else {
					toAdd = addWord(word.pos.x-intersectionIndex, word.pos.y+i, Direction.HORIZONTAL, aux, board, board.getWords(), board.getDictionary());
				}
			} catch (AddWordException ex) {
				
				board.removeCharacter(intersectionChar);
				
				if (word.direction == Direction.HORIZONTAL) {
					board.clearIntersection(word.pos.x+i, word.pos.y);
				} else {
					board.clearIntersection(word.pos.x, word.pos.y+i);
				}
				
				continue;
			}
			
			int score = board.getScore();
			if (score > maxScore)
				maxScore = score;
			
			firstDepth(board);
			
			// Cortamos no me importa seguir mas
			return;
			
		}
		
//		board.print();
		
		System.out.println("Max Score:" + board.getScore());
		
	}
	
	@Override
	public void solve() {
		
		long start = System.nanoTime();
		
		maxScore = 0;
		
		int best = 0, i = 0;
		
		int oldScore = 0;
		
		for (Board board : boards) {
		
			System.out.println("##############");
			System.out.println("--- " + board.getDictionary().getDictionaryFillStrategy());
			System.out.println("##############");
			
			System.out.println(board.getDictionary().getWords());
			
			// Cant be null. Otherwise dictionary would be empty 
			String firstWord = board.getDictionary().bestFirstOption(board.getCharacters(), MAX_LENGTH_WORD);
			
			System.out.println(firstWord);
			
			if (firstWord == null) {
				System.out.println("firstWord is null");
				continue;
			}
			
			int x = (board.size()-firstWord.length())/2;
			int y = board.size()/2;
			
			WordXY word = null;
			
			try {
				word = addWord(x, y, Direction.HORIZONTAL, firstWord, board, board.getWords(), board.getDictionary());
			} catch (AddWordException e) {
			}
			
			firstDepth(board);
			
			i++;
		}
		
		Collections.sort(this.boards, new Comparator<Board>() {
			@Override
			public int compare(Board o1, Board o2) {
				return o2.getScore()-o1.getScore();
			}
		});
		
		System.out.println("Score:" + this.boards.get(0).getScore());
		this.boards.get(0).print();
		
		preHillClimb(boards.get(0), 1);
				//this.boards.get(best).getDictionary(), this.boards.get(best), this._words.get(best));
		
		long end = System.nanoTime() - start; 
		System.out.println("Run Time: " + end/1000000.0 + "ms");
		
	}

}
