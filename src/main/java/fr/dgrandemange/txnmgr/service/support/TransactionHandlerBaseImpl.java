package fr.dgrandemange.txnmgr.service.support;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import fr.dgrandemange.txnmgr.exception.TransactionException;
import fr.dgrandemange.txnmgr.exception.TransactionTimeoutException;
import fr.dgrandemange.txnmgr.model.Transaction;
import fr.dgrandemange.txnmgr.model.TransactionStateEnum;
import fr.dgrandemange.txnmgr.service.ITransactionHandler;

/**
 * @author dgrandemange
 * 
 */
public class TransactionHandlerBaseImpl implements ITransactionHandler {

	private Transaction transaction;

	private Future<Transaction> future;

	public TransactionHandlerBaseImpl(Transaction transaction,
			Future<Transaction> future) {
		super();
		this.transaction = transaction;
		this.future = future;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mbs.testtxmgrcfg.service.ITransactionHandler#getTransactionId()
	 */
	public long getTransactionId() {
		return transaction.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mbs.testtxmgrcfg.service.ITransactionHandler#getState()
	 */
	public TransactionStateEnum getState() {
		return transaction.getState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mbs.testtxmgrcfg.service.ITransactionHandler#cancel(boolean)
	 */
	public boolean cancel(boolean mayInterruptIfRunning) {
		return future.cancel(mayInterruptIfRunning);
	}

	/* (non-Javadoc)
	 * @see com.mbs.testtxmgrcfg.service.ITransactionHandler#getResult(long)
	 */
	public Transaction getResult(long timeout) throws TransactionException {
		try {
			return future.get(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			throw new TransactionException(e);
		} catch (ExecutionException e) {
			throw new TransactionException(e.getCause());
		} catch (TimeoutException e) {
			throw new TransactionTimeoutException();
		}
	}

}
