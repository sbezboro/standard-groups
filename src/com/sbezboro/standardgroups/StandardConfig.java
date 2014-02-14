package com.sbezboro.standardgroups;

import org.bukkit.configuration.Configuration;

public class StandardConfig {
	private StandardGroups plugin;

	private boolean debug = false;

	public StandardConfig(StandardGroups plugin) {
		this.plugin = plugin;
	}

	public void reload() {
		Configuration config = plugin.getConfig();
		
		debug = config.getBoolean("debug");
		if (debug) {
			plugin.getLogger().info("Debug mode enabled!");
		}
	}

	public boolean isDebug() {
		return debug;
	}

}