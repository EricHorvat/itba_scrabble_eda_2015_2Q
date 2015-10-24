package eda.scrabble;

public class Main {

	private final static boolean EXACT_GAME = false;
	
	public static void main(String[] args) {
		
		Game game;
		
		if (EXACT_GAME) {
			
			game = new ExactGame();
		
		} else {
			
			game = new LimitedTimeGame();
			
		}
		
		game.start();
	}
	

}
