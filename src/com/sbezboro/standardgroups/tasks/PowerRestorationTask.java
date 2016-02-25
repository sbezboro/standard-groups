package com.sbezboro.standardgroups.tasks;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.tasks.BaseTask;
import org.bukkit.ChatColor;

public class PowerRestorationTask extends BaseTask {
	private StandardGroups subPlugin;

	public PowerRestorationTask(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin);

		this.subPlugin = subPlugin;
	}

	@Override
	public void run() {
		GroupManager groupManager = subPlugin.getGroupManager();
		
		double restoreAmount = StandardGroups.getPlugin().getGroupPowerGrowth();

		for (Group group : groupManager.getGroups()) {
			double numMembers = group.getPlayerCount();
			double numNonAltMembers = group.getNonAltPlayerCount();
			double onlineModifier = (double)(group.getNonAltOnlineCount()) / numMembers;
			double memberModifier = 3.0 - 6.0/(numNonAltMembers+2.0);
			double altPenalty = 1.333333 * Math.max((numMembers-numNonAltMembers)/numMembers - 0.25, 0.0);
			group.addPower(restoreAmount * (memberModifier * onlineModifier - altPenalty));
		}
	}

}
