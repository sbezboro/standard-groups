package com.sbezboro.standardgroups.model;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;

import com.sbezboro.standardplugin.model.StandardPlayer;
import com.sbezboro.standardplugin.persistence.persistables.Persistable;
import com.sbezboro.standardplugin.persistence.persistables.PersistableImpl;

public class Claim extends PersistableImpl implements Persistable {
	private int x;
	private int z;
	private String world;
	private String player;
	
	private Group group;

	public Claim() {
	}
	
	public Claim(StandardPlayer player, Location location, Group group) {
		this.x = location.getBlockX() >> 4;
		this.z = location.getBlockZ() >> 4;
		this.world = location.getWorld().getName();
		this.player = player.getName();
		this.group = group;
	}

	@Override
	public void loadFromPersistance(Map<String, Object> map) {
		x = (Integer) map.get("x");
		z = (Integer) map.get("z");
		world = (String) map.get("world");
		player = (String) map.get("player");
	}

	@Override
	public Map<String, Object> mapRepresentation() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("x", x);
		map.put("z", z);
		map.put("world", world);
		map.put("player", player);
		
		return map;
	}
	
	public String getLocationKey() {
		return world + ";" + x + ";" + z;
	}
	
	public static String getLocationKey(Location location) {
		return location.getWorld().getName() + ";" + (location.getBlockX() >> 4) + ";" + (location.getBlockZ() >> 4);
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

}
