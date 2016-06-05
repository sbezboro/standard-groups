package com.sbezboro.standardgroups.tasks;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.tasks.BaseTask;

public class PurgeCooldownsTask extends BaseTask {
	private StandardGroups subPlugin;

	// Removes old entries tracking the usage of anti-macro protected commands
	
	public PurgeCooldownsTask(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin);

		this.subPlugin = subPlugin;
	}

	@Override
	public void run() {
		GroupManager groupManager = subPlugin.getGroupManager();

		groupManager.purgePlayerCommandCooldowns();
	}

}
