package com.sbezboro.standardgroups;

import org.bukkit.configuration.Configuration;

import com.sbezboro.standardplugin.StandardPlugin;

public class StandardConfig {
	private StandardPlugin plugin;

	private boolean debug = false;
	
	private int groupNameMinLength;
	private int groupNameMaxLength;

	public StandardConfig(StandardPlugin plugin) {
		this.plugin = plugin;
	}

	public void reload() {
		Configuration config = plugin.getConfig();
		
		groupNameMinLength = config.getInt("group-name-min-length");
		groupNameMaxLength = config.getInt("group-name-max-length");
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

}