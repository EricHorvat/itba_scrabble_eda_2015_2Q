package eda.scrabble.game;

import java.util.Map;

import eda.scrabble.boards.logic.Board;
import eda.scrabble.file.InputData;
import eda.scrabble.storage.Dictionary;

/**
 * 
 * Representacion de un juego en este problema
 * 
 * @author martin
 *
 */
public abstract class Game {
	
	/**
	 * Clase que nos representa los parametros ingresados
	 * para realizar el algoritmo
	 * @author martin
	 *
	 */
	public static class GameParameters {
		
		protected String dictionaryFileName;
		protected String lettersFileName;
		protected String outputFileName;
		protected boolean visual = false;
		protected double maxTime = 0;
		protected boolean stochastic = false;

		/**
		 * Fija el archivo de diccionario a usar
		 * @param dictionaryFileName el nombre del archivo del diccionario
		 */
		public void setDictionaryFileName(String dictionaryFileName) {
			this.dictionaryFileName = dictionaryFileName;
		}

		/**
		 * Fija el archivo de letras disponibles a usar
		 * @param lettersFileName el nombre del archivo de las letras disponibles
		 */
		public void setLettersFileName(String lettersFileName) {
			this.lettersFileName = lettersFileName;
		}

		/**
		 * Fija el archivo de salida
		 * @param outputFileName el nombre del archivo de salida
		 */
		public void setOutputFileName(String outputFileName) {
			this.outputFileName = outputFileName;
		}

		/**
		 * Fija si se tienen que mostrar todos los tableros
		 * por pantalla
		 * @param visual true <==> se quiere ver todos los tableros por pantalla
		 */
		public void setVisual(boolean visual) {
			this.visual = visual;
		}

		/**
		 * Fija el tiempo maximo permitido para devolver un resultado
		 * @param maxTime el tiempo maximo permitido
		 */
		public void setMaxTime(double maxTime) {
			this.maxTime = maxTime;
		}

		/**
		 * Dice si en la partida se debe mostrar el progreso en
		 * todos los tableros
		 * @return true <==> se muestran todos los tableros por los que se pasa
		 */
		public boolean isVisual() {
			return visual;
		}

		/**
		 * El tiempo maximo permitido para operar
		 * @return El tiempo maximo permitido para operar
		 */
		public double getMaxTime() {
			return maxTime;
		}

		/**
		 * Me dice si la partida se debe resolver con el metodo estocastico
		 * @return
		 */
		public boolean isStochastic() {
			return stochastic;
		}

		
		/**
		 * Settea la configuracion donde se usa Hill Climb
		 * estocastico en vez de el normal
		 * @param stochastic
		 */
		public void setStochastic(boolean stochastic) {
			this.stochastic = stochastic;
		}
		
		
		
	}
	
	
	/**
	 * El maximo largo de palabra permitido
	 */
	protected final static int MAX_LENGTH_WORD = 7;
	/**
	 * El archivo que nos guarda el mappeo entre los caracteres y sus valores
	 */
	private final static String CHAR_VALUE_FILENAME = "charValue.txt";
	/**
	 * El mapa que nos guarda cuanto vale cada caracter
	 */
	public final static Map<Character,Integer> CHARACTER_VALUES = InputData.fillValueMap(CHAR_VALUE_FILENAME);
	
	/**
	 * Nuestro tablero por defecto
	 */
	protected Board grid;
	/**
	 * Nuestro mejor puntaje global
	 */
	protected int maxScore = 0;
	/**
	 * Las opciones para este Game
	 */
	protected GameParameters params;
	/**
	 * El tiempo de corte
	 */
	protected double eta = -1;
	
	/**
	 * Creamos nuestro juego con un conjunto de parametros
	 * @param params los parametros para este juego
	 */
	public Game(GameParameters params) {
		this.params = params;
		
		// Cargamos los caracteres disponibles
		Map<Character, Integer> availableCharacters = InputData.getGameChars(params.lettersFileName);
		// Creamos nuestro diccionario
		Dictionary dictionary = InputData.fillDictionary(
				params.dictionaryFileName,
				InputData.DictionaryFillStrategy.HIGHEST_OCURRENCY,
				availableCharacters);
		
		// Fijamos el tiempo de corte de la partida si el limitada
		if (params.getMaxTime() > 0) {
			this.eta = System.nanoTime()+params.getMaxTime()*1E9;
		}
		
		// Creamos el tablero
		grid = new Board(availableCharacters);
		grid.setDictionary(dictionary);
	}
	
	/**
	 * Metodo que deben implementar mis subclases
	 * para resolver de su forma el algoritmo
	 */
	public abstract void solve();
	
	/**
	 * Llamo a mi metodo de resolucion del problema
	 */
	public void start() {
		
		solve();
	}
	

}
