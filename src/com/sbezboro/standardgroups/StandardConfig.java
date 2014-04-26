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
	private int groupAutoKickDays;

	public StandardConfig(StandardPlugin plugin) {
		this.plugin = plugin;
	}

	public void reload() {
		Configuration config = plugin.getConfig();
		
		groupNameMinLength = config.getInt("group-name-min-length");
		groupNameMaxLength = config.getInt("group-name-max-length");
		groupLandGrowth = config.getInt("group-land-growth");
		groupLandGrowthDays = config.getInt("group-land-growth-days");
		groupAutoKickDays = config.getInt("group-auto-kick-days");
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

	public int getGroupAutoKickDays() {
		return groupAutoKickDays;
	}
}