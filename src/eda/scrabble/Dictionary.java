package eda.scrabble;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Dictionary extends Trie {
	
	private static final char END_CHAR = 0;
	
	/*TODO check visibility*/
	private List<Character> popularList; 
	
	public Dictionary() {
		
	}
	
	public Dictionary(Map<Character, Integer> map) {
		super(map);
	}
	
	public void setPopularity(List<Character> list) {
		popularList = list;
	}
	
	public String bestWordByPopularity(List<Character> availableChars, int maxLength) {
		return bestOptionBy(availableChars, popularList, 0, maxLength, this);
	}
	
	private String bestOptionBy(
			List<Character> availableChars,
			List<Character> order,
			int actualPosition,
			int maxLength,
			Trie trie)
	{
		boolean found = false;
		List<Character> list = new ArrayList<Character>();
		for (Character c : order){
			if (availableChars.contains(c) ) /**Creo que no hace falta/&& trie.contains(c)/**/
				list.add(c);
		}
		if (actualPosition > 0) {
			list.add(END_CHAR);
		}
		Iterator<Character> it = list.iterator();
		String s = null;
		while (it.hasNext() && !found) {
			Character c = it.next();
			List<Character> tempList = new ArrayList<Character>();
			tempList.addAll(availableChars);
			tempList.remove(c);
			Trie childTrie = trie.getChildren(c);
			if( c != END_CHAR && childTrie != null ) {
				
				//TODO: Me da curiosidad esta recursiva adentro de un while. Que hace? Se justifica?
				//      Esta parte del codigo hay que cambiarla (creo) porque ahora el trie esta
				//      ordenado
				s = bestOptionBy(tempList, order, actualPosition+1, maxLength, childTrie);
				
				if (s != null) {
					found = true;
					s = c.toString().concat(s);
				}
			}
			else{
				if ( (c == END_CHAR ) && trie.contains(c)) return "";
				it.remove();
			}
		}
		return s;
	}
	
}