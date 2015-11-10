package eda.scrabble.elements;

public class Coordinate {

	private int x;
	private int y;

	public Coordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public int hashCode() {
		int result = (int) (Math.pow(2, x) * Math.pow(3, y));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Coordinate other = (Coordinate) obj;
		if (x != other.x) return false;
		if (y != other.y) return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "("+x+","+y+")";
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	

}
