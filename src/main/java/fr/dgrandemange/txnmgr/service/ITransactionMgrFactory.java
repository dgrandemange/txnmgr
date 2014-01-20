package fr.dgrandemange.txnmgr.service;

import fr.dgrandemange.txnmgr.model.ParticipantsGroupRegistry;

/**
 * @author dgrandemange
 *
 */
public interface ITransactionMgrFactory {
	
	/**
	 * @param name
	 * @param registry
	 * @return
	 */
	ITransactionMgr create(String name, ParticipantsGroupRegistry registry);
}
