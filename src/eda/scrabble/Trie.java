package eda.scrabble;

import java.util.List;
import java.util.Map;

public class Trie {
	
	private static final char END_CHAR = 0;
	
	
	private static class Node {
//		(Martin v7)
//		(first)  value: H, next: C, nextLetter: O
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
				// (Martin v7) Entre al substring
				if (nextLetter == null) nextLetter = new Trie();
				// (Martin v7) Fin de la palabra
				if (word.length() == 1) nextLetter.addEndLetter();
				// (Martin v7) Agrega a mi substring
				else nextLetter.add(word.substring(1, word.length()));
			}
			else {
				//(Martin v7)TODO: Es tarde pero creo que anda. No se bien porque
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

	String bestOptionBy(
			List<Character> manipulableChars,
			int currentIndex,/*(Eric v8) Esto sirve para decir cuando va(n) la(s) ficha(s) fija(s), solo falta ver si las pasamos por listas o arrays, etc*/
			int maxLength, /*(Eric v8) Serviria para no buscar de mas, se pueden poner 7 fichas*/
			Trie trie
			)
	{
		String resultWord = null;
		Node node = trie.bestNode(manipulableChars);
		if (node != null && node.value == END_CHAR)
			return "";
		
		//(Eric v8)Agrega al caracter de fin a las posibilidades
		if (currentIndex == 2)	manipulableChars.add(END_CHAR);
		
		Character currentChar = null;
		
		while (node != null && proWord == null) {
			
			currentChar = node.value;
			manipulableChars.remove(currentChar);
			// baja un nivel. Busca la mejor subopcion
			resultWord = bestOptionBy(manipulableChars, currentIndex+1, maxLength, node.nextLetter);
			
			//(Eric v8)Devuelve el caracter que se borro al array, ya que va a ser reutiizado
			manipulableChars.add(currentChar);
			
			//(Eric v8)Si encontro palabra le agrega el caracter actual al comienzo
			if (resultWord != null) resultWord = currentChar.toString().concat(resultWord);
			else	node = trie.bestNode(manipulableChars);
		}
		return resultWord;
	}

	/**
	 * Recorre horizontalmente los nodos hasta que uno lo contenga
	 */
	private Node bestNode( List<Character> manipulableChars) {
		return bestNode(manipulableChars, first);
	}
	
	private Node bestNode( List<Character> manipulableChars, Node node) {
		if (node == null) return null;
		if (manipulableChars.contains(node.value)) return node;
		return bestNode(manipulableChars, node.next);
	}
	
	/**
	 * Pseudo codigo
	 * @param trie
	 * @param available
	 * @return
	 */
	private String moveHorizontally(Trie trie, List<Character> available) {
		
		if (trie == null) return "";
		
		Node currentNode = trie.first;
		
		while (currentNode != null) {
			
			if (available.contains(currentNode.value)) {
				
				// Marcar como letra usada 
				available.remove(currentNode.value);
				
				// descender en el trie
				String str = moveHorizontally(currentNode.nextLetter, available);
				if (str != null) {
					// Significa que encontre la palabra en mi sub-trie
					// la junto con mi valor y seguimos pa delante
					//TODO: Hacer un remove de la palabra recien usada
					return currentNode.value.toString().concat(str);
				}
				// Mi sub-trie no encontro la palabra
				// La repongo entonces
				available.add(currentNode.value);
				
			}
			
			currentNode = currentNode.next;
		}
		
		return null;
		
	}
	
}
