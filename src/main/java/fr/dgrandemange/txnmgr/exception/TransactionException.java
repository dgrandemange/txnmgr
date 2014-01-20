package fr.dgrandemange.txnmgr.exception;

/**
 * A transaction exception
 * 
 * @author dgrandemange
 *
 */
@SuppressWarnings("serial")
public class TransactionException extends Exception {
	
	public TransactionException() {
		super();
	}

	public TransactionException(String message, Throwable cause) {
		super(message, cause);
	}

	public TransactionException(String message) {
		super(message);
	}

	public TransactionException(Throwable cause) {
		super(cause);
	}

}
