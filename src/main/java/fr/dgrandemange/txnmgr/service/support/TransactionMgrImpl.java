package fr.dgrandemange.txnmgr.service.support;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.dgrandemange.txnmgr.exception.TransactionBadConfigurationException;
import fr.dgrandemange.txnmgr.exception.TransactionException;
import fr.dgrandemange.txnmgr.exception.TransactionRollbackException;
import fr.dgrandemange.txnmgr.exception.TxMgrException;
import fr.dgrandemange.txnmgr.model.ParticipantsGroup;
import fr.dgrandemange.txnmgr.model.ParticipantsGroupRegistry;
import fr.dgrandemange.txnmgr.model.Transaction;
import fr.dgrandemange.txnmgr.model.TransactionStateEnum;
import fr.dgrandemange.txnmgr.service.IContextMgr;
import fr.dgrandemange.txnmgr.service.IContextMgrFactory;
import fr.dgrandemange.txnmgr.service.IParticipant;
import fr.dgrandemange.txnmgr.service.ITransactionHandler;
import fr.dgrandemange.txnmgr.service.ITransactionMgr;

/**
 * Transaction manager base implementation able to procress concurrent
 * transaction by internally using an executor service<br>
 * 
 * @author dgrandemange
 * 
 */
public class TransactionMgrImpl implements ITransactionMgr {

	private static Logger logger = LoggerFactory
			.getLogger(TransactionMgrImpl.class);

	/**
	 * Name identifying this transaction manager instance
	 */
	private String name;

	/**
	 * Transaction counter
	 */
	private AtomicLong idCounter;

	/**
	 * Groups registry
	 */
	private ParticipantsGroupRegistry registry;

	/**
	 * Context manager factory
	 */
	private IContextMgrFactory ctxMgrFactory;

	/**
	 * Currently running transactions
	 */
	private Map<Transaction, ITransactionHandler> runningTransactions;

	/**
	 * Service dedicated to handle concurrent transaction processing
	 */
	private ExecutorService executor;

	/**
	 * Time to wait for transaction manager to stop
	 */
	private long awaitTermination;

	public TransactionMgrImpl(String name, ParticipantsGroupRegistry registry,
			IContextMgrFactory ctxMgrFactory) {
		super();

		this.name = name;
		this.registry = registry;
		this.ctxMgrFactory = ctxMgrFactory;
	}

	/**
	 * Transaction processing task
	 */
	protected class TransactionTask implements Callable<Transaction> {

		private Transaction transaction;

		public TransactionTask(Transaction transaction) {
			super();
			this.transaction = transaction;
		}

		public Transaction call() throws Exception {
			/**
			 * Stack of processed participants<br>
			 * <br>
			 * If at some point, transaction must be rolled back, then
			 * transaction nominal processing is stopped and all the
			 * participants pushed on the stack are popped (following a LIFO
			 * order) and get their <code>rollback()</code> method invoked.<br>
			 */
			Stack<IParticipant> pstack = new Stack<IParticipant>();
			IContextMgr contextMgr = transaction.getContextMgr();

			try {
				transaction.setState(TransactionStateEnum.RUNNING);
				processGroup(
						ParticipantsGroupRegistry.GROUPS_REGISTRY__ROOT_GROUPNAME,
						registry, contextMgr, pstack, null);
				transaction.setState(TransactionStateEnum.COMMITTED);
			} catch (TransactionException e) {
				transaction.setState(TransactionStateEnum.RUNNING_ROLLBACK);
				List<Throwable> rollbackex = null;

				while (!pstack.empty()) {
					IParticipant participant = null;

					try {
						participant = pstack.pop();
					} catch (EmptyStackException es) {
						// Stack is empty, break loop
						break;
					}

					if (null != participant) {
						try {
							if (logger.isDebugEnabled()) {
								logger.debug(String
										.format("Tx=%d, Participant=%s : before rollback",
												transaction.getId(),
												participant.getClass()
														.getName()));
							}

							participant.rollback(contextMgr);

							if (logger.isDebugEnabled()) {
								logger.debug(String
										.format("Tx=%d, Participant=%s : after rollback",
												transaction.getId(),
												participant.getClass()
														.getName()));
							}
						} catch (Throwable t) {
							// Error while attempting to rollback participant

							// FIXME log error

							if (rollbackex == null) {
								rollbackex = new ArrayList<Throwable>();
							}

							rollbackex.add(t);
						}
					}
				}

				if (rollbackex == null) {
					transaction.setException(e);
				} else {
					transaction.setException(new TransactionRollbackException(
							e, rollbackex));
				}

				transaction.setState(TransactionStateEnum.ROLLED_BACK);
			}

			return transaction;
		}

		/**
		 * @param grpName
		 * @param grpreg
		 * @param ctxmgr
		 * @param pstack
		 * @throws TransactionException
		 */
		public void processGroup(String grpnames,
				ParticipantsGroupRegistry grpreg, IContextMgr ctxmgr,
				Stack<IParticipant> pstack, IParticipant originator)
				throws TransactionException {

			if (null == grpnames) {
				// No more group to process
				return;
			}

			grpnames = grpnames.trim();

			if (grpnames.isEmpty()) {
				// No more group to process
				return;
			}

			StringTokenizer tokenizer = new StringTokenizer(grpnames, " ");
			while (tokenizer.hasMoreTokens()) {
				String grpname = tokenizer.nextToken();

				ParticipantsGroup group = grpreg.getGroup(grpname);

				if (group == null) {
					// configuration issue ? group is not declared ?
					throw new TransactionBadConfigurationException(
							String.format(
									"Unable to find group for name '%s' (selected by participant %s)",
									grpname, originator.getClass().getName()));
				}

				for (Iterator<IParticipant> it = group.iterator(); it.hasNext();) {
					IParticipant participant = it.next();
					pstack.push(participant);

					if (logger.isDebugEnabled()) {
						logger.debug(String.format(
								"Tx=%d, Participant=%s : before execution",
								transaction.getId(), participant.getClass()
										.getName()));
					}

					String nextgroups = participant.execute(ctxmgr);
					if (logger.isDebugEnabled()) {
						logger.debug(String.format(
								"Tx=%d, Participant=%s : after execution",
								transaction.getId(), participant.getClass()
										.getName()));
					}

					processGroup(nextgroups, grpreg, ctxmgr, pstack,
							participant);
				}
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mbs.testtxmgrcfg.service.support.ITransactionMgr#start()
	 */
	public void start() throws TxMgrException {
		idCounter = new AtomicLong(0);
		// TODO Init runningTransactions if needed
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mbs.testtxmgrcfg.service.support.ITransactionMgr#stop()
	 */
	public void stop() throws TxMgrException {
		if (executor != null) {
			executor.shutdown();
			try {
				executor.awaitTermination(awaitTermination,
						TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// TODO log error
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mbs.testtxmgrcfg.service.ITransactionMgr#submit(java.util.Map)
	 */
	public ITransactionHandler submit(Map<String, Object> context)
			throws TxMgrException {

		long nextId = getNextTransactionId();
		IContextMgr contextMgr = ctxMgrFactory.create(context);
		Transaction transaction = new Transaction(nextId, contextMgr);

		TransactionTask task = new TransactionTask(transaction);
		Future<Transaction> future = executor.submit(task);

		// FIXME Manage runningTransactions ?

		return new TransactionHandlerBaseImpl(transaction, future);
	}

	protected long getNextTransactionId() {
		long nextId;
		synchronized (idCounter) {
			nextId = idCounter.incrementAndGet();
			if (nextId == Long.MAX_VALUE) {
				idCounter.set(0);
			}
		}
		return nextId;
	}

	public String getName() {
		return name;
	}

	public ParticipantsGroupRegistry getRegistry() {
		return registry;
	}

	protected void setAwaitTermination(long awaitTermination) {
		this.awaitTermination = awaitTermination;
	}

	protected void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}
}
