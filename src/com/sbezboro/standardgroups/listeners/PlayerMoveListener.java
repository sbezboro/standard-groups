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
					player.sendMessage(ChatColor.GREEN + "Entering unclaimed wilderness");
				} else if (toGroup.isSafearea()) {
					player.sendMessage(ChatColor.DARK_GREEN + "Entering the safearea");
				} else if (toGroup == playerGroup) {
					player.sendMessage(ChatColor.YELLOW + "Entering the territory of your group " + ChatColor.GREEN + toGroup.getName());
				} else {
					player.sendMessage(ChatColor.YELLOW + "Entering the territory of " + toGroup.getName());
				}
			}
		}
	}
}
