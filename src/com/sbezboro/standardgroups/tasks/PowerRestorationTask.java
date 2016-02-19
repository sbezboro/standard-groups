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
		// subPlugin.getLogger().info("Running PowerRestorationTask");
		// -> Log spam? Once per minute

		GroupManager groupManager = subPlugin.getGroupManager();
		
		float restoreAmount = StandardGroups.getPlugin().getGroupPowerGrowth();

		for (Group group : groupManager.getGroups()) {
			float numMembers = group.getPlayerCount();
			float numNonAltMembers = group.getNonAltPlayerCount();
			float onlineModifier = (float)(group.getNonAltOnlineCount()) / numMembers; // sic: Groups with alts cannot get full regen
			float memberModifier = 3.0f - 6.0f/(numNonAltMembers+2.0f); // 0 -> 0, 1 -> 1, 2 -> 1.5, 3 -> 1.8, 5 -> 2.14, 10 -> 2.5, inf -> 3
			float altPenalty = Math.max((numMembers-numNonAltMembers)/numMembers - 0.25f, 0.0f);
			group.addPower(restoreAmount * (memberModifier * onlineModifier - altPenalty));
		}
	}

}
