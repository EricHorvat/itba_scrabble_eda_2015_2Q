package eda.scrabble

public class Dictionary{


	public class Node{
		char value;
		Node next;
		Dictionary nextLetter;

		private Node(){}

		private void add(String word){
			if(value==null)
				value = word.charAt(0);
			if(value.equals(word.charAt(0))){
				if(nextLetter == null)
					nextLetter = new Dictionary();
				if(word.lenght==1)
					nextLetter.addEndLetter();
				else
					nextLetter.add(word.substring(1,word.lenght));
			}
			else
				if(next == null)
					next = new Node();
				next.add(word);
		}
	}
	
	Node first = null;

	public Dictionary(){}

	public void add(String word){
		if(first == null)
			first= new Node();
		first.add(word);
	}
	public void addEndLetter(){
		Node node = new Node();
		node.value = 0;
		node.next = first;
		first= node;
	}


}