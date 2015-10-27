package eda.scrabble.file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import eda.scrabble.Dictionary;
import eda.scrabble.Game;

public class InputData{
	
	private final static boolean DEBUG = false;
	
	public enum DictionaryFillStrategy {
		NONE,
		HIGHEST_OCURRENCY,
		LOWEST_OCURRENCY,
		HIGHEST_VALUE,
		LOWEST_VALUE
	};
	
	private InputData() {
		
	}
	
	public static List<String> readAllLines(String filename) {
		
		List<String> lines = new ArrayList<String>();
		
		BufferedReader inStream = null;
		try {
			inStream = new BufferedReader(new FileReader(filename));
			String line;
			while ((line = inStream.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if(inStream != null){
				try {
					inStream.close();
				} catch (IOException e){}
			}
		}
		
		return lines;
	}

	public static HashMap<Character, Integer> fillValueMap(String filename) {
		
		HashMap<Character, Integer> hMap = new HashMap<Character, Integer>();
		char c;
		int value;
		
		for (String line : readAllLines(filename)) {
			c = line.toUpperCase().charAt(0);
			value = Integer.parseInt(line.substring(2, line.length()));
			hMap.put(c, value);
		}
		
		
		return hMap;
	}

	private static boolean containsAll(String s, Map<Character, Integer> chars) {
		
		for (char c : s.toCharArray()) {
			if (chars.get((Character)c) <= 0) {
				if (DEBUG)
					System.out.println(c +" is not in chars: " + Game.getAvailableChars(chars));
				return false;
			}
		}
		return true;
	}
	
	public static Dictionary fillDictionary(String filename, DictionaryFillStrategy strategy, Map<Character, Integer> characters) {
		Map<Character, Integer> popularMap = new HashMap<Character, Integer>();
		
		for (char c = 'A';c <= 'Z';c++)
			popularMap.put(c, 0);
		
		
		List<String> words = readAllLines(filename);
		
		if (DEBUG)
			System.out.println("Got " + words.size() + " words");
		
		Dictionary dict;
		
		Iterator<String> it = words.iterator();
		
		if (strategy == DictionaryFillStrategy.HIGHEST_VALUE || strategy == DictionaryFillStrategy.LOWEST_VALUE) {
			
			dict = new Dictionary(strategy, Game.VALUE_MAP);
			
			while (it.hasNext()) {
				String word = it.next().toUpperCase();
				if (!(2 <= word.length() && word.length() <= 7 && containsAll(word, characters))) {
					it.remove();
					if (DEBUG) System.out.println("Removing " + word + " from collection");
				}
			}
			
		} else {
			
			while (it.hasNext()) {
				String word = it.next().toUpperCase();
				if ( 2 <= word.length() && word.length() <= 7 && containsAll(word, characters)) {
					for (char c : word.toCharArray()) {
						switch (strategy) {
							case NONE:
								popularMap.put(c, 0);
								break;
							case LOWEST_OCURRENCY:
								popularMap.put(c, popularMap.get(c) + 1);
								break;
							case HIGHEST_OCURRENCY:
								popularMap.put(c, popularMap.get(c) - 1);
								break;
							default:
								break;
						}
					}
				} else {
					if (DEBUG)
						System.out.println("Removing " + word + " from collection");
					it.remove();
				}
				
			}
			
			dict = new Dictionary(strategy, popularMap);
		}
		
		if (DEBUG) {
			System.out.println("Trimmed to " + words.size() + " words");
			System.out.println(words);
		}
		
		for (String word : words) {
			dict.add(word.toUpperCase());
		}
		
		if (DEBUG) System.out.println(dict);
		
		return dict;
	}

	public static Map<Character, Integer> getGameChars(String filename){

		Map<Character, Integer> map = new HashMap<Character, Integer>();
		List<String> lines = readAllLines(filename);
		
		for (int i = 'A'; i <= 'Z'; i++)
			map.put((Character)(char)i, 0);
		// END_CHAR
		map.put((Character)(char)0, 0);
		
		for (String line : lines) {
			line = line.toUpperCase();
			for (char c : line.toCharArray()) {
				map.put(c, map.get(c)+1);
			}
		}
		return map;
	}
}