package fr.dgrandemange.txnmgr.service.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.dgrandemange.txnmgr.exception.TransactionBadConfigurationException;
import fr.dgrandemange.txnmgr.exception.TransactionException;
import fr.dgrandemange.txnmgr.exception.TransactionRollbackException;
import fr.dgrandemange.txnmgr.exception.TransactionTimeoutException;
import fr.dgrandemange.txnmgr.exception.TxMgrException;
import fr.dgrandemange.txnmgr.model.ParticipantsGroup;
import fr.dgrandemange.txnmgr.model.ParticipantsGroupRegistry;
import fr.dgrandemange.txnmgr.model.Transaction;
import fr.dgrandemange.txnmgr.model.TransactionStateEnum;
import fr.dgrandemange.txnmgr.service.IContextMgr;
import fr.dgrandemange.txnmgr.service.IContextMgrFactory;
import fr.dgrandemange.txnmgr.service.IParticipant;
import fr.dgrandemange.txnmgr.service.ITransactionHandler;
import fr.dgrandemange.txnmgr.service.support.ContextMgrFactoryBaseImpl;
import fr.dgrandemange.txnmgr.service.support.TransactionMgrImpl;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

/**
 * @author dgrandemange
 * 
 */
public class TransactionMgrImplTest {
	private static final String CTX_ATTR__ROLLED_BACK_PARTICIPANTS = "rolled_back_participants";
	private static final String CTX_ATTR__EXECUTED_PARTICIPANTS = "executed_participants";

	private TransactionMgrImpl txMgr;
	private Map<String, ParticipantsGroup> groups;
	private ParticipantsGroupRegistry registry;
	private IContextMgrFactory ctxMgrFactory;
	private Map<String, Object> context;
	private List<String> executedParticipantsList;
	private List<String> rolledBackParticipantsList;
	private IllegalStateException someDummyException;

	static class DummyParticipantBaseImpl implements IParticipant {

		private String id;
		private String next;
		private Throwable exception;
		private boolean throwExOnRollback;
		private long respDelay = 0L;

		public DummyParticipantBaseImpl(String id, String next) {
			super();
			this.id = id;
			this.next = next;
		}

		public DummyParticipantBaseImpl(String id, Throwable txEx) {
			super();
			this.id = id;
			this.exception = txEx;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.mbs.testtxmgrcfg.service.IParticipant#execute(com.mbs.testtxmgrcfg
		 * .service.IContextMgr)
		 */
		@SuppressWarnings("unchecked")
		public String execute(IContextMgr contextMgr)
				throws TransactionException {
			List<String> executedParticipants = (List<String>) contextMgr
					.get(CTX_ATTR__EXECUTED_PARTICIPANTS);
			executedParticipants.add(this.id);
			if (exception != null) {
				throw new TransactionException(exception);
			} else {
				try {
					Thread.sleep(respDelay);
				} catch (InterruptedException e) {
				}
				return next;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.mbs.testtxmgrcfg.service.IParticipant#rollback(com.mbs.testtxmgrcfg
		 * .service.IContextMgr)
		 */
		@SuppressWarnings("unchecked")
		public void rollback(IContextMgr contextMgr)
				throws TransactionException {
			List<String> rolledbackParticipants = (List<String>) contextMgr
					.get(CTX_ATTR__ROLLED_BACK_PARTICIPANTS);
			rolledbackParticipants.add(id);

			if (throwExOnRollback) {
				throw new RuntimeException("bang!");
			}
		}

		public boolean isThrowExOnRollback() {
			return throwExOnRollback;
		}

		public void setThrowExOnRollback(boolean throwExOnRollback) {
			this.throwExOnRollback = throwExOnRollback;
		}

		public String getId() {
			return id;
		}

		public String getNext() {
			return next;
		}

		public Throwable getException() {
			return exception;
		}

		public long getRespDelay() {
			return respDelay;
		}

		public void setRespDelay(long respDelay) {
			this.respDelay = respDelay;
		}

	}

	@Before
	public void setUp() throws TxMgrException {
		groups = new HashMap<String, ParticipantsGroup>();
		registry = new ParticipantsGroupRegistry(groups);
		int maxTx = 1;
		ctxMgrFactory = new ContextMgrFactoryBaseImpl();
		txMgr = new TransactionMgrImpl("someDummyTxMgr", registry,
				ctxMgrFactory);
		txMgr.setAwaitTermination(60000);
		txMgr.setExecutor(Executors.newFixedThreadPool(maxTx));
		txMgr.start();

		context = new HashMap<String, Object>();
		executedParticipantsList = new ArrayList<String>();
		context.put(CTX_ATTR__EXECUTED_PARTICIPANTS, executedParticipantsList);
		rolledBackParticipantsList = new ArrayList<String>();
		context.put(CTX_ATTR__ROLLED_BACK_PARTICIPANTS,
				rolledBackParticipantsList);
		someDummyException = new IllegalStateException();
	}

	@After
	public void tearDown() throws TxMgrException {
		txMgr.stop();
	}

	@Test
	public void testSubmit_Conf1_NominalCase() throws TxMgrException,
			TransactionException {
		List<IParticipant> rootGrps = new ArrayList<IParticipant>();
		rootGrps.add(new DummyParticipantBaseImpl("ptp1", ""));
		ParticipantsGroup rootGrp = new ParticipantsGroup(
				ParticipantsGroupRegistry.GROUPS_REGISTRY__ROOT_GROUPNAME, rootGrps);
		groups.put(rootGrp.getName(), rootGrp);

		ITransactionHandler txHandler = txMgr.submit(context);
		Transaction result = txHandler.getResult(1000);

		assertThat(result.getState()).isEqualTo(TransactionStateEnum.COMMITTED);
		assertThat(result.getException()).isNull();
		assertThat(result.getId()).isGreaterThan(0);
		checkExecutedParticipants("ptp1", result.getContextMgr());
		checkRolledBackParticipants("", result.getContextMgr());
	}

	@Test
	public void testSubmit_Conf1_TransactionException() throws TxMgrException,
			TransactionException {
		List<IParticipant> rootGrps = new ArrayList<IParticipant>();
		rootGrps.add(new DummyParticipantBaseImpl("ptp1", someDummyException));
		ParticipantsGroup rootGrp = new ParticipantsGroup(
				ParticipantsGroupRegistry.GROUPS_REGISTRY__ROOT_GROUPNAME, rootGrps);
		groups.put(rootGrp.getName(), rootGrp);

		ITransactionHandler txHandler = txMgr.submit(context);
		Transaction result = txHandler.getResult(1000);

		assertThat(result.getState()).isEqualTo(
				TransactionStateEnum.ROLLED_BACK);
		assertThat(result.getException().getCause()).isEqualTo(
				someDummyException);
		assertThat(result.getId()).isGreaterThan(0);
		checkExecutedParticipants("ptp1", result.getContextMgr());
		checkRolledBackParticipants("ptp1", result.getContextMgr());
	}

	@Test
	public void testSubmit_Conf1_BadConfiguration() throws TxMgrException,
			TransactionException {
		List<IParticipant> rootGrps = new ArrayList<IParticipant>();
		rootGrps.add(new DummyParticipantBaseImpl("ptp1", "grp2"));
		ParticipantsGroup rootGrp = new ParticipantsGroup(
				ParticipantsGroupRegistry.GROUPS_REGISTRY__ROOT_GROUPNAME, rootGrps);
		groups.put(rootGrp.getName(), rootGrp);

		// We do not declare group 'grp2' to provoke our bad configuration
		// exception

		ITransactionHandler txHandler = txMgr.submit(context);
		Transaction result = txHandler.getResult(1000);

		assertThat(result.getState()).isEqualTo(
				TransactionStateEnum.ROLLED_BACK);
		assertThat(result.getException()).isInstanceOf(
				TransactionBadConfigurationException.class);
		assertThat(result.getId()).isGreaterThan(0);
		checkExecutedParticipants("ptp1", result.getContextMgr());
		checkRolledBackParticipants("ptp1", result.getContextMgr());
	}

	@Test
	public void testSubmit_Conf1_DelayExpired() throws TxMgrException,
			TransactionException {
		List<IParticipant> rootGrps = new ArrayList<IParticipant>();
		DummyParticipantBaseImpl ptp1 = new DummyParticipantBaseImpl("ptp1", "");
		rootGrps.add(ptp1);
		ptp1.setRespDelay(500);
		ParticipantsGroup rootGrp = new ParticipantsGroup(
				ParticipantsGroupRegistry.GROUPS_REGISTRY__ROOT_GROUPNAME, rootGrps);
		groups.put(rootGrp.getName(), rootGrp);

		ITransactionHandler txHandler = txMgr.submit(context);

		Transaction result;
		try {
			result = txHandler.getResult(100);
			fail("A transaction timeout exception was expected here");
		} catch (TransactionTimeoutException e) {
			// Timeout is expected
		}

		assertThat(txHandler.getState())
				.isEqualTo(TransactionStateEnum.RUNNING);

		// We wait One more second for transaction to be fully processed
		result = txHandler.getResult(1000);

		assertThat(result.getState()).isEqualTo(TransactionStateEnum.COMMITTED);
		assertThat(result.getException()).isNull();
		assertThat(result.getId()).isGreaterThan(0);
		checkExecutedParticipants("ptp1", result.getContextMgr());
		checkRolledBackParticipants("", result.getContextMgr());
	}

	@Test
	public void testSubmit_Conf2_NominalCase() throws TxMgrException,
			TransactionException {
		List<IParticipant> rootGrps = new ArrayList<IParticipant>();
		rootGrps.add(new DummyParticipantBaseImpl("ptp1", ""));
		rootGrps.add(new DummyParticipantBaseImpl("ptp2", ""));
		ParticipantsGroup rootGrp = new ParticipantsGroup(
				ParticipantsGroupRegistry.GROUPS_REGISTRY__ROOT_GROUPNAME, rootGrps);
		groups.put(rootGrp.getName(), rootGrp);

		ITransactionHandler txHandler = txMgr.submit(context);
		Transaction result = txHandler.getResult(1000);

		assertThat(result.getState()).isEqualTo(TransactionStateEnum.COMMITTED);
		assertThat(result.getException()).isNull();
		assertThat(result.getId()).isGreaterThan(0);
		checkExecutedParticipants("ptp1 ptp2", result.getContextMgr());
	}

	@Test
	public void testSubmit_Conf2_TransactionException_OnFirstParticipant()
			throws TxMgrException, TransactionException {
		List<IParticipant> rootGrps = new ArrayList<IParticipant>();
		rootGrps.add(new DummyParticipantBaseImpl("ptp1", someDummyException));
		rootGrps.add(new DummyParticipantBaseImpl("ptp2", ""));
		ParticipantsGroup rootGrp = new ParticipantsGroup(
				ParticipantsGroupRegistry.GROUPS_REGISTRY__ROOT_GROUPNAME, rootGrps);
		groups.put(rootGrp.getName(), rootGrp);

		ITransactionHandler txHandler = txMgr.submit(context);
		Transaction result = txHandler.getResult(1000);

		assertThat(result.getState()).isEqualTo(
				TransactionStateEnum.ROLLED_BACK);
		assertThat(result.getException().getCause()).isEqualTo(
				someDummyException);
		assertThat(result.getId()).isGreaterThan(0);
		checkExecutedParticipants("ptp1", result.getContextMgr());
		checkRolledBackParticipants("ptp1", result.getContextMgr());
	}

	@Test
	public void testSubmit_Conf2_TransactionException_OnLastParticipant()
			throws TxMgrException, TransactionException {
		List<IParticipant> rootGrps = new ArrayList<IParticipant>();
		rootGrps.add(new DummyParticipantBaseImpl("ptp1", ""));
		rootGrps.add(new DummyParticipantBaseImpl("ptp2", someDummyException));
		ParticipantsGroup rootGrp = new ParticipantsGroup(
				ParticipantsGroupRegistry.GROUPS_REGISTRY__ROOT_GROUPNAME, rootGrps);
		groups.put(rootGrp.getName(), rootGrp);

		ITransactionHandler txHandler = txMgr.submit(context);
		Transaction result = txHandler.getResult(1000);

		assertThat(result.getState()).isEqualTo(
				TransactionStateEnum.ROLLED_BACK);
		assertThat(result.getException().getCause()).isEqualTo(
				someDummyException);
		assertThat(result.getId()).isGreaterThan(0);
		checkExecutedParticipants("ptp1 ptp2", result.getContextMgr());
		checkRolledBackParticipants("ptp2 ptp1", result.getContextMgr());
	}

	@Test
	public void testSubmit_Conf3_ChoosePath1() throws TxMgrException,
			TransactionException {
		List<IParticipant> rootGrpPtps = new ArrayList<IParticipant>();
		rootGrpPtps.add(new DummyParticipantBaseImpl("ptp1", "grp1"));
		rootGrpPtps.add(new DummyParticipantBaseImpl("ptp2", ""));
		ParticipantsGroup rootGrp = new ParticipantsGroup(
				ParticipantsGroupRegistry.GROUPS_REGISTRY__ROOT_GROUPNAME, rootGrpPtps);
		groups.put(rootGrp.getName(), rootGrp);

		List<IParticipant> grp1Ptps = new ArrayList<IParticipant>();
		grp1Ptps.add(new DummyParticipantBaseImpl("grp1.ptp1", ""));
		ParticipantsGroup grp1 = new ParticipantsGroup("grp1", grp1Ptps);
		groups.put(grp1.getName(), grp1);

		ITransactionHandler txHandler = txMgr.submit(context);
		Transaction result = txHandler.getResult(1000);

		assertThat(result.getState()).isEqualTo(TransactionStateEnum.COMMITTED);
		assertThat(result.getException()).isNull();
		assertThat(result.getId()).isGreaterThan(0);
		checkExecutedParticipants("ptp1 grp1.ptp1 ptp2", result.getContextMgr());
	}

	@Test
	public void testSubmit_Conf3_ChoosePath2() throws TxMgrException,
			TransactionException {
		List<IParticipant> rootGrpPtps = new ArrayList<IParticipant>();
		rootGrpPtps.add(new DummyParticipantBaseImpl("ptp1", "grp2"));
		rootGrpPtps.add(new DummyParticipantBaseImpl("ptp2", ""));
		ParticipantsGroup rootGrp = new ParticipantsGroup(
				ParticipantsGroupRegistry.GROUPS_REGISTRY__ROOT_GROUPNAME, rootGrpPtps);
		groups.put(rootGrp.getName(), rootGrp);

		List<IParticipant> grp2Ptps = new ArrayList<IParticipant>();
		grp2Ptps.add(new DummyParticipantBaseImpl("grp2.ptp1", ""));
		grp2Ptps.add(new DummyParticipantBaseImpl("grp2.ptp2", ""));
		ParticipantsGroup grp2 = new ParticipantsGroup("grp2", grp2Ptps);
		groups.put(grp2.getName(), grp2);

		ITransactionHandler txHandler = txMgr.submit(context);
		Transaction result = txHandler.getResult(1000);

		assertThat(result.getState()).isEqualTo(TransactionStateEnum.COMMITTED);
		assertThat(result.getException()).isNull();
		assertThat(result.getId()).isGreaterThan(0);
		checkExecutedParticipants("ptp1 grp2.ptp1 grp2.ptp2 ptp2",
				result.getContextMgr());
	}

	@Test
	public void testSubmit_Conf3_ChoosePath2_TransactionExceptionInGrp2Ptp2_ExceptionWhileRollbackingInGrp2Ptp1()
			throws TxMgrException, TransactionException {
		List<IParticipant> rootGrpPtps = new ArrayList<IParticipant>();
		rootGrpPtps.add(new DummyParticipantBaseImpl("ptp1", "grp2"));
		rootGrpPtps.add(new DummyParticipantBaseImpl("ptp2", ""));
		ParticipantsGroup rootGrp = new ParticipantsGroup(
				ParticipantsGroupRegistry.GROUPS_REGISTRY__ROOT_GROUPNAME, rootGrpPtps);
		groups.put(rootGrp.getName(), rootGrp);

		List<IParticipant> grp2Ptps = new ArrayList<IParticipant>();
		DummyParticipantBaseImpl grp2ptp1 = new DummyParticipantBaseImpl(
				"grp2.ptp1", "");
		grp2ptp1.setThrowExOnRollback(true);
		grp2Ptps.add(grp2ptp1);
		grp2Ptps.add(new DummyParticipantBaseImpl("grp2.ptp2",
				someDummyException));
		ParticipantsGroup grp2 = new ParticipantsGroup("grp2", grp2Ptps);
		groups.put(grp2.getName(), grp2);

		ITransactionHandler txHandler = txMgr.submit(context);
		Transaction result = txHandler.getResult(1000);

		assertThat(result.getState()).isEqualTo(
				TransactionStateEnum.ROLLED_BACK);

		assertThat(result.getException().getCause()).isEqualTo(
				someDummyException);
		assertThat(result.getException()).isInstanceOf(
				TransactionRollbackException.class);
		List<Throwable> rbexs = ((TransactionRollbackException) result
				.getException()).getRbexs();
		assertThat(rbexs.size()).isEqualTo(1);
		assertThat(rbexs.get(0).getMessage()).isEqualTo("bang!");

		assertThat(result.getId()).isGreaterThan(0);
		checkExecutedParticipants("ptp1 grp2.ptp1 grp2.ptp2",
				result.getContextMgr());
		checkRolledBackParticipants("grp2.ptp2 grp2.ptp1 ptp1",
				result.getContextMgr());
	}

	@Test
	public void testSubmit_Conf4_ChooseLongestPath() throws TxMgrException,
			TransactionException {
		List<IParticipant> rootGrpPtps = new ArrayList<IParticipant>();
		rootGrpPtps.add(new DummyParticipantBaseImpl("ptp1", "grp2"));
		rootGrpPtps.add(new DummyParticipantBaseImpl("ptp2", ""));
		ParticipantsGroup rootGrp = new ParticipantsGroup(
				ParticipantsGroupRegistry.GROUPS_REGISTRY__ROOT_GROUPNAME, rootGrpPtps);
		groups.put(rootGrp.getName(), rootGrp);

		List<IParticipant> grp2Ptps = new ArrayList<IParticipant>();
		grp2Ptps.add(new DummyParticipantBaseImpl("grp2.ptp1", "grp3"));
		grp2Ptps.add(new DummyParticipantBaseImpl("grp2.ptp2", ""));
		ParticipantsGroup grp2 = new ParticipantsGroup("grp2", grp2Ptps);
		groups.put(grp2.getName(), grp2);

		List<IParticipant> grp3Ptps = new ArrayList<IParticipant>();
		grp3Ptps.add(new DummyParticipantBaseImpl("grp3.ptp1", ""));
		ParticipantsGroup grp3 = new ParticipantsGroup("grp3", grp3Ptps);
		groups.put(grp3.getName(), grp3);

		ITransactionHandler txHandler = txMgr.submit(context);
		Transaction result = txHandler.getResult(1000);

		assertThat(result.getState()).isEqualTo(TransactionStateEnum.COMMITTED);
		assertThat(result.getException()).isNull();
		assertThat(result.getId()).isGreaterThan(0);
		checkExecutedParticipants("ptp1 grp2.ptp1 grp3.ptp1 grp2.ptp2 ptp2",
				result.getContextMgr());
	}

	@Test
	public void testSubmit_Conf4_ChooseLongestPath_TransactionExceptionInGrp3Ptp1()
			throws TxMgrException, TransactionException {
		List<IParticipant> rootGrpPtps = new ArrayList<IParticipant>();
		rootGrpPtps.add(new DummyParticipantBaseImpl("ptp1", "grp2"));
		rootGrpPtps.add(new DummyParticipantBaseImpl("ptp2", ""));
		ParticipantsGroup rootGrp = new ParticipantsGroup(
				ParticipantsGroupRegistry.GROUPS_REGISTRY__ROOT_GROUPNAME, rootGrpPtps);
		groups.put(rootGrp.getName(), rootGrp);

		List<IParticipant> grp2Ptps = new ArrayList<IParticipant>();
		grp2Ptps.add(new DummyParticipantBaseImpl("grp2.ptp1", "grp3"));
		grp2Ptps.add(new DummyParticipantBaseImpl("grp2.ptp2", ""));
		ParticipantsGroup grp2 = new ParticipantsGroup("grp2", grp2Ptps);
		groups.put(grp2.getName(), grp2);

		List<IParticipant> grp3Ptps = new ArrayList<IParticipant>();
		grp3Ptps.add(new DummyParticipantBaseImpl("grp3.ptp1",
				someDummyException));
		ParticipantsGroup grp3 = new ParticipantsGroup("grp3", grp3Ptps);
		groups.put(grp3.getName(), grp3);

		ITransactionHandler txHandler = txMgr.submit(context);
		Transaction result = txHandler.getResult(1000);

		assertThat(result.getState()).isEqualTo(
				TransactionStateEnum.ROLLED_BACK);
		assertThat(result.getException().getCause()).isEqualTo(
				someDummyException);
		assertThat(result.getId()).isGreaterThan(0);
		checkExecutedParticipants("ptp1 grp2.ptp1 grp3.ptp1",
				result.getContextMgr());
		checkRolledBackParticipants("grp3.ptp1 grp2.ptp1 ptp1",
				result.getContextMgr());
	}

	protected void checkExecutedParticipants(String expected,
			IContextMgr contextMgr) {
		if (null == expected) {
			return;
		}

		expected = expected.trim();
		if (expected.isEmpty()) {
			return;
		}
		List<String> executedParticipants = (List<String>) contextMgr
				.get(CTX_ATTR__EXECUTED_PARTICIPANTS);
		StringTokenizer tokenizer = new StringTokenizer(expected, " ");
		int idx = 0;
		while (tokenizer.hasMoreTokens()) {
			if (executedParticipants.size() <= idx) {
				fail("executed participants size does not match");
			}
			String ptpId = executedParticipants.get(idx);

			assertThat(ptpId).isEqualTo(tokenizer.nextToken());
			idx++;
		}

		if (idx != executedParticipants.size()) {
			fail("executed participants size does not match");
		}
	}

	protected void checkRolledBackParticipants(String expected,
			IContextMgr contextMgr) {
		if (null == expected) {
			return;
		}

		expected = expected.trim();
		if (expected.isEmpty()) {
			return;
		}
		List<String> rolledbackParticipants = (List<String>) contextMgr
				.get(CTX_ATTR__ROLLED_BACK_PARTICIPANTS);
		StringTokenizer tokenizer = new StringTokenizer(expected, " ");
		int idx = 0;
		while (tokenizer.hasMoreTokens()) {
			if (rolledbackParticipants.size() <= idx) {
				fail("rolled back participants size does not match");
			}
			String ptpId = rolledbackParticipants.get(idx);
			assertThat(ptpId).isEqualTo(tokenizer.nextToken());
			idx++;
		}

		if (idx != rolledbackParticipants.size()) {
			fail("rolled back participants size does not match");
		}
	}
}
