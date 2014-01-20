package fr.dgrandemange.txnmgr.service;

import java.util.Iterator;

/**
 * A simple context management interface
 * 
 * @author dgrandemange
 * 
 */
public interface IContextMgr {

	/**
	 * @param key
	 * @param value
	 */
	void add(String key, Object value);

	/**
	 * @param key
	 * @return
	 */
	Object remove(String key);

	/**
	 * @param key
	 * @return
	 */
	Object get(String key);
	
	/**
	 * @return iterator on all registered attributes in context
	 */
	Iterator<String> keyIterator();
	
	/**
	 * Clear context
	 */
	void clear();
}
