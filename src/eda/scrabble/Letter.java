package eda.scrabble;

public class Letter {

	Word word;
	Character c;
	int pos;
	
	public Letter(Word word, Character c, int pos) {
		this.word = word;
		this.c = c;
		this.pos = pos;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Letter)) return false;
		Letter other = (Letter) obj;
		if (!this.word.equals(other.word)) return false;
		if (this.pos != other.pos) return false;
		if (this.c != other.c) return false;
		return true;
	}
	
	@Override
	public String toString() {
		return word.toString()+" " + c + "["+pos+"]";
	}
	
	@Override
	public int hashCode() {
		return word.hashCode()+c.hashCode()+pos;
	}

}
