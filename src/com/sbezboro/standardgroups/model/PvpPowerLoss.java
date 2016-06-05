package com.sbezboro.standardgroups.model;

import java.util.HashMap;
import java.util.Map;

import com.sbezboro.standardplugin.persistence.persistables.Persistable;
import com.sbezboro.standardplugin.persistence.persistables.PersistableImpl;

public class PvpPowerLoss extends PersistableImpl implements Persistable {
	
	// Used for keeping track of what power damage a specific group has caused to their victim within the last hour
	// The raiders may then proceed to break blocks worth the same amount of power
	
	private String groupUid; // The victorious group
	private double powerLoss; // Power damage caused by the kill
	private long time; // Timestamp
	
	// Needed for persistance
	public PvpPowerLoss() {
	}
	
	public PvpPowerLoss(String groupUid, double powerLoss, long time) {
		this.groupUid = groupUid;
		this.powerLoss = powerLoss;
		this.time = time;
	}
	
	@Override
	public void loadFromPersistance(Map<String, Object> map) {
		groupUid = (String) map.get("group-uid");
		powerLoss = (Double) map.get("power-loss");
		time = (Long) map.get("time");
	}
	
	@Override
	public Map<String, Object> mapRepresentation() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("group-uid", groupUid);
		map.put("power-loss", powerLoss);
		map.put("time", time);
		
		return map;
	}
	
	public String getGroupUid() {
		return groupUid;
	}
	
	public double getPowerLoss() {
		return powerLoss;
	}
	
	public long getTime() {
		return time;
	}
	
	public void setPowerLoss(double powerLoss) {
		this.powerLoss = powerLoss;
	}
}
