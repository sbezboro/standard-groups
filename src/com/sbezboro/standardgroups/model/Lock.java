package com.sbezboro.standardgroups.model;

import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.model.StandardPlayer;
import com.sbezboro.standardplugin.persistence.persistables.Persistable;
import com.sbezboro.standardplugin.persistence.persistables.PersistableImpl;
import com.sbezboro.standardplugin.persistence.persistables.PersistableLocation;
import com.sbezboro.standardplugin.util.MiscUtil;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lock extends PersistableImpl implements Persistable {
	private PersistableLocation location;
	private String owner;
	private List<String> members;
	private LockType type;
	private boolean publicLock;

	private Group group;

	public static enum LockType {
		INDIVIDUAL, TRUSTED;
	}

	public Lock() {
	}

	public Lock(StandardPlayer player, Location location, Group group) {
		this.location = new PersistableLocation(location);
		this.group = group;

		this.owner = player.getName();
		this.members = new ArrayList<String>();
		this.members.add(player.getName());

		this.type = LockType.INDIVIDUAL;
		this.publicLock = false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void loadFromPersistance(Map<String, Object> map) {
		location = new PersistableLocation();
		location.loadFromPersistance((Map<String, Object>) map.get("location"));

		owner = (String) map.get("owner");
		members = (List<String>) map.get("members");
		type = LockType.valueOf((String) map.get("type"));
		publicLock = MiscUtil.safeBoolean(map.get("public"));
	}

	@Override
	public Map<String, Object> mapRepresentation() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("location", location.mapRepresentation());
		map.put("owner", owner);
		map.put("members", members);
		map.put("type", type.toString());
		map.put("public", publicLock);
		
		return map;
	}

	public Location getLocation() {
		return location.getLocation();
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public List<StandardPlayer> getMembers() {
		ArrayList<StandardPlayer> list = new ArrayList<StandardPlayer>();
		for (String username : members) {
			StandardPlayer player = StandardPlugin.getPlugin().getStandardPlayer(username);
			list.add(player);
		}

		return list;
	}

	public String getOwnerName() {
		return owner;
	}

	public StandardPlayer getOwner() {
		return StandardPlugin.getPlugin().getStandardPlayer(owner);
	}

	public boolean isOwner(StandardPlayer player) {
		return owner.equalsIgnoreCase(player.getName());
	}

	public boolean hasAccess(StandardPlayer player) {
		return members.contains(player.getName());
	}

	public void addMember(StandardPlayer player) {
		members.add(player.getName());
	}

	public void removeMember(StandardPlayer otherPlayer) {
		members.remove(otherPlayer.getName());
	}

	public boolean isPublic() {
		return publicLock;
	}

	public void setPublic(boolean publicLock) {
		this.publicLock = publicLock;
	}

}
