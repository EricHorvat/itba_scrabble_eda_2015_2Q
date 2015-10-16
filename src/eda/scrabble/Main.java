package eda.scrabble;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		dictoniary=fillDictoniary();
		chars = getGameChars();
	}
	
	public static Map<Character,Integer> VALUE_MAP = fillValueMap();

	Dictoniary dictoniary;
	List<Character> chars;

}
