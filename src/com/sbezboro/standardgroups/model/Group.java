package com.sbezboro.standardgroups.model;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.model.StandardPlayer;
import com.sbezboro.standardplugin.persistence.PersistedListProperty;
import com.sbezboro.standardplugin.persistence.PersistedObject;
import com.sbezboro.standardplugin.persistence.PersistedProperty;
import com.sbezboro.standardplugin.persistence.storages.FileStorage;
import com.sbezboro.standardplugin.util.MiscUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.*;

public class Group extends PersistedObject implements Comparable<Group> {
	public static final String SAFE_AREA = "safearea";

	private PersistedListProperty<String> memberUuids;
	private PersistedListProperty<String> moderatorUuids;
	private PersistedListProperty<String> invites;
	private PersistedListProperty<Claim> claims;
	private PersistedListProperty<Lock> locks;
	private PersistedListProperty<String> chatPlayerUuids;

	private PersistedProperty<String> uid;
	private PersistedProperty<Long> established;
	private PersistedProperty<Long> lastGrowth;
	private PersistedProperty<Integer> maxClaims;
	private PersistedProperty<String> leaderUuid;

	private Map<String, Claim> locationToClaimMap;
	private Map<String, Lock> locationToLockMap;

	public Group(FileStorage storage, String name) {
		super(storage, name);

		initialize();
	}
	
	public Group(FileStorage storage, String name, long established, StandardPlayer leader) {
		super(storage, name);

		this.established.setValue(established);
		this.maxClaims.setValue(10);
		this.leaderUuid.setValue(leader.getUuidString());
		this.memberUuids.add(leader.getUuidString());

		initialize();
	}

	public void initialize() {
		this.locationToLockMap = new HashMap<String, Lock>();
		this.locationToClaimMap = new HashMap<String, Claim>();
	}

	@Override
	public void createProperties() {
		memberUuids = createList(String.class, "member-uuids");
		moderatorUuids = createList(String.class, "moderator-uuids");
		invites = createList(String.class, "invites");
		claims = createList(Claim.class, "claims");
		locks = createList(Lock.class, "locks");
		chatPlayerUuids = createList(String.class, "chat-player-uuids");

		uid = createProperty(String.class, "uid");
		established = createProperty(Long.class, "established");
		lastGrowth = createProperty(Long.class, "last-growth");
		maxClaims = createProperty(Integer.class, "max-claims");
		leaderUuid = createProperty(String.class, "leader-uuid");
	}

	@Override
	public void loadProperties() {
		super.loadProperties();

		initialize();

		ArrayList<Claim> claimsToRemove = new ArrayList<Claim>();

		for (Claim claim : claims) {
			if (locationToClaimMap.containsKey(claim.getLocationKey())) {
				StandardGroups.getPlugin().getLogger().severe("Duplicate claim for " + getName() + " - " + claim.getX() + ", " + claim.getZ());
				claimsToRemove.add(claim);
			} else {
				claim.setGroup(this);

				locationToClaimMap.put(claim.getLocationKey(), claim);
			}
		}

		for (Claim claim : claimsToRemove) {
			claims.remove(claim);
		}

		if (!claimsToRemove.isEmpty()) {
			this.save();
		}

		for (Lock lock : locks) {
			lock.setGroup(this);

			locationToLockMap.put(MiscUtil.getLocationKey(lock.getLocation()), lock);
		}

		if (uid.getValue() == null || uid.getValue().length() == 0) {
			uid.setValue(UUID.randomUUID().toString().replaceAll("-", ""));
		}

		try {
			if (lastGrowth.getValue() == 0) {
				lastGrowth.setValue(established.getValue());
			}
		} catch (ClassCastException e) {
			// ignore
		}
	}

	public String getUid() {
		return uid.getValue();
	}

	public String getName() {
		return getIdentifier();
	}

	public String getNameWithRelation(StandardPlayer player) {
		return (player != null && isMember(player) ? ChatColor.GREEN : ChatColor.YELLOW) + getIdentifier();
	}

	public boolean isSafearea() {
		return getName().equals(Group.SAFE_AREA);
	}
	
	public void addMember(StandardPlayer player) {
		memberUuids.add(player.getUuidString());
		
		this.save();
	}
	
	public void removeMember(StandardPlayer player) {
		memberUuids.remove(player.getUuidString());

		if (isModerator(player)) {
			moderatorUuids.remove(player.getUuidString());
		}
		
		this.save();
	}

	public boolean isLeader(StandardPlayer player) {
		return getLeaderUuid().equals(player.getUuidString());
	}

	public boolean isModerator(StandardPlayer player) {
		return moderatorUuids.contains(player.getUuidString());
	}
	
	public boolean isMember(StandardPlayer player) {
		return memberUuids.contains(player.getUuidString());
	}

	public String getLeaderUuid() {
		return leaderUuid.getValue();
	}
	
	public List<String> getMemberUuids() {
		return memberUuids.getList();
	}

	public List<String> getModeratorUuids() {
		return moderatorUuids.getList();
	}

	public int getOnlineCount() {
		int online = 0;

		for (StandardPlayer player : getPlayers()) {
			if (player.isOnline()) {
				online++;
			}
		}

		return online;
	}
	
	public List<StandardPlayer> getPlayers() {
		ArrayList<StandardPlayer> list = new ArrayList<StandardPlayer>();

		for (String uuid : memberUuids) {
			StandardPlayer player = StandardPlugin.getPlugin().getStandardPlayerByUUID(uuid);
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

		locationToClaimMap.put(claim.getLocationKey(), claim);
		
		this.save();
		
		return claim;
	}
	
	public void unclaim(Claim claim) {
		for (Lock lock : new ArrayList<Lock>(getLocks())) {
			if (locationToClaimMap.get(Claim.getLocationKey(lock.getLocation())) == claim) {
				locks.remove(lock);

				locationToLockMap.remove(MiscUtil.getLocationKey(lock.getLocation()));
			}
		}

		claims.remove(claim);

		locationToClaimMap.remove(claim.getLocationKey());
		
		this.save();
	}

	public Claim getClaim(Location location) {
		return locationToClaimMap.get(Claim.getLocationKey(location));
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

	public void addModerator(StandardPlayer player) {
		moderatorUuids.add(player.getUuidString());

		this.save();
	}

	public void removeModerator(StandardPlayer player) {
		moderatorUuids.remove(player.getUuidString());

		this.save();
	}

	public void setLeader(StandardPlayer player) {
		leaderUuid.setValue(player.getUuidString());

		this.save();
	}

	public long getEstablished() {
		return established.getValue();
	}

	public long getNextGrowth() {
		return getLastGrowth() + StandardGroups.getPlugin().getGroupLandGrowthDays() * 86400000;
	}

	public void setLastGrowth(long time) {
		lastGrowth.setValue(time);
	}

	public long getLastGrowth() {
		return lastGrowth.getValue();
	}

	public void grow() {
		maxClaims.setValue(maxClaims.getValue() + 2);
	}

	public void rename(String name) {
		setIdentifier(name);
	}

	public int getMaxClaims() {
		if (isSafearea()) {
			return 999999;
		}
		return maxClaims.getValue();
	}

	public void setMaxClaims(int maxClaims) {
		this.maxClaims.setValue(maxClaims);
	}

	public void addLockMember(Lock lock, StandardPlayer otherPlayer) {
		lock.addMember(otherPlayer);

		this.save();
	}

	public void removeLockMember(Lock lock, StandardPlayer otherPlayer) {
		lock.removeMember(otherPlayer);

		this.save();
	}

	public boolean togglePublicLock(Lock lock) {
		if (lock.isPublic()) {
			lock.setPublic(false);
		} else {
			lock.setPublic(true);
		}

		this.save();

		return lock.isPublic();
	}

	public void sendGroupMessage(String message) {
		sendGroupMessage(message, null);
	}

	public void sendGroupMessage(String message, StandardPlayer from) {
		for (StandardPlayer member : getPlayers()) {
			if (from != member && member.isOnline()) {
				member.sendMessage(message);
			}
		}
	}

	public boolean isGroupChat(StandardPlayer player) {
		return chatPlayerUuids.contains(player.getUuidString());
	}

	public boolean toggleChat(StandardPlayer player) {
		boolean result;

		if (isGroupChat(player)) {
			chatPlayerUuids.remove(player.getUuidString());
			result = false;
		} else {
			chatPlayerUuids.add(player.getUuidString());
			result = true;
		}

		this.save();

		return result;
	}

	@Override
	public int compareTo(Group other) {
		if (getMemberUuids().size() == other.getMemberUuids().size()) {
			return getName().compareTo(other.getName());
		} else {
			return other.getMemberUuids().size() - getMemberUuids().size();
		}
	}

	public Map<String, Object> getInfo() {
		HashMap<String, Object> info = new HashMap<String, Object>();

		info.put("uid", getUid());
		info.put("name", getName());
		info.put("established", getEstablished());
		info.put("land_count", getClaims().size());
		info.put("land_limit", getMaxClaims());
		info.put("lock_count", getLocks().size());

		info.put("invites", invites.getList());

		info.put("leader_uuid", leaderUuid.getValue());
		info.put("moderator_uuids", getModeratorUuids());
		info.put("member_uuids", getMemberUuids());

		return info;
	}

}
