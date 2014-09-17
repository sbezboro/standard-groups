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
	private String ownerUuid;
	private List<String> memberUuids;
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

		this.ownerUuid = player.getUuidString();
		this.memberUuids = new ArrayList<String>();
		this.memberUuids.add(player.getUuidString());

		this.type = LockType.INDIVIDUAL;
		this.publicLock = false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void loadFromPersistance(Map<String, Object> map) {
		location = new PersistableLocation();
		location.loadFromPersistance((Map<String, Object>) map.get("location"));

		ownerUuid = (String) map.get("owner-uuid");
		memberUuids = (List<String>) map.get("member-uuids");
		type = LockType.valueOf((String) map.get("type"));
		publicLock = MiscUtil.safeBoolean(map.get("public"));

		if (memberUuids == null) {
			memberUuids = new ArrayList<String>();
		}
	}

	@Override
	public Map<String, Object> mapRepresentation() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("location", location.mapRepresentation());
		map.put("owner-uuid", ownerUuid);
		map.put("member-uuids", memberUuids);
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
		for (String uuid : memberUuids) {
			StandardPlayer player = StandardPlugin.getPlugin().getStandardPlayerByUUID(uuid);
			list.add(player);
		}

		return list;
	}

	public String getOwnerUuid() {
		return ownerUuid;
	}

	public StandardPlayer getOwner() {
		return StandardPlugin.getPlugin().getStandardPlayerByUUID(ownerUuid);
	}

	public boolean isOwner(StandardPlayer player) {
		return ownerUuid.equals(player.getUuidString());
	}

	public boolean hasAccess(StandardPlayer player) {
		return memberUuids.contains(player.getUuidString());
	}

	public void addMember(StandardPlayer player) {
		memberUuids.add(player.getUuidString());
	}

	public void removeMember(StandardPlayer otherPlayer) {
		memberUuids.remove(otherPlayer.getUuidString());
	}

	public boolean isPublic() {
		return publicLock;
	}

	public void setPublic(boolean publicLock) {
		this.publicLock = publicLock;
	}

}
