package fr.dgrandemange.txnmgr.service.support;

import java.util.Collections;
import java.util.Map;

import fr.dgrandemange.txnmgr.service.IContextMgr;
import fr.dgrandemange.txnmgr.service.IContextMgrFactory;

/**
 * A context manager factory that creates a new instance of ContextMgrMapImpl on
 * each call<br>
 * 
 * @author dgrandemange
 * 
 */
public class ContextMgrFactoryBaseImpl implements IContextMgrFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mbs.testtxmgrcfg.service.IContextMgrFactory#create()
	 */
	public IContextMgr create(Map<String, Object> map) {
		return new ContextMgrMapImpl(
				Collections.synchronizedMap(map));
	}
}
