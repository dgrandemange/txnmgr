package fr.dgrandemange.txnmgr.service.support;

import java.util.Iterator;
import java.util.Map;

import fr.dgrandemange.txnmgr.service.IContextMgr;

/**
 * Context management using an embedded map<br>
 * 
 * @author dgrandemange
 *
 */
public class ContextMgrMapImpl implements IContextMgr {

	private Map<String, Object> map;
	
	public ContextMgrMapImpl(Map<String, Object> map) {
		super();
		this.map = map;
	}

	/* (non-Javadoc)
	 * @see com.mbs.testtxmgrcfg.service.IContextMgr#add(java.lang.String, java.lang.Object)
	 */
	public void add(String key, Object value) {
		map.put(key, value);
	}

	/* (non-Javadoc)
	 * @see com.mbs.testtxmgrcfg.service.IContextMgr#remove(java.lang.String)
	 */
	public Object remove(String key) {
		return map.remove(key);
	}

	/* (non-Javadoc)
	 * @see com.mbs.testtxmgrcfg.service.IContextMgr#get(java.lang.String)
	 */
	public Object get(String key) {
		return map.get(key);
	}

	/* (non-Javadoc)
	 * @see com.mbs.testtxmgrcfg.service.IContextMgr#keyIterator()
	 */
	public Iterator<String> keyIterator() {
		return map.keySet().iterator();
	}

	/* (non-Javadoc)
	 * @see com.mbs.testtxmgrcfg.service.IContextMgr#clear()
	 */
	public void clear() {
		map.clear();
	}

}
