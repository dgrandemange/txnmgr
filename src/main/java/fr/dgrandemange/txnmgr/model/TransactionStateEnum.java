package fr.dgrandemange.txnmgr.model;

/**
 * Enumerates the available states for a transaction<br>
 * <br>
 * At creation, transaction is set to <code>INITIAL</code> state.<br>
 * When processed by transaction manager, state moves to <code>RUNNING</code>. <br>
 * Eventually, if no exception occurs, state moves to <code>COMMITTED</code>.<br>
 * <br>
 * If an exception occurs, and transaction enters in a roll back phase :<br>
 * <li>while transaction is being rolled back, state is in
 * <code>RUNNING_ROLLBACK</code> state,</li><br>
 * <li>when rollback is completed, state eventually moves to
 * <code>ROLLED_BACK</code></li><br>
 * <br>
 * @author dgrandemange
 * 
 */
public enum TransactionStateEnum {
	INITIAL, RUNNING, RUNNING_ROLLBACK, COMMITTED, ROLLED_BACK;
}
