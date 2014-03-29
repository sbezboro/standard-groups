package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardgroups.model.Lock;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonRetractEvent;

public class BlockPistonRetractListener extends SubPluginEventListener<StandardGroups> implements Listener {
	public BlockPistonRetractListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockPistonRetract(BlockPistonRetractEvent event) {
		GroupManager groupManager = subPlugin.getGroupManager();

		BlockFace direction = event.getDirection().getOppositeFace();

		Location toLocation = event.getRetractLocation();

		Block block = toLocation.getBlock().getRelative(direction);
		Location fromLocation = block.getLocation();

		Lock lock = groupManager.getLockAffectedByBlock(fromLocation);

		if (lock != null) {
			event.setCancelled(true);
		}

		Group fromGroup = groupManager.getGroupByLocation(fromLocation);
		Group toGroup = groupManager.getGroupByLocation(toLocation);

		if (block.getType() != Material.AIR && fromGroup != toGroup) {
			event.setCancelled(true);
		}
	}

}
