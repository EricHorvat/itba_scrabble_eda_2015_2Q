package eda.scrabble;

import java.util.Map;

public class InvertedDictionary extends Dictionary {

	public InvertedDictionary(Map<Character, Integer> map){
		super(map);
	}
	public Map<Character, Integer> invertMap(Map<Character, Integer> map){
		for(Character c : map.keySet()){
			map.put(c, -map.get(c));
		}
		return map;
	}
	
}
