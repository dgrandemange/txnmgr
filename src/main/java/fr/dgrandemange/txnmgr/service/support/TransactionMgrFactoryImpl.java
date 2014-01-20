package fr.dgrandemange.txnmgr.service.support;

import java.util.concurrent.Executors;

import fr.dgrandemange.txnmgr.model.ParticipantsGroupRegistry;
import fr.dgrandemange.txnmgr.service.IContextMgrFactory;
import fr.dgrandemange.txnmgr.service.ITransactionMgr;
import fr.dgrandemange.txnmgr.service.ITransactionMgrFactory;

/**
 * Factory of transaction managers based on <code>TransactionMgrImpl</code>
 * implementation<br>
 * 
 * @author dgrandemange
 * 
 */
public class TransactionMgrFactoryImpl implements ITransactionMgrFactory {
	public static final int DEFAULT_MAX_CONCURRENT_TRANSACTIONS = 100;
	public static final int DEFAULT_AWAIT_TERMINATION = 60000;

	private IContextMgrFactory ctxMgrFactory = new ContextMgrFactoryBaseImpl();

	/**
	 * Time to wait for transaction manager to stop
	 */
	private long awaitTermination = DEFAULT_AWAIT_TERMINATION;

	/**
	 * Maximum of parallel transactions
	 */
	private int maxTx = DEFAULT_MAX_CONCURRENT_TRANSACTIONS;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mbs.txnmgr.service.ITransactionMgrFactory#create(java.lang.String,
	 * com.mbs.txnmgr.model.ParticipantsGroupRegistry)
	 */
	public ITransactionMgr create(String name,
			ParticipantsGroupRegistry registry) {
		TransactionMgrImpl transactionMgrImpl = new TransactionMgrImpl(name,
				registry, ctxMgrFactory);
		transactionMgrImpl
				.setExecutor(Executors.newFixedThreadPool(this.maxTx));
		transactionMgrImpl.setAwaitTermination(awaitTermination);
		return transactionMgrImpl;
	}

	public IContextMgrFactory getCtxMgrFactory() {
		return ctxMgrFactory;
	}

	public void setCtxMgrFactory(IContextMgrFactory ctxMgrFactory) {
		this.ctxMgrFactory = ctxMgrFactory;
	}

	public int getMaxTx() {
		return maxTx;
	}

	public void setMaxTx(int maxTx) {
		if (maxTx >= 1) {
			this.maxTx = maxTx;
		} else {
			this.maxTx = DEFAULT_MAX_CONCURRENT_TRANSACTIONS;
		}
	}

	protected long getAwaitTermination() {
		return awaitTermination;
	}

	protected void setAwaitTermination(long awaitTermination) {
		this.awaitTermination = awaitTermination;
	}

}
