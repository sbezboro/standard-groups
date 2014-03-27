package com.sbezboro.standardgroups.persistence.storages;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.model.StandardPlayer;
import com.sbezboro.standardplugin.persistence.storages.MultiFileStorage;

public class GroupStorage extends MultiFileStorage<Group> {

	public GroupStorage(StandardPlugin plugin) {
		super(plugin, "groups");
	}
	
	public Group createGroup(String name, StandardPlayer leader) {
		Group group = new Group(this, name, new Date().getTime(), leader);
		
		cacheObject(name, group);
		
		return group;
	}
	
	public void destroyGroup(Group group) {
		group.clearLocks();

		remove(group.getIdentifier());
	}
	
	public Group getGroupByName(String name) {
		return getObject(name);
	}

	public List<Group> getGroups() {
		return new ArrayList<Group>(getObjects());
	}
	
	public Group createObject(String identifier) {
		return new Group(this, identifier);
	}

}
