package eda.scrabble;

import eda.scrabble.Game.Direction;

public class Word {

	public String word;
	public Vector vec;
	
	public int intersected;
	
	public Word(String word, Vector vec, int intersection) {
		this.word = word;
		this.vec = vec;
		this.intersected = intersection;
	}
	
	public boolean has(int x, int y) {
		if (this.vec.dir == Direction.HORIZONTAL) {
			return this.vec.pos.y == y && this.vec.pos.x <= x && x <= this.vec.pos.x + word.length();
		} else {
			return this.vec.pos.x == x && this.vec.pos.y <= y && y <= this.vec.pos.y + word.length();
		}
	}
	
	public boolean has(Coordinate coord) {
		return has(coord.x, coord.y);
	}
	
	@Override
	public String toString() {
		return word+"("+vec.pos.x+","+vec.pos.y+")"+vec.dir;
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
	
	

}
