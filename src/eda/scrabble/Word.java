package eda.scrabble;

import eda.scrabble.Game.Direction;

public class Word {

	public String word;
	public Coordinate pos;
	public Direction direction;
	
	public Word(String word, Coordinate pos, Direction d) {
		this.word = word;
		this.pos = pos;
		this.direction = d;
	}
	
	public boolean has(int x, int y) {
		if (this.direction == Direction.HORIZONTAL) {
			return this.pos.y == y && this.pos.x <= x && x <= this.pos.x + word.length();
		} else {
			return this.pos.x == x && this.pos.y <= y && y <= this.pos.y + word.length();
		}
	}
	
	public boolean has(Coordinate coord) {
		return has(coord.x, coord.y);
	}
	
	@Override
	public String toString() {
		return word+"("+pos.x+","+pos.y+")"+direction;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Word)) return false;
		Word other = (Word) obj;
		if (!this.word.equals(other.word)) return false;
		if (!this.pos.equals(other.pos)) return false;
		if (this.direction != other.direction) return false;
		return true;
		
	}
	
	@Override
	public int hashCode() {
		
		return pos.hashCode()+word.hashCode()*(direction == Direction.HORIZONTAL ? 1 : -1);
	}

}
