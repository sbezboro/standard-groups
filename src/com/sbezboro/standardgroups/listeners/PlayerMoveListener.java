package com.sbezboro.standardgroups.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import com.sbezboro.standardplugin.model.StandardPlayer;

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
				
				if (toGroup == null) {
					player.sendMessage(ChatColor.GREEN + "Entering unclaimed wilderness");
				} else {
					player.sendMessage(ChatColor.YELLOW + "Entering the territory of " + toGroup.getName());
				}
			}
		}
	}
}
