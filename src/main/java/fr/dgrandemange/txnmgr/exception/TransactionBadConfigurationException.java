package fr.dgrandemange.txnmgr.exception;

/**
 * @author dgrandemange
 * 
 */
@SuppressWarnings("serial")
public class TransactionBadConfigurationException extends TransactionException {

	public TransactionBadConfigurationException() {
		super();
	}

	public TransactionBadConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public TransactionBadConfigurationException(String message) {
		super(message);
	}

	public TransactionBadConfigurationException(Throwable cause) {
		super(cause);
	}

}
