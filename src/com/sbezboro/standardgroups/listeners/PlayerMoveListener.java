package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.managers.MapManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import com.sbezboro.standardplugin.model.StandardPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener extends SubPluginEventListener<StandardGroups> implements Listener {

	public PlayerMoveListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		Location from = event.getFrom();
		Location to = event.getTo();

		if (from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ()) {
			GroupManager groupManager = subPlugin.getGroupManager();
			
			Group fromGroup = groupManager.getGroupByLocation(from);
			Group toGroup = groupManager.getGroupByLocation(to);
			
			if (fromGroup != toGroup) {
				StandardPlayer player = plugin.getStandardPlayer(event.getPlayer());

				Group playerGroup = groupManager.getPlayerGroup(player);
				
				if (toGroup == null) {
					player.sendMessage(String.valueOf(ChatColor.GREEN) + ChatColor.BOLD + "Entering unclaimed wilderness");
				} else if (toGroup.isSafeArea()) {
					player.sendMessage(String.valueOf(ChatColor.DARK_GREEN) + ChatColor.BOLD + "Entering the safe area");
					if (player.isInPvp()) {
						player.sendMessage(String.valueOf(ChatColor.RED) + ChatColor.BOLD + "You are still vulnerable to PVP");
					}
				} else if (toGroup.isNeutralArea()) {
					player.sendMessage(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD + "Entering the neutral area");
				} else if (toGroup == playerGroup) {
					player.sendMessage(String.valueOf(ChatColor.YELLOW) + ChatColor.BOLD + "Entering the territory of your group " + ChatColor.GREEN + ChatColor.BOLD + toGroup.getName());
				} else if (toGroup.isMutualFriendship(playerGroup)) {
					player.sendMessage(String.valueOf(ChatColor.YELLOW) + ChatColor.BOLD + "Entering the territory of " + ChatColor.DARK_AQUA + ChatColor.BOLD + toGroup.getName());
				} else {
					player.sendMessage(String.valueOf(ChatColor.YELLOW) + ChatColor.BOLD + "Entering the territory of " + toGroup.getName());
				}
			}

			// New chunk
			if (from.getBlockX() >> 4 != to.getBlockX() >> 4 || from.getBlockZ() >> 4 != to.getBlockZ() >> 4) {
				MapManager mapManager = subPlugin.getMapManager();

				StandardPlayer player = plugin.getStandardPlayer(event.getPlayer());

				mapManager.updateMap(player);
			}
		}
	}
}
