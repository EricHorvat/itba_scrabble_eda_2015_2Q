package eda.scrabble;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Dictionary extends Trie{

	/*TODO check visibility*/private List<Character> popularList; 
	
	public Dictionary(){}
	
	public void setPopularity(List<Character> list){
		popularList = list;
	}
	
	public String bestWordByPopularity(List<Character> disponibleChars, int maxLength){
		return bestOptionBy(disponibleChars,popularList,0,maxLength,this);
	}
	
	private String bestOptionBy(List<Character> disponibleChars,List<Character> order, int actualPosition, int maxLength, Trie trie){
		boolean found = false;
		List<Character> list = new ArrayList<Character>();
		
		for(Character c : order){
			if(disponibleChars.contains(c) /**Creo que no hace falta/&& trie.contains(c)/**/)
				list.add(c);
		}
		if(actualPosition>0){
			list.add((char)0);
		}
		Iterator<Character> it = list.iterator();
		String s = null;
		while( it.hasNext() && !found){
			Character c = it.next();
			List<Character> tempList = new ArrayList<Character>();
			tempList.addAll(disponibleChars);
			tempList.remove(c);
			Trie childTrie = trie.getChildren(c);
			if((c!=(char)0)&&(childTrie!= null)&&((s = bestOptionBy(tempList, order, actualPosition+1, maxLength, childTrie))!=null))
				{
					found = true;
					s = c.toString().concat(s);
				}
			else{
				if((c==(char)0)&& trie.contains(c))
					return "";
				it.remove();
			}
		}
		return s;
	}
	
}