package eda.scrabble;

import eda.scrabble.file.InputData;

import java.util.HashMap;
import java.util.Map;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		dictionary = InputData.fillDictoniary();
		chars = InputData.getGameChars();
	}
	
	public static Map<Character,Integer> VALUE_MAP = InputData.fillValueMap();
	public static Map<Character,Integer> POPULARITY_MAP = newPopularityMap();
	
	static Dictionary dictionary;
	static char[] chars;
	
	private static Map<Character, Integer> newPopularityMap() {
		HashMap<Character, Integer> hMap = new HashMap<Character, Integer>();
		for(char c = 'A';c <= 'Z';c++){
			hMap.put(c, 0);
		}
		return hMap;
	}

}
