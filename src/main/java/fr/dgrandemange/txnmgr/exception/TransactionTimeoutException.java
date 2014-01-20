package fr.dgrandemange.txnmgr.exception;

/**
 * @author dgrandemange
 * 
 */
@SuppressWarnings("serial")
public class TransactionTimeoutException extends TransactionException {

	public TransactionTimeoutException() {
		super();
	}

	public TransactionTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public TransactionTimeoutException(String message) {
		super(message);
	}

	public TransactionTimeoutException(Throwable cause) {
		super(cause);
	}

}
