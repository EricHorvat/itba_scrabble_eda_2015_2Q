package eda.scrabble.file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eda.scrabble.Dictionary;
import eda.scrabble.Game;
import eda.scrabble.Trie;

public class InputData{
	
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

	public static Dictionary fillDictionary(String filename, DictionaryFillStrategy strategy) {
		Map<Character, Integer> popularMap = new HashMap<Character, Integer>();
		
		for (char c = 'A';c <= 'Z';c++)
			popularMap.put(c, 0);
		
		
		List<String> words = readAllLines(filename);
		
		Dictionary dict;
		
		if (strategy == DictionaryFillStrategy.HIGHEST_VALUE || strategy == DictionaryFillStrategy.LOWEST_VALUE) {
			
			dict = new Dictionary(Game.VALUE_MAP);
			
		} else {
		
			for (String word : words) {
				//TODO: falta la validacion de si me alcanzan las letras
				if (2 <= word.length() && word.length() <= 7) {
					word = word.toUpperCase();
					for (char c : word.toCharArray()) {
						switch (strategy) {
						case NONE:
							// Todos son iguales
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
					words.remove(word);
				}
				
			}
			
			dict = new Dictionary(popularMap);
		}
		
		for (String word : words) {
			dict.add(word.toUpperCase());
		}
		
		//Trie.moveVertically(dict);
		
		return dict;
	}

	public static List<Character> getGameChars(String filename){

		List<Character> list = new ArrayList<Character>();
		List<String> lines = readAllLines(filename);
		
		for (String line : lines) {
			for (char c : line.toCharArray()) {
				list.add(c);
			}
		}
		return list;
	}
}