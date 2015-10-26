package eda.scrabble;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import eda.scrabble.Game.Direction;
import eda.scrabble.Game.GameParameters;
import eda.scrabble.Game.WordXY;
import eda.scrabble.file.InputData;

public class LimitedTimeGame extends Game {

	private List<Board> boards;
	
	private final static int T = 2;
	
	private final static boolean STOCHASTIC = false;
	
	public LimitedTimeGame(GameParameters params) {
		super(params);
		
		boards = new ArrayList<Board>(5);
		
		for (int i = 0; i < 5; i++) {
			boards.add(new Board(grid.characters));
		}
		
		boards.get(0)
		.setDictionary(dictionary);
		
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
		
		List<LetterXY> willUse = new ArrayList<>();
	
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
		
		board.print();
		
		if (STOCHASTIC) {
			hillClimbStochastic(board, willUse, following);
		} else {
			hillClimb(board, willUse, following);
		}
		
	}
	
	private void hillClimb(Board board, List<LetterXY> letters, int following) {
		if (board == null) return;
		
		System.out.println("Hill Climb");
		System.out.println(getAvailableChars(board.getCharacters()));
		System.out.println(letters);
		
		Board bestBoard = board;
		WordXY wordToAdd = null; 
		LetterXY bestLetter = null;
		
		int maxScore = board.getScore();
		
		String aux = null;
		
		for (LetterXY letter : letters) {
			
			while (true) {
				
				board.addCharacter(letter.c);
				
				aux = board.getDictionary().bestLimitedOptionAfter(board.getCharacters(), MAX_LENGTH_WORD, letter.c, aux);
				
				board.removeCharacter(letter.c);
				
				if (aux == null) {
					
					
					break;
				}
				
				WordXY toAdd;
				
				int intersectionIndex = aux.indexOf(letter.c);
				
				if (letter.word.direction == Direction.HORIZONTAL) {
					board.markIntersection(letter.word.pos.x+letter.pos, letter.word.pos.y);
				} else {
					board.markIntersection(letter.word.pos.x, letter.word.pos.y+letter.pos);
				}
				
				try {
					if (letter.word.direction == Direction.HORIZONTAL) {
						toAdd = addWord(letter.word.pos.x+letter.pos, letter.word.pos.y-intersectionIndex, Direction.VERTICAL, aux, board, board.getWords());
					} else {
						toAdd = addWord(letter.word.pos.x-intersectionIndex, letter.word.pos.y+letter.pos, Direction.HORIZONTAL, aux, board, board.getWords());
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
				
				if (letter.word.direction == Direction.HORIZONTAL) {
					board.clearIntersection(letter.word.pos.x+letter.pos, letter.word.pos.y);
				} else {
					board.clearIntersection(letter.word.pos.x, letter.word.pos.y+letter.pos);
				}
				
				
			}
			
		}
		
		System.out.println("best board");
		bestBoard.print();
		
		if (bestBoard == board) {
			// Maximo Local
			System.out.println("maximo local");
			System.out.println("bestScore: " + bestBoard.getScore());
//			if (following < this.boards.size()-1)
//				preHillClimb(this.boards.get(following), following+1);
			return;
		}
		
		boolean first = false;
		
		for (int j = 0; j < wordToAdd.word.length(); j++) {
			if (!first && wordToAdd.word.charAt(j) != bestLetter.c) {
				first = true;
				letters.remove(bestLetter);
			} else {
				letters.add(new LetterXY(wordToAdd, wordToAdd.word.charAt(j), j));
			}
		}
		
		
		
		hillClimb(bestBoard, letters, following);
		
	}
	
	
	private void hillClimbStochastic(Board board, List<LetterXY> letters, int following) {
		
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
			
			while (true) {
				
				board.addCharacter(letter.c);
				
				aux = board.getDictionary().bestLimitedOptionAfter(board.getCharacters(), MAX_LENGTH_WORD, letter.c, aux);
				
				board.removeCharacter(letter.c);
				
				if (aux == null) {
					
					
					break;
				}
				
				WordXY toAdd;
				
				int intersectionIndex = aux.indexOf(letter.c);
				
				if (letter.word.direction == Direction.HORIZONTAL) {
					board.markIntersection(letter.word.pos.x+letter.pos, letter.word.pos.y);
				} else {
					board.markIntersection(letter.word.pos.x, letter.word.pos.y+letter.pos);
				}
				
				try {
					if (letter.word.direction == Direction.HORIZONTAL) {
						toAdd = addWord(letter.word.pos.x+letter.pos, letter.word.pos.y-intersectionIndex, Direction.VERTICAL, aux, board, board.getWords());
					} else {
						toAdd = addWord(letter.word.pos.x-intersectionIndex, letter.word.pos.y+letter.pos, Direction.HORIZONTAL, aux, board, board.getWords());
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
				
				
				System.out.println("score: " + score);
				
				// TODO: Probar T
				double p = 1 / ( 1 + Math.pow(Math.E, (initScore - score) / T ) );
				System.out.println(p);
				if ( p  > 0.5f  ) {
					
					bestBoard = new Board(board);
					wordToAdd = toAdd;
					bestLetter = letter;
					
					found = true;
					
					break;
					
				}
				
				if (letter.word.direction == Direction.HORIZONTAL) {
					board.clearIntersection(letter.word.pos.x+letter.pos, letter.word.pos.y);
				} else {
					board.clearIntersection(letter.word.pos.x, letter.word.pos.y+letter.pos);
				}
				
				
			}
			
			if (found)
				break;
			
		}
		
		System.out.println("best board");
		bestBoard.print();
		System.out.println("bestScore: " + bestBoard.getScore());
		
//		if (bestBoard == board) {
//			// Maximo Local
//			System.out.println("maximo local");
//			if (following < this.boards.size()-1)
//				preHillClimb(this.boards.get(following), following+1);
//			return;
//		}
		
		if (wordToAdd == null) {
			return;
		}
		
		boolean first = false;
		
		for (int j = 0; j < wordToAdd.word.length(); j++) {
			if (!first && wordToAdd.word.charAt(j) != bestLetter.c) {
				first = true;
				letters.remove(bestLetter);
			} else {
				letters.add(new LetterXY(wordToAdd, wordToAdd.word.charAt(j), j));
			}
		}
		
		
		
		hillClimbStochastic(bestBoard, letters, following);
		
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
			
			Character intersectionChar = (Character)word.word.charAt(i);
			
			board.addCharacter(intersectionChar);
			
			aux = board.getDictionary().bestFirstLimitedOption(board.getCharacters(), MAX_LENGTH_WORD, intersectionChar);
			
			System.out.println("aux: " + aux);
			
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
					toAdd = addWord(word.pos.x+i, word.pos.y-intersectionIndex, Direction.VERTICAL, aux, board, board.getWords());
				} else {
					toAdd = addWord(word.pos.x-intersectionIndex, word.pos.y+i, Direction.HORIZONTAL, aux, board, board.getWords());
				}
			} catch (AddWordException ex) {
				
				System.out.println(ex.getMessage());
				
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
			
			board.print();
			
			firstDepth(board);
			
			// Cortamos no me importa seguir mas
			return;
			
		}
		
		System.out.println("Max Score:" + maxScore);
		
	}
	
	@Override
	public void solve() {
		
		long start = System.nanoTime();
		
		maxScore = 0;
		
		int best = 0, i = 0;
		
		int oldScore = 0;
		
		Board board = this.boards.get(0);
		
		System.out.println("##############");
		System.out.println("--- " + board.getDictionary().getDictionaryFillStrategy());
		System.out.println("##############");
		
		
		System.out.println(board.getDictionary().getWords());
		
		// Cant be null. Otherwise dictionary would be empty 
		String firstWord = dictionary.bestFirstOption(board.getCharacters(), MAX_LENGTH_WORD);
		
		System.out.println(firstWord);
		
		int x = (board.size()-firstWord.length())/2;
		int y = board.size()/2;
		
		WordXY word = null;
		
		try {
			word = addWord(x, y, Direction.HORIZONTAL, firstWord, board, board.getWords());
		} catch (AddWordException e) {
		}
		
		firstDepth(board);
		
//		for (Board board : boards) {
//		
//			System.out.println("##############");
//			System.out.println("--- " + board.getDictionary().getDictionaryFillStrategy());
//			System.out.println("##############");
//			
//			
//			System.out.println(board.getDictionary().getWords());
//			
//			// Cant be null. Otherwise dictionary would be empty 
//			String firstWord = dictionary.bestFirstOption(board.getCharacters(), MAX_LENGTH_WORD);
//			
//			System.out.println(firstWord);
//			
//			if (firstWord == null)
//				continue;
//			
//			int x = (board.size()-firstWord.length())/2;
//			int y = board.size()/2;
//			
//			WordXY word = null;
//			
//			try {
//				word = addWord(x, y, Direction.HORIZONTAL, firstWord, board, board.getWords());
//			} catch (AddWordException e) {
//			}
//			
//			firstDepth(board);
//			
//			i++;
//		}
//		
//		this.boards.sort(new Comparator<Board>() {
//			@Override
//			public int compare(Board o1, Board o2) {
//				return o1.getScore()-o2.getScore();
//			}
//		});
		
		preHillClimb(board, 1);
				//this.boards.get(best).getDictionary(), this.boards.get(best), this._words.get(best));
		
		long end = System.nanoTime() - start; 
		System.out.println("Run Time: " + end/1000000.0 + "ms");
		
	}

}
