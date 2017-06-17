package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import com.sbezboro.standardplugin.model.StandardPlayer;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;

public class VehicleEnterListener extends SubPluginEventListener<StandardGroups> implements Listener {

	public VehicleEnterListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(ignoreCancelled = true)
	public void onVehicleEnter(VehicleEnterEvent event) {
		if (event.getVehicle().getType() == EntityType.BOAT) {
			StandardPlayer player = plugin.getStandardPlayer(event.getEntered());
			
			if (player == null) {
				return;
			}
			
			GroupManager groupManager = subPlugin.getGroupManager();
			Location loc = event.getVehicle().getLocation();
			
			Block block = loc.getBlock();
			Group locationGroup = groupManager.getGroupByLocation(loc);
			
			if (locationGroup != null) {
				if (!locationGroup.isMember(player)
						&& !(block.getType() == Material.STATIONARY_WATER && block.getData() == 0)) {
					player.sendMessage(ChatColor.RED + "Cannot enter a boat here");
					event.setCancelled(true);
				}
			}
		}
	}
}
