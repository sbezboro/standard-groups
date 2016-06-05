package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;

public class BlockFadeListener extends SubPluginEventListener<StandardGroups> implements Listener {
	public BlockFadeListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	// Prevent ice blocks from melting on group land (could be used to flood a base)
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockFade(BlockFadeEvent event) {
		Location location = event.getBlock().getLocation();
		
		GroupManager groupManager = subPlugin.getGroupManager();
		
		Group group = groupManager.getGroupByLocation(location);
		
		if (group != null && event.getBlock().getType() == Material.ICE) {
			event.setCancelled(true);
		}
	}
}
