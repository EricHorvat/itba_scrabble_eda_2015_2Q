package eda.scrabble.file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eda.scrabble.Dictionary;

public class InputData{
	
	public enum DictionaryStrategy {
		HIGHEST_OCURRENCY_FIRST,
		HIGHEST_OCURRENCY_LAST
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

	public static Dictionary fillDictionary(String filename, DictionaryStrategy strategy) {
		Map<Character, Integer> popularMap = new HashMap<Character, Integer>();
		
		for (char c = 'A';c <= 'Z';c++)
			popularMap.put(c, 0);
		
		
		List<String> words = readAllLines(filename);
		
		for (String word : words) {
			word = word.toUpperCase();
			for (char c : word.toCharArray()) {
				if (strategy == DictionaryStrategy.HIGHEST_OCURRENCY_FIRST)
					popularMap.put(c, popularMap.get(c) + 1);
				else
					popularMap.put(c, popularMap.get(c) - 1);
			}
			
		}
		Dictionary dict = new Dictionary(popularMap);
		
		System.out.println(popularMap);
		
		for (String word : words) {
			dict.add(word.toUpperCase());
		}
		
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