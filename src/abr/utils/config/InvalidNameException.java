package abr.utils.config;

public class InvalidNameException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = -8162918610060867653L;

	public InvalidNameException(String str) {
		super(str);
	}

	public InvalidNameException(String str, Throwable arg1) {
		super(str, arg1);
	}
}
