package com.sbezboro.standardgroups.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sbezboro.standardplugin.util.MiscUtil;
import org.bukkit.Location;

import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.model.StandardPlayer;
import com.sbezboro.standardplugin.persistence.PersistedListProperty;
import com.sbezboro.standardplugin.persistence.PersistedObject;
import com.sbezboro.standardplugin.persistence.PersistedProperty;
import com.sbezboro.standardplugin.persistence.storages.FileStorage;

public class Group extends PersistedObject {
	private PersistedListProperty<String> members;
	private PersistedListProperty<String> invites;
	private PersistedListProperty<Claim> claims;
	private PersistedListProperty<Lock> locks;

	private PersistedProperty<Long> established;
	private PersistedProperty<Integer> maxClaims;
	private PersistedProperty<String> leader;

	private Map<String, Lock> locationToLockMap;

	public Group(FileStorage storage, String name) {
		super(storage, name);

		initialize();
	}
	
	public Group(FileStorage storage, String name, long established, StandardPlayer leader) {
		super(storage, name);

		this.established.setValue(established);
		this.maxClaims.setValue(10);
		this.leader.setValue(leader.getName());
		this.members.add(leader.getName());

		initialize();
	}

	public void initialize() {
		this.locationToLockMap = new HashMap<String, Lock>();
	}
	
	@Override
	public void loadProperties() {
		super.loadProperties();
		
		for (Claim claim : claims) {
			claim.setGroup(this);
		}

		for (Lock lock : locks) {
			lock.setGroup(this);

			locationToLockMap.put(MiscUtil.getLocationKey(lock.getLocation()), lock);
		}
	}

	@Override
	public void createProperties() {
		members = createList(String.class, "members");
		invites = createList(String.class, "invites");
		claims = createList(Claim.class, "claims");
		locks = createList(Lock.class, "locks");
		
		established = createProperty(Long.class, "established");
		maxClaims = createProperty(Integer.class, "max-claims");
		leader = createProperty(String.class, "leader");
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

	public boolean isLeader(StandardPlayer player) {
		return leader.getValue().equals(player.getName());
	}
	
	public boolean isMember(StandardPlayer player) {
		return members.contains(player.getName());
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

	public List<Lock>getLocks() {
		return locks.getList();
	}

	public Lock getLock(Location location) {
		return locationToLockMap.get(MiscUtil.getLocationKey(location));
	}

	public Lock lock(StandardPlayer player, Location location) {
		Lock lock = new Lock(player, location, this);
		locks.add(lock);

		locationToLockMap.put(MiscUtil.getLocationKey(location), lock);

		this.save();

		return lock;
	}

	public void unlock(Lock lock) {
		locks.remove(lock);

		locationToLockMap.remove(MiscUtil.getLocationKey(lock.getLocation()));

		this.save();
	}

	public void clearLocks() {
		this.locationToLockMap.clear();
		this.locks.clear();
	}
	
	public boolean isInvited(StandardPlayer player) {
		return invites.contains(player.getName());
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

	public void addLockMember(Lock lock, StandardPlayer otherPlayer) {
		lock.addMember(otherPlayer);

		this.save();
	}

	public void removeLockMember(Lock lock, StandardPlayer otherPlayer) {
		lock.removeMember(otherPlayer);

		this.save();
	}
}
