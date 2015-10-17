package eda.scrabble;

import eda.scrabble.file.InputData;

import java.util.Map;

public class Main {

	private final static String CHAR_VALUE_FILENAME = "charValue.txt";
	
	
	public static Map<Character,Integer> VALUE_MAP = InputData.fillValueMap(CHAR_VALUE_FILENAME);
	
	public static void main(String[] args) {
		Game.getInstance().start();
	}
	

}
