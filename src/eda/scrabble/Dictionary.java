package eda.scrabble;

import java.util.List;
import java.util.Map;

import eda.scrabble.file.InputData.DictionaryFillStrategy;

public class Dictionary extends Trie {

	private static final int MIN_LENGTH_ALLOWED = 2;
	private static final int MAX_LENGTH_ALLOWED = 7;
	
	private DictionaryFillStrategy kind;
	
	public DictionaryFillStrategy getDictionaryFillStrategy() {
		return this.kind;
	}
	
	public Dictionary(DictionaryFillStrategy kind, Map<Character, Integer> map) {
		super(map);
		this.kind = kind;
	}
	
	public void add(String word) {
		if (word != null)
			if (word.length() >= MIN_LENGTH_ALLOWED && word.length() <= MAX_LENGTH_ALLOWED)
				super.add(word);
	}


	/**
	 * Busca la mejor palabra del diccionario
	 * @param availableChars La lista de chars para formar la palabra
	 * @param maxLength La longitud maxima de la palabra
	 * @return la palabra encontrada
	 */
	public String bestFirstOption(
			Map<Character, Integer> availableChars,
			int maxLength
			)
	{
		return bestFirstLimitedOption(availableChars, maxLength, null);
	}

	/**
	 * Busca la mejor palabra limitada por el caracter <b>searchedChar</b>, con los caracteres de la lista 
	 * <b>availableChars</b> y longitud maxima <b>maxLength</b>
	 * @param availableChars
	 * @param maxLength
	 * @param searchedChar Caracter obligado a estar en la palabra (si es null, busca sin preferencia)
	 * @return
	 */
	public String bestFirstLimitedOption(
			Map<Character, Integer> availableChars,
			int maxLength,
			Character searchedChar
			)
	{
		return bestLimitedOptionAfter(availableChars, maxLength, searchedChar, null);
	}

	/**
	 * Busca la mejor palabra limitada por el caracter <b>searchedChar</b>, con los caracteres de la lista 
	 * <b>availableChars</b> y longitud maxima <b>maxLength</b>, a partir de la palabra <b>prevWord</b>
	 * @param availableChars
	 * @param maxLength
	 * @param searchedChar Caracter obligado a estar en la palabra (si es null, busca sin preferencia)
	 * @param prevWord Palabra a partir del cual se busca la siguiente mejor palabra(si es null, busca sin preferencia)
	 * @return
	 */
	public String bestLimitedOptionAfter(
			Map<Character, Integer> availableChars,
			int maxLength, /*(Eric v8) Serviria para no buscar de mas, se pueden poner 7 fichas*/
			Character searchedChar,
			String prevWord
			)
	{
		//List<Character> manipulableChars = new ArrayList<Character>(availableChars);
		String best = null;
		for( int i =0; i < maxLength && best == null; i++){	
			best = bestOption(availableChars, 0, maxLength, searchedChar, i, prevWord, this);	
			while (best != null && searchedChar != null && !best.contains(searchedChar.toString())) {
				best = bestOption(availableChars, 0, maxLength, searchedChar, i, best, this);
			}
		}
		while (availableChars.get(END_CHAR) > 0) {
			availableChars.put(END_CHAR, availableChars.get((Character)END_CHAR) - 1);
		}
		if (best != null) {
			for (int i = 0; i < best.length(); i++) {
				availableChars.put((Character)best.charAt(i), availableChars.get((Character)best.charAt(i)) - 1);
			}
			if (searchedChar != null) {
				availableChars.put(searchedChar, availableChars.get(searchedChar) + 1);
			}
		} else {
			
		}
		return best;
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
	
}