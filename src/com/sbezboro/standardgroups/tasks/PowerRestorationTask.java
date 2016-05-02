package com.sbezboro.standardgroups.tasks;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.tasks.BaseTask;

public class PowerRestorationTask extends BaseTask {
	private StandardGroups subPlugin;

	// Takes care of naturally regenerating all groups' power. Also purges old records of power losses through PVP
	
	public PowerRestorationTask(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin);

		this.subPlugin = subPlugin;
	}

	@Override
	public void run() {
		GroupManager groupManager = subPlugin.getGroupManager();

		for (Group group : groupManager.getGroups()) {
			if (!group.hasPvpPowerLoss()) {
				double restoreAmount = subPlugin.getGroupPowerGrowth();
				double numMembers = group.getPlayerCount();
				double onlineRatio = (double)(group.getOnlineCount()) / numMembers;
				double onlineModifier = ((1.0 - (1.0-onlineRatio)*(1.0-onlineRatio)) + onlineRatio) / 2.0;
				double memberModifier = 3.0 - 6.0/(numMembers+2.0);
				group.addPower(restoreAmount * memberModifier * onlineModifier);
			}
			
			group.purgePvpPowerLosses();
		}
	}

}
