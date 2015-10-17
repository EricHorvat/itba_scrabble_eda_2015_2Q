package eda.scrabble;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Dictionary extends Trie {

	public Dictionary() {
		
	}
	
	public Dictionary(Map<Character, Integer> map) {
		super(map);
	}

	/*(Eric v8) Si quiero la prioridad que me pasan al reves, sea, el mayor al final*/
	public static Dictionary invertedDictionary(Map<Character, Integer> hierarchy){
		for(Character c : hierarchy.keySet()){
		hierarchy.put(c, -hierarchy.get(c));
		}
		return new Dictionary(hierarchy);
	}
	
	public String bestOption(
			List<Character> availableChars,
			int maxLength /*(Eric v8) Serviria para no buscar de mas, se pueden poner 7 fichas*/
			)
	{
		List<Character> manipulableChars = new ArrayList<Character>(availableChars);
		return bestOptionBy(manipulableChars,0,maxLength, this);
	}
	
	
}