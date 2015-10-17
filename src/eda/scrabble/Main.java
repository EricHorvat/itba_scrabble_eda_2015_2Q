package eda.scrabble;

import eda.scrabble.file.InputData;

import java.util.List;
import java.util.Map;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		dictionary = InputData.fillDictoniary();
		chars = InputData.getGameChars();
		System.out.print(dictionary.bestWordByPopularity(chars, 7));
	}
	
	public static Map<Character,Integer> VALUE_MAP = InputData.fillValueMap();
	
	static Dictionary dictionary;
	static List<Character> chars;
	

}
