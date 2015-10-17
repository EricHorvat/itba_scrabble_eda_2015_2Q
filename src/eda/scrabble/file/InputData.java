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

	private InputData(){}

	public static HashMap<Character, Integer> fillValueMap(){
		
		HashMap<Character, Integer> hMap = new HashMap<Character, Integer>();
		String fileName = "charValue.txt";
		BufferedReader inStream = null;
		try{
			inStream = new BufferedReader(new FileReader(fileName));
			String line;
			while((line = inStream.readLine())!= null){
				char c = line.charAt(0);
				int value = Integer.parseInt(line.substring(2,line.length()));
				hMap.put(c,value);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if(inStream != null){
				try {
					inStream.close();
				} catch (IOException e){}
			}
		}
		return hMap;
	}

	public static Dictionary fillDictoniary(){
		Map<Character,Integer> popularMap = newPopularityMap();
		Dictionary dict = new Dictionary();
		String fileName = "diccionario.txt";
		BufferedReader inStream = null;
		try{
			inStream = new BufferedReader(new FileReader(fileName));
			String line;
			while((line = inStream.readLine())!= null){
				dict.add(line.toUpperCase());
				for (char c : line.toUpperCase().toCharArray()) {
					popularMap.put(c, popularMap.get(c)+1);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if(inStream != null){
				try {
					inStream.close();
				} catch (IOException e) {}
			}
		}
		List<Character> popularList = new ArrayList<Character>();
		while(!popularMap.isEmpty()){
			int max=-1;
			Character ch = null;
			for(Character c : popularMap.keySet())
			{
				if(max < popularMap.get(c))
				{
					max = popularMap.get(c);
					ch = c;
				}
			}
			popularList.add(ch);
			popularMap.remove(ch);
		}			
		dict.setPopularity(popularList);
		return dict;
	}

	public static List<Character> getGameChars(){

		List<Character> list = new ArrayList<Character>();
		String fileName = "letras.txt";
		BufferedReader inStream = null;
		try{
			inStream = new BufferedReader(new FileReader(fileName));
			String line;
			if((line = inStream.readLine())!= null){
				for(char c : line.toCharArray()){
					list.add(c);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(inStream != null){
				try {
					inStream.close();
				} catch (IOException e) {}
			}
		}
		return list;
	}


	private static Map<Character, Integer> newPopularityMap() {
		HashMap<Character, Integer> hMap = new HashMap<Character, Integer>();
		for(char c = 'A';c <= 'Z';c++){
			hMap.put(c, 0);
		}
		return hMap;
	}
}