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
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;

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

		Group group = groupManager.getGroupByLocation(location);

		if (group != null) {
			if (!groupManager.playerInGroup(player, group) && !groupManager.isGroupsAdmin(player)) {
				event.setCancelled(true);
				player.sendMessage(ChatColor.RED + "Cannot break blocks in the territory of " + group.getName());
			}
		}
	}
}
