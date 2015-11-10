package eda.scrabble.file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sun.xml.internal.ws.util.StringUtils;

import eda.scrabble.Dictionary;
import eda.scrabble.Game;

public class InputData{
	
	public enum DictionaryFillStrategy {
		NONE,
		HIGHEST_OCURRENCY,
		LOWEST_OCURRENCY,
		HIGHEST_VALUE,
		LOWEST_VALUE,
		ALPHABETIC
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
	
	private static int countOccurencesOf(String s, char c) {
		int r = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == c) {
				r++;
			}
		}
		return r;
	}

	private static boolean containsAll(String s, Map<Character, Integer> chars) {
		
		for (char c : s.toCharArray()) {
			Integer times = chars.get((Character)c);
			if (times == null || times <= 0 || countOccurencesOf(s, c) > times) {
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
		
		Dictionary dict = null;
		
		Iterator<String> it = words.iterator();
		
		if (strategy == DictionaryFillStrategy.HIGHEST_VALUE || strategy == DictionaryFillStrategy.LOWEST_VALUE) {
			
			dict = new Dictionary(strategy, Game.VALUE_MAP);
			
			while (it.hasNext()) {
				String word = it.next().toUpperCase();
				if (!(2 <= word.length() && word.length() <= 7 && containsAll(word, characters))) {
					it.remove();
				}
			}
			
		} else if ( strategy == DictionaryFillStrategy.NONE ||
								strategy == DictionaryFillStrategy.LOWEST_OCURRENCY ||
								strategy == DictionaryFillStrategy.HIGHEST_OCURRENCY) {
			
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
					it.remove();
				}
				
			}
			
			dict = new Dictionary(strategy, popularMap);
		} else {
			
			for (char c = 'A';c <= 'Z';c++)
				popularMap.put(c, -(int)c);
			
			dict = new Dictionary(strategy, popularMap);
			
			while (it.hasNext()) {
				String word = it.next().toUpperCase();
				if (!(2 <= word.length() && word.length() <= 7 && containsAll(word, characters))) {
					it.remove();
				}
			}
		}
		
		for (String word : words) {
			dict.add(word.toUpperCase());
		}
		
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