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
import org.bukkit.event.hanging.HangingPlaceEvent;

public class HangingPlaceListener extends SubPluginEventListener<StandardGroups> implements Listener {

	public HangingPlaceListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(ignoreCancelled = true)
	public void onHangingBreak(HangingPlaceEvent event) {
		StandardPlayer player = plugin.getStandardPlayer(event.getPlayer());

		Location location = event.getEntity().getLocation();

		GroupManager groupManager = subPlugin.getGroupManager();

		Group group = groupManager.getGroupByLocation(location);

		if (group != null) {
			if (!groupManager.playerInGroup(player, group) && !groupManager.isGroupsAdmin(player)) {
				if (player.hasTitle("Alt")) {
					player.sendMessage(ChatColor.RED + "Cannot place blocks in the territory of " + group.getName());
					event.setCancelled(true);
					return;
				}
				if (group.getPower() >= groupManager.ENTITY_POWER_THRESHOLD) {
					event.setCancelled(true);
					player.sendMessage(ChatColor.RED + "Cannot place blocks in the territory of " + group.getName());
				}
			}
		}
	}
}
