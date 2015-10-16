package eda.scrabble.file

public class InputData{

	private class InputData(){}

	public static HashMap<Character, Integer> fillValueMap(){
		
		HashMap<Character, Integer> hMap = new HashMap<Character, Integer>();
		String fileName = "charValue.txt";
		BufferedReader inStream = null;
		try{
			inStream = new BufferedReader(new FileReader(fileName));
			String line;
			while((line = inStream.readLine())!= null){
				char c = line.charAt(0)
				int value = Integer.parseInt(line.substring(2,line.lenght));
				hMap.put(c,value);
			}

		} finally{
			if(inStream != null){
				inStream.close();
			}
		}
		return hMap
	}

	public static Dictoniary fillDictoniary(){

		Dictoniary dict = new Dictoniary();
		String fileName = "diccinario.txt";
		BufferedReader inStream = null;
		try{
			inStream = new BufferedReader(new FileReader(fileName));
			String line;
			while((line = inStream.readLine())!= null){
				dict.add(line);
			}
		} finally{
			if(inStream != null){
				inStream.close();
			}
		}
	}

	public static char[] getGameChars(){

		char[] vect;
		String fileName = "letras.txt";
		BufferedReader inStream = null;
		try{
			inStream = new BufferedReader(new FileReader(fileName));
			String line;
			if((line = inStream.readLine())!= null){
				vect = line.toCharArray();
			}
		} finally{
			if(inStream != null){
				inStream.close();
			}
		}
		return vect;
	}
}