package eda.scrabble;

public class AddWordException extends Exception {

	
	private static final long serialVersionUID = 1L;

	public AddWordException() {
	}

	public AddWordException(String message) {
		super(message);
	}

	public AddWordException(Throwable cause) {
		super(cause);
	}

	public AddWordException(String message, Throwable cause) {
		super(message, cause);
	}

}
