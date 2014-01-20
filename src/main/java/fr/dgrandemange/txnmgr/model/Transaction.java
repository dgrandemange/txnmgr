package fr.dgrandemange.txnmgr.model;

import fr.dgrandemange.txnmgr.exception.TransactionException;
import fr.dgrandemange.txnmgr.service.IContextMgr;

/**
 * Describes a transaction
 * 
 * @author dgrandemange
 * 
 */
public class Transaction {
	
	/**
	 * Transaction id
	 */
	private long id;
	
	/**
	 * Transaction context manager
	 */
	private IContextMgr contextMgr;
	
	/**
	 * Transaction state
	 */
	private TransactionStateEnum state;

	/**
	 * Transaction exception if one has occurred
	 */
	private TransactionException exception;

	public Transaction(long id, IContextMgr contextMgr) {
		super();
		this.id = id;
		this.contextMgr = contextMgr;
		this.state = TransactionStateEnum.INITIAL;
	}

	public TransactionStateEnum getState() {
		return state;
	}

	public void setState(TransactionStateEnum state) {
		this.state = state;
	}

	public long getId() {
		return id;
	}

	public IContextMgr getContextMgr() {
		return contextMgr;
	}
	
	public TransactionException getException() {
		return exception;
	}

	public void setException(TransactionException ex) {
		this.exception = ex;
	}
}
