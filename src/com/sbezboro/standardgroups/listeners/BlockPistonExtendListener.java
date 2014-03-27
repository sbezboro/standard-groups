package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardgroups.model.Lock;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockPistonExtendListener extends SubPluginEventListener<StandardGroups> implements Listener {
	public BlockPistonExtendListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
		GroupManager groupManager = subPlugin.getGroupManager();

		for (Block block : event.getBlocks()) {
			Group group = groupManager.getGroupByLocation(block.getLocation());

			Lock lock = groupManager.getLockAffectedByBlock(group, block.getLocation());

			if (lock != null) {
				event.setCancelled(true);
			}
		}
	}
}
