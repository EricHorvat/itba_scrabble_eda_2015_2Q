package eda.scrabble;

import java.util.ArrayList;
import java.util.List;

import eda.scrabble.Game.Direction;
import eda.scrabble.Game.LetterXY;
import eda.scrabble.Game.WordXY;
import eda.scrabble.file.InputData;

public class ExactGame extends Game {
	
	
	
	public ExactGame() {
		super();
	}
	
	private void possibleSolution1(List<LetterXY> used) {
		
		String aux = null;
		WordXY toAdd = null;
		
		LetterXY letter = null;
		
		if (DEBUG) printUsed(used);
		
		// Backup used letters for restauration later
		List<LetterXY> backup = new ArrayList<>(grid.size()*grid.size());
		for (LetterXY l : used)
			backup.add(l);
		
		// Loop through @{used}. It contains the letters that will be analyzed
		// Basically Letters\IntersectedLetters
		for (int i = 0; i < used.size(); i++) {
		
			letter = used.get(i);
			
			// Lookup all words available for inserting with letter=used.get(i)
			while (true) {
				
				// Show available chars
				if (DEBUG) {
					List<Character> l = getAvailableChars();
					System.out.println("("+l.size()+"): "+l);
				}
				
				// Character in the intersection should be added to available chars
				// since it isnt available but next word will contain this letter
				addCharacter(letter.c);
				
				if (DEBUG) System.out.println("Adding " + letter.c + " for search");
				
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
				aux = dictionary.bestLimitedOptionAfter(characters, MAX_LENGTH_WORD, letter.c, aux);
				
				if (DEBUG) System.out.println("/SEARCH/"+letter.word.word+"/"+ letter.c + "/"+aux);
				if (DEBUG) System.out.println("Removing " + letter.c + ". Already searched");
				
				// Since we added letter.c we should remove it
				removeCharacter(letter.c);
				
				// Si aux == null ==> con este caracter no hay mas palabras para buscar
				if (aux == null) {
					
					// No hay mas palabras con esta letra (letter.c)
					// ==> pasamos a la proxima
					break;
				}
				
				
				// Hacemos la matematica para que en la grilla se coloque todo prolijo
				// y en su lugar
				int intersectionIndex = aux.indexOf(letter.c);
				int letterIndex = letter.word.word.indexOf(letter.c);
				
				// Marcamos a la interseccion como lugar ocupado
				if (letter.word.direction == Direction.HORIZONTAL) {
					markOccupied(letter.word.pos.x+letterIndex, letter.word.pos.y);
				} else {
					markOccupied(letter.word.pos.x, letter.word.pos.y+letterIndex);
				}
				
				// Attempt to add word
				try {
					if (letter.word.direction == Direction.HORIZONTAL) {
						toAdd = addWord(letter.word.pos.x+letterIndex, letter.word.pos.y-intersectionIndex, Direction.VERTICAL, aux);
					} else {
						toAdd = addWord(letter.word.pos.x-intersectionIndex, letter.word.pos.y+letterIndex, Direction.HORIZONTAL, aux);
					}
					
				} catch (IllegalArgumentException ex) {
					// Reseteamos porque la palabra no se pudo agregar
					if (DEBUG) System.out.println(aux + " was not added. " + ex.getMessage());
					
					// Marcamos el lugar que habiamos marcado como ocupado
					// como libre porque no agregamos la palabra
					if (letter.word.direction == Direction.HORIZONTAL) {
						markAvailable(letter.word.pos.x+letterIndex, letter.word.pos.y);
					} else {
						markAvailable(letter.word.pos.x, letter.word.pos.y+letterIndex);
					}
					
					// Devolvemos los caracteres que no se agregaron al conjunto de caracteres
					// disponibles
					// OjO, menos el de la interseccion porque lo usa la palabra anterior
					for (int j = 0; j < aux.length(); j++) {
						Character c = (Character)aux.charAt(j);
						if (j != intersectionIndex) {
							addCharacter(c);
						}
					}
					
					// Seguimos y probamos si la proxima palabra calza en este lugar
					continue;
				}
				
				
				
				// Sacamos a @{letter} de @{used} para que cuando entre en la recursiva
				// No se ponga a buscar palabras para calzar en la interseccion
				used.remove(letter);
				
				
				// Nos fijamos si este es mejor tablero que el anterior mejor
				// De ser asi actualizamos
				int score = grid.getScore();
				if (score > maxScore)
					maxScore = score;
				
				//TODO: Aca cuando implementemos Ant y toda la bola habria que preguntar
				//     por la opcion --display 
				// Mostramos el tablero
				grid.printSimple();
				
				
				// Agregamos los caracteres que en la recursiva se van a usar para buscar
				// mas palabras
				for (int j = 0; j < aux.length(); j++) {
					Character c = (Character)aux.charAt(j);
					if (j != intersectionIndex) {
						used.add(new LetterXY(toAdd, c));
					}
				}
				
				// Perform recursive call to same method
				possibleSolution1(used);
				
				/*
				 * #################
				 * --- Cleanup Stage
				 * #################
				 */
				
				
				// Borramos la palabra que acabamos de agregar al tablero
				removeWord(toAdd);
				
				// Marcamos la interseccion como disponible porque ya que sacamos la palabra
				// no hay mas interseccion
				if (letter.word.direction == Direction.HORIZONTAL) {
					markAvailable(letter.word.pos.x+letterIndex, letter.word.pos.y);
				} else {
					markAvailable(letter.word.pos.x, letter.word.pos.y+letterIndex);
				}
				
				
				// Restauramos la informacion para que el proximo ciclo haga su trabajo
				used.clear();
				for (LetterXY l: backup)
					used.add(l);
				
				
				// Reponemos los caracteres que acabamos de consumir
				for (int j = 0; j < aux.length(); j++) {
					if (j != intersectionIndex)
						addCharacter((Character)aux.charAt(j));
				}
				
			}
			
			// No deberia suceder, pero chequeamos igual por si las moscas
			if (letter.c != used.get(i).c) {
				System.err.println("######################\n---------------Mismatch\n#################");
				throw new IllegalAccessError(letter.c + " should be equal to " + used.get(i).c);
			}
			
		}
		
	}
	
	/**
	 * Places each of the words in all posible starting positions
	 * And calls the solver methods.
	 */
	@Override
	public void solve() {
		maxScore = 0;
		
		List<String> allWords = dictionary.getWords();
		
		// Reservo a lo sumo grid.size()^2 de letras
		List<LetterXY> l = new ArrayList<>(grid.size()*grid.size());
		
		WordXY tmp = null;
		
		for (String w : allWords) {
			if (DEBUG) {
				System.out.println("########################################");
				System.out.println("------------ Starting from scratch");
				System.out.println("########################################");
			}
			if (DEBUG) {
				List<Character> li = getAvailableChars();
				System.out.println("Available chars("+li.size()+"): " + li);
			}
			
			int x = grid.size()/2-w.length()+1;
			int y = grid.size()/2;
			
			
			// Throws Exception but board should be empty
			tmp = addWord(x, y, Direction.HORIZONTAL, w);
			
			// Mark characters as used
			for (char c : w.toCharArray()) {
				removeCharacter(c);
			}
			
			if (DEBUG)
				System.out.println("Printing initial board");
			grid.print();
			
			// Probamos todas las posiciones posibles en el eje x
			for (int i = x+1; i <= grid.size()/2; i++) {
				for (char c : w.toCharArray()) {
					l.add(new LetterXY(tmp, (Character)c));
				}
				possibleSolution1(l);
				while (words.size() > 0)
					removeWord(words.get(words.size()-1));
				tmp = addWord(i, y, Direction.HORIZONTAL, w);
				l.clear();
			}
			
			for (char c : w.toCharArray()) {
				addCharacter((Character)c);
			}
			
			removeWord(words.get(words.size()-1));
		}
		
		System.out.println("Max Score is: " + maxScore);
	}

}
