package fr.dgrandemange.txnmgr.exception;

import java.util.List;

/**
 * Rollback transaction exception wrapping the transaction exception that
 * initially caused the rollback, plus the possible exceptions occurred during
 * rollback phase
 * 
 * @author dgrandemange
 * 
 */
@SuppressWarnings("serial")
public class TransactionRollbackException extends TransactionException {

	/**
	 * List of all exceptions occurred during transaction rollback
	 */
	private List<Throwable> rbexs;

	public TransactionRollbackException(TransactionException transactionex) {
		super(transactionex.getCause());		
	}
	
	public TransactionRollbackException(TransactionException transactionex,
			List<Throwable> rbexs) {
		super(transactionex.getCause());
		this.rbexs = rbexs;
	}

	public List<Throwable> getRbexs() {
		return rbexs;
	}

	@Override
	public String toString() {
		// TODO See if we should dump nested rollback exceptions
		return super.toString();
	}

}
