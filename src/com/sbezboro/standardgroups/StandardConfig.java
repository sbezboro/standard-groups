package com.sbezboro.standardgroups;

import org.bukkit.configuration.Configuration;

import com.sbezboro.standardplugin.StandardPlugin;

public class StandardConfig {
	private StandardPlugin plugin;

	private boolean debug = false;
	
	private int groupNameMinLength;
	private int groupNameMaxLength;
	private int groupLandGrowth;
	private int groupLandGrowthDays;
	private int groupLandGrowthLimit;
	private int groupStartingLand;
	private int groupAutoKickDays;
	private int maxLocksPerChunk;
	private double groupPowerGrowth;
	private double groupPowerMinValue;
	private double groupPowerMaxValue;
	private double powerDamageModifier;
	private int spawnClaimCost;
	private boolean disableTNTCartsInClaimedLand;
  
	public StandardConfig(StandardPlugin plugin) {
		this.plugin = plugin;
	}

	public void reload() {
		Configuration config = plugin.getConfig();
		
		groupNameMinLength = config.getInt("group-name-min-length");
		groupNameMaxLength = config.getInt("group-name-max-length");
		groupLandGrowth = config.getInt("group-land-growth");
		groupLandGrowthDays = config.getInt("group-land-growth-days");
		groupLandGrowthLimit = config.getInt("group-land-growth-limit");
		groupStartingLand = config.getInt("group-starting-land");
		groupAutoKickDays = config.getInt("group-auto-kick-days");
		maxLocksPerChunk = config.getInt("max-locks-per-chunk");
		groupPowerGrowth = config.getDouble("group-power-growth");
		groupPowerMinValue = config.getDouble("group-power-min-value");
		groupPowerMaxValue = config.getDouble("group-power-max-value");
		powerDamageModifier = config.getDouble("power-damage-modifier");
		spawnClaimCost = config.getInt("spawn-claim-cost");
		disableTNTCartsInClaimedLand = config.getBoolean("disable-tnt-carts-in-claimed-land");
	}

	public boolean isDebug() {
		return debug;
	}

	public int getGroupNameMinLength() {
		return groupNameMinLength;
	}

	public int getGroupNameMaxLength() {
		return groupNameMaxLength;
	}

	public int getGroupLandGrowth() {
		return groupLandGrowth;
	}

	public int getGroupLandGrowthDays() {
		return groupLandGrowthDays;
	}

	public int getGroupLandGrowthLimit() {
		return groupLandGrowthLimit;
	}

	public int getGroupStartingLand() {
		return groupStartingLand;
	}

	public int getGroupAutoKickDays() {
		return groupAutoKickDays;
	}

	public int getMaxLocksPerChunk() {
		return maxLocksPerChunk;
	}
	
	public double getGroupPowerGrowth() {
		return groupPowerGrowth;
	}
	
	public double getGroupPowerMinValue() {
		return groupPowerMinValue;
	}
	
	public double getGroupPowerMaxValue() {
		return groupPowerMaxValue;
	}
	
	public double getPowerDamageModifier() {
		return powerDamageModifier;
	}

	public int getSpawnClaimCost() {
		return spawnClaimCost;
	}

	public boolean disableTNTCartsInClaimedLand() { return disableTNTCartsInClaimedLand; }

}
