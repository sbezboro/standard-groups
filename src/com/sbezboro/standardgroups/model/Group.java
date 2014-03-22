package com.sbezboro.standardgroups.model;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.model.StandardPlayer;
import com.sbezboro.standardplugin.persistence.PersistedListProperty;
import com.sbezboro.standardplugin.persistence.PersistedObject;
import com.sbezboro.standardplugin.persistence.PersistedProperty;
import com.sbezboro.standardplugin.persistence.storages.FileStorage;

public class Group extends PersistedObject {
	public PersistedListProperty<String> members;
	public PersistedListProperty<String> invites;
	public PersistedListProperty<Claim> claims;
	
	public PersistedProperty<Long> established;
	public PersistedProperty<Integer> maxClaims;

	public Group(FileStorage storage, String name) {
		super(storage, name);
	}
	
	public Group(FileStorage storage, String name, long established) {
		super(storage, name);
		
		this.established.setValue(established);
		this.maxClaims.setValue(10);
	}
	
	@Override
	public void loadProperties() {
		super.loadProperties();
		
		for (Claim claim : claims) {
			claim.setGroup(this);
		}
	}

	@Override
	public void createProperties() {
		members = createList(String.class, "members");
		invites = createList(String.class, "invites");
		claims = createList(Claim.class, "claims");
		
		established = createProperty(Long.class, "established");
		maxClaims = createProperty(Integer.class, "max-claims");
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
	
	public List<Claim> getClaims() {
		return claims.getList();
	}

	public Claim claim(StandardPlayer player, Location location) {
		Claim claim = new Claim(player, location, this);
		claims.add(claim);
		
		this.save();
		
		return claim;
	}
	
	public void unclaim(Claim claim) {
		claims.remove(claim);
		
		this.save();
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
	
	public long getEstablished() {
		return established.getValue();
	}
	
	public void rename(String name) {
		setIdentifier(name);
	}

	public int getMaxClaims() {
		return maxClaims.getValue();
	}

	public void setMaxClaims(int maxClaims) {
		this.maxClaims.setValue(maxClaims);;
	}

}
