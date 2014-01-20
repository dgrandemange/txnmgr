package fr.dgrandemange.txnmgr.exception;

/**
 * A transaction manager exception
 * 
 * @author dgrandemange
 *
 */
@SuppressWarnings("serial")
public class TxMgrException extends Exception {

	public TxMgrException() {
		super();
	}

	public TxMgrException(String message, Throwable cause) {
		super(message, cause);
	}

	public TxMgrException(String message) {
		super(message);
	}

	public TxMgrException(Throwable cause) {
		super(cause);
	}

}
