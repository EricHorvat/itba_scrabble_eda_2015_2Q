package eda.scrabble.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

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

		Dictionary dict = new Dictionary();
		String fileName = "diccinario.txt";
		BufferedReader inStream = null;
		try{
			inStream = new BufferedReader(new FileReader(fileName));
			String line;
			while((line = inStream.readLine())!= null){
				dict.add(line);
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
		return dict;
	}

	public static char[] getGameChars(){

		char[] vect = new char[0];
		String fileName = "letras.txt";
		BufferedReader inStream = null;
		try{
			inStream = new BufferedReader(new FileReader(fileName));
			String line;
			if((line = inStream.readLine())!= null){
				vect = line.toCharArray();
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
		return vect;
	}
}