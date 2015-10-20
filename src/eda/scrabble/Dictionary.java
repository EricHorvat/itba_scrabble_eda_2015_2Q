package eda.scrabble;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Dictionary extends Trie {

	private static final int MIN_LENGTH_ALLOWED = 2;
	private static final int MAX_LENGTH_ALLOWED = 7;
	
	public Dictionary() {
		
	}
	
	public Dictionary(Map<Character, Integer> map) {
		super(map);
	}
	
	public void add(String word) {
		if (word != null)
			if (word.length() >= MIN_LENGTH_ALLOWED && word.length() <= MAX_LENGTH_ALLOWED)
				super.add(word);
	}

	/**
	 * @deprecated Lo movi a Input Data para que lo pasen como estrategia
	 *             asi no nos joden por POO. Habria que hacer una clase
	 *             InvertedDictionary :S 
	 * @see @{InputData.DictionaryStategy}
	 */
	/*(Eric v8) Si quiero la prioridad que me pasan al reves, sea, el mayor al final*/
	public static Dictionary invertedDictionary(Map<Character, Integer> hierarchy){
		for(Character c : hierarchy.keySet()){
			hierarchy.put(c, -hierarchy.get(c));
		}
		return new Dictionary(hierarchy);
	}

	public String bestFirstOption(
			List<Character> availableChars,
			int maxLength
			)
	{
		return bestFirstLimitedOption(availableChars, maxLength, null);
	}

	public String bestFirstLimitedOption(
			List<Character> availableChars,
			int maxLength,
			Character searchedChar
			)
	{
		return bestLimitedOptionAfter(availableChars, maxLength, searchedChar, null);
	}
	
	public String bestLimitedOptionAfter(
			List<Character> availableChars,
			int maxLength, /*(Eric v8) Serviria para no buscar de mas, se pueden poner 7 fichas*/
			Character character,
			String prevWord
			)
	{
		List<Character> manipulableChars = new ArrayList<Character>(availableChars);
		String best = null;
		for(int i = 0; i < maxLength && best == null; i++){
			best = bestOption(manipulableChars, 0, maxLength, character, i, prevWord, this);
		}
		return best;
	}
	
	
	
}