package fr.dgrandemange.txnmgr.service;

import java.util.Map;

/**
 * Describes a IContextMgr factory 
 * 
 * @author dgrandemange
 *
 */
public interface IContextMgrFactory {
	
	/**
	 * @return a context manager
	 */
	IContextMgr create(Map<String, Object> map);
}
