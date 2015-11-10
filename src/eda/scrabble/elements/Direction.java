package eda.scrabble.elements;

/**
 * Representacion de una direccion de insertado de una palabra
 * Puede ser Horizontal o Vertical
 * @author martin
 *
 */
public enum Direction {

	HORIZONTAL,
	VERTICAL;
	
	/**
	 * Obtiene la direccion opuesta
	 * @return la direccion opuesta
	 */
	public Direction getOpposite() {
		if (this == HORIZONTAL) {
			return VERTICAL;
		}
		return HORIZONTAL;
	}
	
	/**
	 * Me dice si es horizontal 
	 * @return true <==> es horizontal
	 */
	public boolean isHorizontal() {
		return this == Direction.HORIZONTAL;
	}
	
	/**
	 * Me dice si es vertical
	 * @return true <==> es vertical
	 */
	public boolean isVertical() {
		return this == Direction.VERTICAL;
	}
	
}
