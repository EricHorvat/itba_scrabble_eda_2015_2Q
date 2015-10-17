package eda.scrabble;

public class Trie {

	class Node{
		Character value;
		Node next;
		Trie nextLetter;

		private Node(){}

		private void add(String word){
			if(value==null)
				value = word.charAt(0);
			if(value==word.charAt(0)){
				if(nextLetter == null)
					nextLetter = new Trie();
				if(word.length()==1)
					nextLetter.addEndLetter();
				else
					nextLetter.add(word.substring(1,word.length()));
			}
			else
			{
				if(next == null)
					next = new Node();
				next.add(word);
			}
		}
	}
	
	Node first = null;

	public Trie(){}

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

	private boolean contains(Node node,char c){
		if(node.value.equals(c))
			return true;
		if(node.next!=null)
			return contains(node.next,c);
		return false;
	}
	
	public boolean contains(char c){
		return contains(first,c);
	}

	public Trie getChildren(Character c){
		return getChildren(first,c);
	}
	
	public Trie getChildren(Node node, Character c){
		if(node.value.equals(c))
			return node.nextLetter;
		if(node.next!=null)
			return getChildren(node.next,c);
		return null;
	}
	
}
