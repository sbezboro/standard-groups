package com.sbezboro.standardgroups.model;

import java.util.HashMap;
import java.util.Map;

import com.sbezboro.standardplugin.StandardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.sbezboro.standardplugin.model.StandardPlayer;
import com.sbezboro.standardplugin.persistence.persistables.Persistable;
import com.sbezboro.standardplugin.persistence.persistables.PersistableImpl;
import org.bukkit.Server;
import org.bukkit.World;

public class Claim extends PersistableImpl implements Persistable {
	private int x;
	private int z;
	private String worldName;
	private String playerUuid;
	
	private Group group;
	private World world;

	public Claim() {
	}
	
	public Claim(StandardPlayer player, Location location, Group group) {
		this.x = location.getBlockX() >> 4;
		this.z = location.getBlockZ() >> 4;
		this.world = location.getWorld();
		this.playerUuid = player.getUuidString();
		this.group = group;

		this.worldName = this.world.getName();
	}

	@Override
	public void loadFromPersistance(Map<String, Object> map) {
		x = (Integer) map.get("x");
		z = (Integer) map.get("z");
		worldName = (String) map.get("world");
		playerUuid = (String) map.get("player-uuid");

		world = Bukkit.getWorld(worldName);
	}

	@Override
	public Map<String, Object> mapRepresentation() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("x", x);
		map.put("z", z);
		map.put("world", worldName);
		map.put("player-uuid", playerUuid);
		
		return map;
	}
	
	public String getLocationKey() {
		return worldName + ";" + x + ";" + z;
	}
	
	public static String getLocationKey(Location location) {
		return location.getWorld().getName() + ";" + (location.getBlockX() >> 4) + ";" + (location.getBlockZ() >> 4);
	}

	public World getWorld() {
		return world;
	}

	public String getWorldDisplayName() {
		switch (world.getEnvironment()) {
			case NORMAL:
				return "Overworld";
			case NETHER:
				return "Nether";
			case THE_END:
				return "End";
		}

		return worldName;
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
