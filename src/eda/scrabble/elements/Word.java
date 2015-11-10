package eda.scrabble.elements;

/**
 * Representacion de una palabra en el tablero.
 * Cuenta con la palabra a representar, junto con
 * un Vector que nos indicar su ubicacio
 *  y direccion
 * @author martin
 *
 */
public class Word {

	/**
	 * La palabra que estamos tratando
	 */
	private String word;
	/**
	 * La posicion y direccion de la palabra
	 */
	private Vector vec;
	
	/**
	 * El indice en el cual se engancho esta palabra
	 */
	private int intersected;
	
	public Word(String word, Vector vec, int intersection) {
		this.word = word;
		this.vec = vec;
		this.intersected = intersection;
	}
	
	@Override
	public String toString() {
		return word+vec.getPosition().toString()+vec.getDirection();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + intersected;
		result = prime * result + ((vec == null) ? 0 : vec.hashCode());
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
		if (!(obj instanceof Word)) {
			return false;
		}
		Word other = (Word) obj;
		if (intersected != other.intersected) {
			return false;
		}
		if (vec == null) {
			if (other.vec != null) {
				return false;
			}
		} else if (!vec.equals(other.vec)) {
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

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public Vector getVector() {
		return vec;
	}

	public void setVector(Vector vec) {
		this.vec = vec;
	}

	public int getIntersectedIndex() {
		return intersected;
	}

	public void setIntersectedIndex(int intersected) {
		this.intersected = intersected;
	}
	
	

}
