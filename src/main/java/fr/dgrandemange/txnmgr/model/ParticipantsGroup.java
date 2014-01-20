package fr.dgrandemange.txnmgr.model;

import java.util.Iterator;
import java.util.List;

import fr.dgrandemange.txnmgr.service.IParticipant;

/**
 * Describes a group of transaction participants
 * 
 * @author dgrandemange
 * 
 */
public class ParticipantsGroup {

	/**
	 * Group's name
	 */
	private String name;

	/**
	 * List of participants of this group sorted by execution order
	 */
	private List<IParticipant> participants;

	public String getName() {
		return name;
	}

	public ParticipantsGroup(String name, List<IParticipant> participants) {
		super();
		this.name = name;
		this.participants = participants;
	}
	
	public Iterator<IParticipant> iterator() {
		return participants.iterator();
	}
}
