package fr.dgrandemange.txnmgr.model;

import java.util.Map;


/**
 * A registry of groups
 * 
 * @author dgrandemange
 *
 */
public class ParticipantsGroupRegistry {
	public static final String GROUPS_REGISTRY__ROOT_GROUPNAME = "!!!ROOT_GROUP!!!";
	
	private Map<String, ParticipantsGroup> groups;

	public ParticipantsGroupRegistry(Map<String, ParticipantsGroup> groups) {
		super();
		this.groups = groups;
	}

	public ParticipantsGroup getRoot() {
		return groups.get("");
	}

	public ParticipantsGroup getGroup(String name) {
		return groups.get(name);
	}
	
}
