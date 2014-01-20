package fr.dgrandemange.txnmgr.service;

import fr.dgrandemange.txnmgr.exception.TransactionException;

/**
 * @author dgrandemange
 * 
 */
public interface IParticipant {

	/**
	 * @param contextMgr
	 * @return space delimited list of next group(s) of participant(s) to execute if any, null
	 *         otherwise
	 * @throws TransactionException
	 */
	String execute(IContextMgr contextMgr) throws TransactionException;

	/**
	 * @param contextMgr
	 * @throws TransactionException
	 */
	void rollback(IContextMgr contextMgr) throws TransactionException;
}
