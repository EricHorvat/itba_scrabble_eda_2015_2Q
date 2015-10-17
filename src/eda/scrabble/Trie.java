package eda.scrabble;

import java.util.Map;

public class Trie {
	
	private static class Node {
//		 (first)  value: H, next: C, nextLetter: O
//		    |
//		    V10   8                      5
//				H---->C (next) ------------> J
//		   /      | (Trie) nextLetter    |
//		  O-->I   H                      O
//		  |   |   |                      |
//		  L   L   A                      D
//		  |   |   |                      |
//		  A   O   U                      A
		
		Character value;
		Node next;
		Trie nextLetter;
		
		static Map<Character, Integer> hierarchy;
		Trie owner;

		private Node(Trie owner) {
			this.owner = owner;
		}
		
		@Override
				public String toString() {
					return "Node{"+value+"}";
				}

		private void add(String word, Node prev) {
			char c = word.charAt(0);
			System.out.print("Word: "+word+". La "+c);
			if (value == null) value = c;
			if (value == c) {
				System.out.println(" matcheo");
				// Entre al substring
				if (nextLetter == null) nextLetter = new Trie();
				// Fin de la palabra
				if (word.length() == 1) nextLetter.addEndLetter();
				// Agrega a mi substring
				else nextLetter.add(word.substring(1, word.length()));
			}
			else {
				//TODO: Es tarde pero creo que anda. No se bien porque
				//      pero el objetivo es que el trie este ordenado
				//      descendentemente como el dibujo de arriba
				System.out.println(" "+this + " vs " + c);
				if (hierarchy.get(this.value) < hierarchy.get(c)) {
					System.out.println("Entra "+ c);
					Node n = new Node(owner);
					n.value = c;
					n.next = this;
					
					if (prev == null) {
						System.out.println("First es "+ c);
						owner.first = n;
					}
					n.add(word, this);
				} else {
					if (next == null)
						next = new Node(owner);
					next.add(word, this);
				}
			}
		}
	}
	
	private Node first = null;
	private Map<Character, Integer> hierarchy;

	public Trie() {
		
	}
	
	public Trie(Map<Character, Integer> hierarchy) {
		Node.hierarchy = hierarchy;
	}

	public void add(String word) {
		if (first == null)
			first = new Node(this);
		first.add(word, null);
	}
	public void addEndLetter() {
		Node node = new Node(this);
		node.value = 0;
		node.next = first;
		first = node;
	}

	private boolean contains(Node node, char c) {
		if (node.value.equals(c))
			return true;
		if (node.next != null)
			return contains(node.next, c);
		return false;
	}
	
	public boolean contains(char c) {
		return contains(first, c);
	}

	public Trie getChildren(Character c) {
		return getChildren(first, c);
	}
	
	private Trie getChildren(Node node, Character c) {
		if (node.value.equals(c))
			return node.nextLetter;
		if (node.next != null)
			return getChildren(node.next, c);
		return null;
	}
	
}
