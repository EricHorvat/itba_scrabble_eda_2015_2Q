package eda.scrabble.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Trie {
	
	protected static final Character END_CHAR = (Character)(char)0;
	
	private static final boolean DEBUG = false;
	
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
			if (DEBUG)
				System.out.print("Word: "+word+". La "+c);
			if (value == null) value = c;
			if (value == c) {
				if (DEBUG)
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
				if (DEBUG)
					System.out.println(" "+this + " vs " + c);
				if (hierarchy.get(this.value) < hierarchy.get(c)) {
					if (DEBUG)
						System.out.println("Entra "+ c);
					Node n = new Node(owner);
					n.value = c;
					n.next = this;
					
					if (prev == null) {
						if (DEBUG)
							System.out.println("First es "+ c);
						owner.first = n;
					}
					else {prev.next = n;} 
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

	public Trie() {
		
	}
	
	public Trie(Map<Character, Integer> hierarchy) {
		Node.hierarchy = hierarchy;
		hierarchy.put(END_CHAR, Integer.MIN_VALUE);
	}

	public void add(String word) {
		if (first == null)
			first = new Node(this);
		if (DEBUG)
				System.out.println("Adding " + word);
		first.add(word, null);
	}
	public void addEndLetter() {
		Node node = first;
		Node prevNode = null;
		while (node != null) {
			prevNode = node;
			node = node.next;
		}
		Node endNode = new Node(this);
		endNode.value = 0;
		endNode.next = null;
		if (prevNode == null)
			first = endNode;
		else
			prevNode.next = endNode;
	}

	private Node containingCharacter(Node node, char c) {
		if (node.value.equals(c))
			return node;
		if (node.next != null)
			return containingCharacter(node.next, c);
		return null;
	}
	
	public Node containingCharacter(char c) {
		return containingCharacter(first, c);
	}
	
	@Override
	public String toString() {
		
		List<String> l = new ArrayList<String>();
		toString(l, first, "");
		return l.toString();
	}
	
	public List<String> getWords() {
		List<String> l = new ArrayList<String>();
		toString(l, first, "");
		return l;
	}
	
	private void toString(List<String> l, Node node, String acum){
		if(node == null)
			return;
		if(node.value.equals(END_CHAR))
			l.add(acum);
		
		if(node.nextLetter!=null)
			{

			String s2 = new String(acum); 
			toString(l,node.nextLetter.first,s2.concat(node.value.toString()));
		}
		if(node.next!=null)
		{
			String s = new String(acum); 
			toString(l,node.next,s);
		}
	}
	

	public boolean contains(String s) {
		
		return contains(first,s);
	}
	
	private boolean contains(Node node, String s){
		if(node.value.equals(END_CHAR)){
				if(s.length()==0)
					return true;
		}else{
			if(s.equals("")){}
			else if (node.value.equals(s.charAt(0))){
				if(node.nextLetter!=null)
				{
					String s2 = s.substring(1, s.length());
					return node.nextLetter.contains(s2);
				}
			}
			else
				if(node.next!=null)
					return contains(node.next,s);
		}
		return false;
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

	/**
	 * Busca la mejor palabra limitada por el caracter <b>searchedChar</b> en la posicion<><>, con los caracteres de la lista 
	 * <b>manipulableChars</b> y longitud maxima <b>maxLength</b>, a partir de la palabra <b>prevWord</b>
	 * @param manipulableChars
	 * @param currentIndex
	 * @param maxLength
	 * @param searchedChar Caracter obligado a estar en la palabra (si es null, busca sin preferencia)
	 * @param searchedPosition posicion en la que se desea que este el caracter
	 * @param prevWord Palabra a partir del cual se busca la siguiente mejor palabra(si es null, busca sin preferencia)
	 * @param trie Trie en el que se busca
	 * @return
	 */
	public String bestOption(
			Map<Character, Integer> manipulableChars,
			int currentIndex,/*(Eric v8) Esto sirve para decir cuando va(n) la(s) ficha(s) fija(s), solo falta ver si las pasamos por listas o arrays, etc*/
			int maxLength, /*(Eric v8) Serviria para no buscar de mas, se pueden poner 7 fichas*/
			Character searchedChar,
			int searchedPosition,
			String prevWord,//Se pide una palabra y se busca la mejor soludcion DESPUES de esta
			Trie trie
			)
	{		
		
		String resultWord = null;
		Node node = null;
		if (trie == null) return null;
		if (prevWord != null) {
			Character c = prevWord.charAt(0);
			//TODO OJO, NO CONTROLO QUE LA PALABRA INGREADA NO ESTE EN EL TRIE (agregar un if != null)
			// Busco horizontalmente el nodo que tenga la primera letra de prevWord
			node = trie.containingCharacter(c);
			if (prevWord.length() == 1) {
				// Si es la ultima letra
				node = trie.bestNode(manipulableChars, node.next);
				prevWord = null;
			} else {
				// Busca en mis descendientes el que se corresponda con prevWord
				manipulableChars.put(c, manipulableChars.get(c) - 1);
				resultWord = bestOption(manipulableChars, currentIndex+1, maxLength, searchedChar, searchedPosition, prevWord.substring(1, prevWord.length()), node.nextLetter);
				if (resultWord != null) {
					// Armamos la palabra resultante
					resultWord = c.toString().concat(resultWord);
					if (	searchedChar != null
							&& searchedPosition == currentIndex
							&& resultWord.charAt(0) != searchedChar )
					{
						resultWord = null;
						manipulableChars.put(c, manipulableChars.get(c) + 1);
						node = trie.bestNode(manipulableChars, node.next);
					}
				}
				else {
					manipulableChars.put(c, manipulableChars.get(c) + 1);
					node = trie.bestNode(manipulableChars, node.next);
				}
			}
		}
		else if (searchedPosition != currentIndex || searchedChar == null){
			node = trie.bestNode(manipulableChars);
		}else{
			node = trie.containingCharacter(searchedChar);
			if(manipulableChars.get(searchedChar) <= 0)
				node = trie.bestNode(manipulableChars);
		}
		
		if (node != null && node.value == END_CHAR)
			return "";
		
		//(Eric v8)Agrega al caracter de fin a las posibilidades
		if (currentIndex == 2)	manipulableChars.put(END_CHAR, 1);
		
		Character currentChar = null;
		
		while (node != null && resultWord == null) {
			currentChar = node.value;
			// baja un nivel. Busca la mejor subopcion
			resultWord = nextOption(manipulableChars, currentIndex, maxLength, searchedChar, searchedPosition, node);
			
			//(Eric v8)Si encontro palabra le agrega el caracter actual al comienzo
			//(Eric v13) Esto seria joya si se agrega a nextOption, pero no se puede
			if (resultWord != null) {
				resultWord = currentChar.toString().concat(resultWord);
				if(searchedChar!= null && searchedPosition == currentIndex && !(resultWord.charAt(0) == searchedChar)){
					resultWord = null;
					node = trie.bestNode(manipulableChars,node.next);
				}
					
			}
			else node = trie.bestNode(manipulableChars,node.next);
		}

		return resultWord;
	}
	
	//Si queres lo comento, reemplace por la seccion de codigo para no repetir
	public String nextOption(
			Map<Character, Integer> manipulableChars,
			int currentIndex,/*(Eric v8) Esto sirve para decir cuando va(n) la(s) ficha(s) fija(s), solo falta ver si las pasamos por listas o arrays, etc*/
			int maxLength, /*(Eric v8) Serviria para no buscar de mas, se pueden poner 7 fichas*/
			Character searchedChar,
			int searchedPosition,
			Node node){
		
		Character currentChar = node.value;
		manipulableChars.put(currentChar, manipulableChars.get(currentChar) - 1);
		// baja un nivel. Busca la mejor subopcion
		String resultWord = bestOption(manipulableChars, currentIndex+1, maxLength, searchedChar, searchedPosition, null,  node.nextLetter);
		
		//(Eric v8)Devuelve el caracter que se borro al array, ya que va a ser reutiizado
		manipulableChars.put(currentChar, manipulableChars.get(currentChar) + 1);
		return resultWord;
	}

	/**
	 * Recorre horizontalmente los nodos hasta que uno
	 * este disponible, su valor sea una letra disponible
	 */
	private Node bestNode( Map<Character, Integer> manipulableChars) {
		return bestNode(manipulableChars, first);
	}
	
	private Node bestNode( Map<Character, Integer> manipulableChars, Node node) {
		if (node == null) return null;
		if (manipulableChars.get(node.value) > 0) return node;
		return bestNode(manipulableChars, node.next);
	}
	
}
