package fr.dgrandemange.txnmgr.service;

import fr.dgrandemange.txnmgr.exception.TransactionException;
import fr.dgrandemange.txnmgr.model.Transaction;
import fr.dgrandemange.txnmgr.model.TransactionStateEnum;

/**
 * @author dgrandemange
 * 
 */
public interface ITransactionHandler {

	/**
	 * @return transaction id
	 */
	long getTransactionId();

	/**
	 * @return transaction current state
	 */
	TransactionStateEnum getState();

	/**
	 * Try to cancel handled transaction<br>
	 * 
	 * @return true if transaction has been successfully cancelled, false
	 *         otherwise
	 */
	boolean cancel(boolean mayInterruptIfRunning);

	/**
	 * @param timeout Time to wait (in ms) for a result before returning
	 * @return transaction result
	 * @throws TransactionException when timeout expires or transaction processing has been interrupted 
	 */
	Transaction getResult(long timeout) throws TransactionException;
}
