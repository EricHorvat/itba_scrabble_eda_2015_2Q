package eda.scrabble;

import eda.scrabble.file.InputData;

import java.util.Map;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		dictionary = InputData.fillDictoniary();
		chars = InputData.getGameChars();
	}
	
	public static Map<Character,Integer> VALUE_MAP = InputData.fillValueMap();

	static Dictionary dictionary;
	static char[] chars;

}
