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
import org.bukkit.event.block.EntityBlockFormEvent;

public class BlockFormListener extends SubPluginEventListener<StandardGroups> implements Listener {
	public BlockFormListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	// Prevent the frost walker enchantment from working on claimed land (could quite easily put out water)
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockForm(EntityBlockFormEvent event) {
		Location location = event.getBlock().getLocation();
		
		GroupManager groupManager = subPlugin.getGroupManager();
		
		Group group = groupManager.getGroupByLocation(location);
		
		if (group != null && event.getNewState().getType() == Material.FROSTED_ICE) {
			event.setCancelled(true);
		}
	}
}
