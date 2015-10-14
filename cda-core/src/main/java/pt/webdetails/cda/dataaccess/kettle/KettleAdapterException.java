package pt.webdetails.cda.dataaccess.kettle;

public class KettleAdapterException extends Exception {
	private static final long serialVersionUID = 1L;

	public KettleAdapterException(String message, Throwable cause) {
		super(message, cause);
	}

	public KettleAdapterException(String message) {
		super(message);
	}

	public KettleAdapterException(Throwable cause) {
		super(cause);
	}
}
