package com.sbezboro.standardgroups.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import com.sbezboro.standardplugin.model.StandardPlayer;

public class BlockPlaceEvent extends SubPluginEventListener<StandardGroups> implements Listener {

	public BlockPlaceEvent(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler
	public void onBlockPlace(final org.bukkit.event.block.BlockPlaceEvent event) {
		StandardPlayer player = plugin.getStandardPlayer(event.getPlayer());
		
		Location location = event.getBlock().getLocation();
		
		GroupManager groupManager = subPlugin.getGroupManager();
		
		Group group = groupManager.getGroupByLocation(location);
		
		if (group != null) {
			if (!groupManager.playerInGroup(player, group)) {
				event.setCancelled(true);
				player.sendMessage(ChatColor.RED + "Cannot place blocks in the territory of " + group.getName());
			}
		}
	}

}
