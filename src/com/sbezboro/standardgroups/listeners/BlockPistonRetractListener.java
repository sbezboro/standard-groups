package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardgroups.model.Lock;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.List;

public class BlockPistonRetractListener extends SubPluginEventListener<StandardGroups> implements Listener {
	public BlockPistonRetractListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockPistonRetract(BlockPistonRetractEvent event) {
		if (!event.isSticky()) {
			return;
		}

		GroupManager groupManager = subPlugin.getGroupManager();

		BlockFace direction = event.getDirection().getOppositeFace();

		Location fromLocation = event.getRetractLocation();
		Location toLocation = fromLocation.getBlock().getRelative(direction).getLocation();

		List<Lock> locks = groupManager.getLocksAffectedByBlock(fromLocation);

		if (!locks.isEmpty()) {
			event.setCancelled(true);
			return;
		}

		Group fromGroup = groupManager.getGroupByLocation(fromLocation);
		Group toGroup = groupManager.getGroupByLocation(toLocation);

		if (fromLocation.getBlock().getType() != Material.AIR && fromGroup != toGroup) {
			event.setCancelled(true);
		}
	}

}
