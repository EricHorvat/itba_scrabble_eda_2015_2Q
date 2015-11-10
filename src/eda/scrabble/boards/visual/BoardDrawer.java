package eda.scrabble.boards.visual;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import eda.scrabble.boards.logic.Board;
import eda.scrabble.boards.logic.Grid;
import eda.scrabble.elements.Word;

/**
 * Singleton que se encarga de la manipulacion de palabras en
 * los tableros
 * @author martin
 *
 */
public class BoardDrawer {

	private static BoardDrawer drawer = null;
	
	private BoardDrawer() {
		
	}
	
	/**
	 * Metodo de acceso para nuestro singleton
	 * @return
	 */
	public static BoardDrawer getCurrentBoardDrawer() {
		if (drawer == null) {
			drawer =  new BoardDrawer();
		}
		return drawer;
	}
	
	/**
	 * Metodo que me da vuelta un String
	 * @param s el string que quiero dar vuelta
	 * @return el string dado vuelta
	 */
	private static String reverse(String s) {
		String reverse = "";
		for (int k = s.length() -1; k >= 0; k--)
			reverse += s.charAt(k);
		return reverse;
	}
	
	/**
	 * Metodo que me agrega la palabra word en el Board grid si es posible.
	 * De no ser posible devuelve false y borra los rastros de la palabra
	 * @param word la palabra a agregar
	 * @param grid el tablero donde queremos agregar
	 * @return true <==> se agrego la palabra correctamente
	 */
	public boolean addWord(Word word, Board grid) {
		
		// Chequeo que este en los limites del tablero
		if (!grid.canHaveWord(word)) {
			return false;
		}
		
		// Distingo entre horizontal y vertical
		if (word.getVector().getDirection().isHorizontal()) {
			
			// Chequeo a mi izquierda
			if (grid.get(word.getVector().getPosition().getX()-1, word.getVector().getPosition().getY()) != Grid.EMPTY_SPACE) {				
				return false;
			}
			
			// Chequeo a mi derecha
			if (grid.get(word.getVector().getPosition().getX()+word.getWord().length(), word.getVector().getPosition().getY()) != Grid.EMPTY_SPACE) {
				return false;
			}
			int mask = 0;
			// Valido las condiciones caracter a caracter
			for (int i = word.getVector().getPosition().getX(); i < word.getVector().getPosition().getX()+word.getWord().length(); i++) {
				if (i - word.getVector().getPosition().getX() != word.getIntersectedIndex()) { 
				
					// Si es una interseccion tiene tratamiento distinto. Ya hay otra letra ahi
					boolean occupied = grid.isIntersection(i, word.getVector().getPosition().getY());
					// Flag de borrado de palabra
					boolean needsRemoval = false;
					
					boolean masked = false;
					
					if (word.getWord().charAt(i-word.getVector().getPosition().getX()) == grid.get(i, word.getVector().getPosition().getY())) {
//						mask |= 1 << (i - word.vec.pos.x);
//						masked = true;
					}
					
					// Chequeamos que no estemos pisando nada
					if (/*word.word.charAt(i-word.vec.pos.x) != grid.get(i, word.vec.pos.y) && */grid.get(i, word.getVector().getPosition().getY()) != Grid.EMPTY_SPACE) {
						// Marcamos para eliminar
						needsRemoval = true;
					}
					
					// Hay otra letra abajo mio. Veamos que se trate de una palabra valida
					if (!needsRemoval && grid.get(i, word.getVector().getPosition().getY()+1) != Grid.EMPTY_SPACE && !occupied) {
						// Armamos el string que se acaba de formar
						String s = "";
						int j = 0;
						while (grid.get(i, word.getVector().getPosition().getY()+j) != Grid.EMPTY_SPACE) {
							s += grid.get(i, word.getVector().getPosition().getY()+j);
							j++;
						}
						// verificamos que este en el diccionario
						if (!grid.getDictionary().contains(reverse(s))) {
							// Marcamos para eliminar
							needsRemoval = true;
						}
					}
				// Hay otra letra arriba mio. Veamos que se trate de una palabra valida
					if (!needsRemoval && grid.get(i, word.getVector().getPosition().getY()-1) != Grid.EMPTY_SPACE && !occupied) {
						// Armamos el string que se acaba de formar
						String s = "";
						int j = 0;
						while (grid.get(i, word.getVector().getPosition().getY()-j) != Grid.EMPTY_SPACE) {
							s += grid.get(i, word.getVector().getPosition().getY()-j);
							j++;
						}
						// verificamos que este en el diccionario
						if (!grid.getDictionary().contains(s)) {
							// Marcamos para eliminar
							needsRemoval = true;
						}
					}
					
					
					// Si hace falta eliminar
					if (needsRemoval) {
						for (int j = word.getVector().getPosition().getX(); j < i; j++){
							if (!grid.isIntersection(j, word.getVector().getPosition().getY())/* && ((mask & (1 << (j - word.vec.pos.x))) == 0 )*/) {
								grid.set(j, word.getVector().getPosition().getY(), Grid.EMPTY_SPACE);
							}
//							else if ( ((mask & (1 << (j - word.vec.pos.x))) != 0 ) ) {
//								grid.removeCharacter((Character)word.word.charAt(j - word.vec.pos.x));
//							}
						}
						return false;
					}
					
//					if (masked) {
//						grid.addCharacter((Character)word.word.charAt(i-word.vec.pos.x));
//					}
					
					// Setteamos el caracter en el tablero
					grid.set(i, word.getVector().getPosition().getY(), word.getWord().charAt(i-word.getVector().getPosition().getX()));
				}
			}
			
		} else { // Vertical
			
			// Chequeo arriba mio
			if (grid.get(word.getVector().getPosition().getX(), word.getVector().getPosition().getY()-1) != Grid.EMPTY_SPACE) {
				return false;
			}
			// Chequeo abajo mio 
			if (grid.get(word.getVector().getPosition().getX(), word.getWord().length()+word.getVector().getPosition().getY()) != Grid.EMPTY_SPACE) {
				return false;
			}
			int mask = 0;
			// Valido las condiciones caracter a caracter
			for (int i = word.getVector().getPosition().getY(); i < word.getVector().getPosition().getY()+word.getWord().length(); i++) {
				if (i - word.getVector().getPosition().getY() != word.getIntersectedIndex()) {
					
					// Si es una interseccion tiene tratamiento distinto. Ya hay otra letra ahi
					boolean isOccupied = grid.isIntersection(word.getVector().getPosition().getX(), i);
					// Flag de borrado de palabra
					boolean needsRemoval = false;
					
					boolean masked = false;
					
					if (word.getWord().charAt(i-word.getVector().getPosition().getY()) == grid.get(word.getVector().getPosition().getX(), i)) {
//						mask |= 1 << (i - word.vec.pos.y);
//						masked = true;
					}
					
					// Chequeamos que no estemos pisando nada
					if (/*word.word.charAt(i-word.vec.pos.y) != grid.get(word.vec.pos.x, i) && */grid.get(word.getVector().getPosition().getX(), i) != Grid.EMPTY_SPACE) {
						needsRemoval = true;
					}
					
					// Hay otra letra a mi derecha. Veamos que se trate de una palabra valida
					if (!needsRemoval && grid.get(word.getVector().getPosition().getX()+1, i) != Grid.EMPTY_SPACE && !isOccupied) {
					// Armamos el string a eliminar
						String s = "";
						int j = 0;
						while (grid.get(word.getVector().getPosition().getX()+j,i) != Grid.EMPTY_SPACE) {
							s += grid.get(word.getVector().getPosition().getX()+j,i);
							j++;
						}
						if (!grid.getDictionary().contains(s)) {
							// Sacar del tablero lo que quedo
							needsRemoval = true;
						}
					}
					
					// Hay otra letra a mi izquierda. Veamos que se trate de una palabra valida
					if (!needsRemoval && grid.get(word.getVector().getPosition().getX()-1, i) != Grid.EMPTY_SPACE && !isOccupied) {
						// Armamos el string a eliminar
						String s = "";
						int j = 0;
						while (grid.get(word.getVector().getPosition().getX()-j,i) != Grid.EMPTY_SPACE) {
							s += grid.get(word.getVector().getPosition().getX()-j,i);
							j++;
						}
						if (!grid.getDictionary().contains(reverse(s))) {
							needsRemoval = true;
						}
					}
					
				// Si hace falta eliminar
					if (needsRemoval) {
						for (int j = word.getVector().getPosition().getY(); j < i; j++) {
							if (!grid.isIntersection(word.getVector().getPosition().getX(), j)/* && ((mask & (1 << (j - word.vec.pos.y))) == 0 )*/) {
								grid.set(word.getVector().getPosition().getX(), j, Grid.EMPTY_SPACE);
							}
//							else if ( ((mask & (1 << (j - word.vec.pos.y))) != 0 ) ) {
//								grid.removeCharacter((Character)word.word.charAt(j - word.vec.pos.y));
//							}
						}
						return false;
					}
					
//					if (masked) {
//						grid.addCharacter((Character)word.word.charAt(i-word.vec.pos.y));
//					}
				// Setteamos el caracter en el tablero
					grid.set(word.getVector().getPosition().getX(), i, word.getWord().charAt(i-word.getVector().getPosition().getY()));
				}
			}
		}
		
		grid.addWord(word);
		
		return true;
	}
	
	/**
	 * Saca una palabra de la lista de palabras
	 * Luego, la remueve visualmente del tablero
	 * @param word la palabra a eliminar
	 * @param grid el tablero del cual sacar
	 * @return La palabra eliminada
	 */
	public Word removeWord(Word word, Board grid) {
		Word w = grid.removeWord(word);
		removeWordVisually(w, grid);
		return w;
	}
	
	/**
	 * Eliminar visualmente del tablero dejando espacios en blanco
	 * en el lugar donde estaban las letras de word
	 * @param word la palabra a borrar
	 * @param grid el tablero del cual borrar
	 */
	private void removeWordVisually(Word word, Board grid) {
		if (word.getVector().getDirection().isHorizontal()) {
			for (int i = word.getVector().getPosition().getX(); i < word.getWord().length() + word.getVector().getPosition().getX(); i++) {
				if (i - word.getVector().getPosition().getX() != word.getIntersectedIndex() && !grid.isIntersection(i, word.getVector().getPosition().getY())) {
					
					if (grid.get(i, word.getVector().getPosition().getY()+1) == Grid.EMPTY_SPACE &&
							grid.get(i, word.getVector().getPosition().getY()-1) == Grid.EMPTY_SPACE) {
						
						grid.addCharacter((Character) word.getWord().charAt(i - word.getVector().getPosition().getX()));
						grid.set(i, word.getVector().getPosition().getY(), Grid.EMPTY_SPACE);
					}
				}
			}
		} else {
			for (int i = word.getVector().getPosition().getY(); i < word.getWord().length()+word.getVector().getPosition().getY(); i++) {
				if (i - word.getVector().getPosition().getY() != word.getIntersectedIndex() &&
						!grid.isIntersection(word.getVector().getPosition().getX(), i)) {
					
					if (grid.get(word.getVector().getPosition().getX()+1, i) == Grid.EMPTY_SPACE &&
							grid.get(word.getVector().getPosition().getX()-1, i) == Grid.EMPTY_SPACE) {
						grid.addCharacter((Character) word.getWord().charAt(i - word.getVector().getPosition().getY()));
						grid.set(word.getVector().getPosition().getX(), i, Grid.EMPTY_SPACE);
					}
				}
			}
		}
		
	}
	
	
	/**
	 * Imprime por pantalla un tablero con
	 * cuadriculado y numero de columna
	 * @param board El tablero a mostrar
	 */
	public void printStyled(Board board) {
		
		System.out.print(" +");
		for (int i = 0; i < board.size(); i++) {
			int h = i %10; 
			System.out.print(h+"|");
		}
		System.out.println('+');
		System.out.print(" +");
		for (int i = 0; i < board.size()*2-1; i++)
			System.out.print('-');
		System.out.println('+');
		for (int i = 0; i < board.size(); i++) {
			int h = i%10;
			System.out.print(String.valueOf(h)+'|');
			for (int j = 0; j < board.size(); j++) {
				System.out.print(board.get(j, i)+"|");
			}
			System.out.println();
			System.out.print(" |");
			for (int j = 0; j < board.size()*2-1; j++) {
				System.out.print("-");
			}
			System.out.println("+");
		}
		System.out.print(" +");
		for (int i = 0; i < board.size(); i++) {
			int h = i %10; 
			System.out.print(h+"|");
		}
		System.out.println('+');
	}
	
	/**
	 * Imprime un tablero sin estilo
	 * @param board El tablero a imprimir
	 */
	public void printUnstyled(Board board) {
		for (int i = 0; i < board.size(); i++) {
			for (int j = 0; j < board.size(); j++) {
				System.out.print(board.get(j, i));
			}
			System.out.println();
		}
	}
	
	/**
	 * Escribe en outputFile el tablero sin estilo
	 * @param board El tablero a imprimir
	 * @param outputFile El archivo donde guardar
	 * @throws IOException
	 */
	public void printUnstyledDump(Board board, String outputFile) throws IOException {
		
		File fout = new File(outputFile);
		
		FileOutputStream fos = new FileOutputStream(fout);
		
		BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fos));
		
		for (int i = 0; i < board.size(); i++) {
			for (int j = 0; j < board.size(); j++) {
				bufferedWriter.write(board.get(j, i));			
			}
			bufferedWriter.newLine();
		}
		bufferedWriter.write("Max. Score: " + board.getScore());
		bufferedWriter.newLine();
		bufferedWriter.close();
	}
	
	
	/**
	 * Escribe en outputFile el tablero con estilo
	 * @param board El tablero a giardar
	 * @param outputFile El archivo donde guardar
	 * @throws IOException
	 */
	public void printStyledDump(Board board, String outputFile) throws IOException {
		
		File fout = new File(outputFile);
		
		FileOutputStream fos = new FileOutputStream(fout);
		
		BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fos));
		
		bufferedWriter.write(" +");
		for (int i = 0; i < board.size(); i++) {
			int h = i %10; 
			bufferedWriter.write(h+"|");
		}
		bufferedWriter.write('+');
		bufferedWriter.newLine();
		bufferedWriter.write(" +");
		for (int i = 0; i < board.size()*2-1; i++)
			bufferedWriter.write('-');
		bufferedWriter.write('+');
		bufferedWriter.newLine();
		for (int i = 0; i < board.size(); i++) {
			int h = i%10;
			bufferedWriter.write(String.valueOf(h)+'|');
			for (int j = 0; j < board.size(); j++) {
				bufferedWriter.write(board.get(j, i)+"|");
			}
			bufferedWriter.newLine();
			bufferedWriter.write(" |");
			for (int j = 0; j < board.size()*2-1; j++) {
				bufferedWriter.write('-');
			}
			bufferedWriter.write('+');
			bufferedWriter.newLine();
		}
		bufferedWriter.write(" +");
		for (int i = 0; i < board.size(); i++) {
			int h = i %10;
			bufferedWriter.write(h+"|");
		}
		bufferedWriter.write('+');
		bufferedWriter.newLine();
		
		bufferedWriter.write("Max. Score: " + board.getScore());
		bufferedWriter.newLine();
		bufferedWriter.close();
	}

}
