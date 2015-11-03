package eda.scrabble;

import eda.scrabble.Game.Direction;

public class Vector {

	public Coordinate pos;
	public Direction dir;
	
	public Vector(Coordinate pos, Direction dir) {
		this.pos = pos;
		this.dir = dir;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dir == null) ? 0 : dir.hashCode());
		result = prime * result + ((pos == null) ? 0 : pos.hashCode());
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
		if (!(obj instanceof Vector)) {
			return false;
		}
		Vector other = (Vector) obj;
		if (dir != other.dir) {
			return false;
		}
		if (pos == null) {
			if (other.pos != null) {
				return false;
			}
		} else if (!pos.equals(other.pos)) {
			return false;
		}
		return true;
	}
	
	

}
