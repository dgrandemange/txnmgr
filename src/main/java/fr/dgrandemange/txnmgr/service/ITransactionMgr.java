package fr.dgrandemange.txnmgr.service;

import java.util.Map;

import fr.dgrandemange.txnmgr.exception.TxMgrException;

/**
 * Describes a transaction manager service
 * 
 * @author dgrandemange
 * 
 */
public interface ITransactionMgr {

	/**
	 * Start transaction manager
	 * 
	 * @throws TxMgrException
	 */
	void start() throws TxMgrException;

	/**
	 * Stop transaction manager
	 * 
	 * @throws TxMgrException
	 */
	void stop() throws TxMgrException;

	/**
	 * Submit a new transaction processing task to the transaction manager
	 * 
	 * @param contextMgr
	 * @return a transaction handler
	 * @throws TxMgrException
	 */
	ITransactionHandler submit(Map<String, Object> context) throws TxMgrException;
}
