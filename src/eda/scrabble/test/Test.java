package eda.scrabble.test;

import static org.junit.Assert.*;
import eda.scrabble.ExactGame;
import eda.scrabble.Game;
import eda.scrabble.LimitedTimeGame;
import eda.scrabble.Game.GameParameters;
import eda.scrabble.Main;

public class Test {

	@org.junit.Test
	public void test() {
		String s[] = {"Dict1.txt", "Let1.txt", "OutTest.txt", "-maxtime", "0.0010"}; 
		testGame(STOCHASTIC,s);		
	}

	private final String EXACT = "Ex";
	private final String HILLCLIMB = "Hi";
	private final String STOCHASTIC = "St";
	
	public void testGame(String opc, String[] args){
		Game game;
		GameParameters params = Main.parseParameters(args);
		
		if (params == null) {
			fail("Error de Parseo de Parametros");		
		}
		
		long start = System.nanoTime();
		
		switch(opc){
		
		case HILLCLIMB:
			LimitedTimeGame.STOCHASTIC = false;
			game = new LimitedTimeGame(params);
			break;
		case STOCHASTIC:
			LimitedTimeGame.STOCHASTIC = false;
			game = new LimitedTimeGame(params);
			break;
		case EXACT:
		default:
			game = new ExactGame(params);
			break;
		
		}
		
		game.start();
		
		long end = System.nanoTime() - start; 
		System.out.println("Run Time: " + end/1000000.0 + "ms");
	}
}
