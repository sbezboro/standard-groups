package com.sbezboro.standardgroups.model;

import java.util.ArrayList;
import java.util.List;

import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.model.StandardPlayer;
import com.sbezboro.standardplugin.persistence.PersistedListProperty;
import com.sbezboro.standardplugin.persistence.PersistedObject;
import com.sbezboro.standardplugin.persistence.storages.FileStorage;

public class Group extends PersistedObject {
	public PersistedListProperty<String> members;
	public PersistedListProperty<String> claims;
	public PersistedListProperty<String> invites;

	public Group(FileStorage storage, String name) {
		super(storage, name);
	}

	@Override
	public void createProperties() {
		members = createList(String.class, "members");
		claims = createList(String.class, "claims");
		invites = createList(String.class, "invites");
	}
	
	public String getName() {
		return getIdentifier();
	}
	
	public void addMember(StandardPlayer player) {
		members.add(player.getName());
		
		this.save();
	}
	
	public void removeMember(StandardPlayer player) {
		members.remove(player.getName());
		
		this.save();
	}
	
	public boolean isMember(String username) {
		return members.contains(username);
	}
	
	public List<String> getMembers() {
		return members.getList();
	}
	
	public List<StandardPlayer> getPlayers() {
		ArrayList<StandardPlayer> list = new ArrayList<StandardPlayer>();
		for (String username : members) {
			StandardPlayer player = StandardPlugin.getPlugin().getStandardPlayer(username);
			list.add(player);
		}
		
		return list;
	}
	
	public boolean isInvited(String username) {
		return invites.contains(username);
	}
	
	public void invite(String username) {
		invites.add(username);
		
		this.save();
	}
	
	public void removeInvite(String username) {
		invites.remove(username);
		
		this.save();
	}

}
