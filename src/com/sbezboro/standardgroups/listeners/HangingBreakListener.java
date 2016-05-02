package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import com.sbezboro.standardplugin.model.StandardPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;

public class HangingBreakListener extends SubPluginEventListener<StandardGroups> implements Listener {

	public HangingBreakListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(ignoreCancelled = true)
	public void onHangingBreak(HangingBreakByEntityEvent event) {
		StandardPlayer player = plugin.getStandardPlayer(event.getRemover());

		if (player == null) {
			return;
		}

		Location location = event.getEntity().getLocation();

		GroupManager groupManager = subPlugin.getGroupManager();

		Group victimGroup = groupManager.getGroupByLocation(location);
		Group attackerGroup = groupManager.getPlayerGroup(player);

		if (victimGroup != null) {
			if (!groupManager.playerInGroup(player, victimGroup) && !groupManager.isGroupsAdmin(player)) {
				if (victimGroup.getPower() >= GroupManager.ENTITY_POWER_THRESHOLD) {
					event.setCancelled(true);
					player.sendMessage(ChatColor.RED + "Cannot break blocks in the territory of " + victimGroup.getName());
					return;
				}
				if (attackerGroup == null) {
					event.setCancelled(true);
					player.sendMessage(ChatColor.RED + "Cannot break blocks in the territory of " + victimGroup.getName());
					return;
				}
				String attackerGroupUid = attackerGroup.getUid();
				
				double powerRestoration = 0.25;
				if (victimGroup.getPvpPowerLoss(attackerGroupUid) < powerRestoration) {
					event.setCancelled(true);
					player.sendMessage(ChatColor.GOLD + "Cannot yet break this type of block in the territory of " + victimGroup.getName());
					return;
				}
				victimGroup.addPower(powerRestoration);
				victimGroup.reducePvpPowerLoss(attackerGroupUid, powerRestoration);
			}
		}
	}
}
