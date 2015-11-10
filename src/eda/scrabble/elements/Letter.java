package eda.scrabble.elements;

/**
 * Representacion de una letra en el tablero
 * Esta pertenece a una palabra.
 * Esta asociada a un caracter.
 * Y cuenta con su posicion en la palabra
 * 
 * Se utiliza para acumular las letras donde se pueden
 * insertar palabras
 * @author martin
 *
 */
public class Letter {

	private Word word;
	private Character c;
	private int pos;
	
	public Letter(Word word, Character c, int pos) {
		this.word = word;
		this.c = c;
		this.pos = pos;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((c == null) ? 0 : c.hashCode());
		result = prime * result + pos;
		result = prime * result + ((word == null) ? 0 : word.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Letter)) {
			return false;
		}
		Letter other = (Letter) obj;
		if (c == null) {
			if (other.c != null) {
				return false;
			}
		} else if (!c.equals(other.c)) {
			return false;
		}
		if (pos != other.pos) {
			return false;
		}
		if (word == null) {
			if (other.word != null) {
				return false;
			}
		} else if (!word.equals(other.word)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return word.toString()+" " + c + "["+pos+"]";
	}

	public Word getWord() {
		return word;
	}

	public void setWord(Word word) {
		this.word = word;
	}

	public Character getCharacter() {
		return c;
	}

	public void setCharacter(Character c) {
		this.c = c;
	}

	public int getPosition() {
		return pos;
	}

	public void setPosition(int pos) {
		this.pos = pos;
	}
	
	
	

}
