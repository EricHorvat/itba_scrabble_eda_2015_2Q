package eda.scrabble;

import eda.scrabble.Game.GameParameters;

public class Main {
	
	// Para testeo es public
	public static GameParameters parseParameters(String[] args) {
		
		GameParameters params = new GameParameters();
		//dict
		try {
			if (args[0].trim().equals("")) {
				return null; 
			}
		} catch (IndexOutOfBoundsException ex) {
			return null;
		}
		params.setDictionaryFileName(args[0].trim());
		
		//letters
		try {
			if (args[1].trim().equals("")) {
				return null; 
			}
		} catch (IndexOutOfBoundsException ex) {
			return null;
		}
		params.setLettersFileName(args[1]);
		
		//output
		try {
			if (args[2].trim().equals("")) {
				return null;
			}
		} catch (IndexOutOfBoundsException ex) {
			return null;
		}
		params.setOutputFileName(args[2]);
		
		//maxtime & visual
		try {
			if (!args[3].trim().equals("")) {
				if (args[3].trim().equals("-visual")) {
					
					params.setVisual(true);
					
					if (args[4].trim().equals("-maxtime")) {
						
						params.setMaxTime(Double.parseDouble(args[5]));
						
					}
					
				}				
				else if (args[3].trim().equals("-maxtime")) {
					
					params.setMaxTime(Float.parseFloat(args[4]));
					
				}
				
			}
		} catch (IndexOutOfBoundsException ex) {
//			return null;
		}
		
		
		return params;
	}
	
	public static void printUsage() {
		System.out.println("Usage: java -jar tpe.jar diccionario letras salida [-visual] [-maxtime n]");
	}
	
	public static void main(String[] args) {
		
		Game game;
		GameParameters params = parseParameters(args);
		
		if (params == null) {
			System.err.println("Error de Parseo de Parametros");
			printUsage();
			return;
		}
		
		if (params.getMaxTime() == 0) {
			
			game = new ExactGame(params);
		
		} else {
			
			game = new LimitedTimeGame(params);
			
		}
		
		
		game.start();
	}
	

}
