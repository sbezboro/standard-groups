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
		
		float restoreAmount = StandardGroups.getPlugin().getGroupPowerGrowth();

		for (Group group : groupManager.getGroups()) {
			float numMembers = group.getPlayerCount();
			float numNonAltMembers = group.getNonAltPlayerCount();
			float onlineModifier = (float)(group.getNonAltOnlineCount()) / numMembers;
			float memberModifier = 3.0f - 6.0f/(numNonAltMembers+2.0f);
			float altPenalty = 1.333333f * Math.max((numMembers-numNonAltMembers)/numMembers - 0.25f, 0.0f);
			group.addPower(restoreAmount * (memberModifier * onlineModifier - altPenalty));
		}
	}

}
