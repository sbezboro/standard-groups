package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Lock;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import com.sbezboro.standardplugin.model.StandardPlayer;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;

public class InventoryMoveListener  extends SubPluginEventListener<StandardGroups> implements Listener {
	public InventoryMoveListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryMoveItem(InventoryMoveItemEvent event) {
		Lock sourceLock = getLock(event.getSource().getHolder());
		Lock destinationLock = getLock(event.getDestination().getHolder());

		if (sourceLock != null && destinationLock != null) {
			String sourceOwner = sourceLock.getOwnerName();
			String destinationOwner = destinationLock.getOwnerName();

			if (!sourceOwner.equalsIgnoreCase(destinationOwner)) {
				event.setCancelled(true);
			}
		} else if (sourceLock != null || destinationLock != null) {
			event.setCancelled(true);
		}
	}

	private Lock getLock(InventoryHolder holder) {
		GroupManager groupManager = subPlugin.getGroupManager();

		if (holder instanceof DoubleChest) {
			holder = ((DoubleChest) holder).getLeftSide();
		}

		if (holder instanceof BlockState) {
			Block block = ((BlockState) holder).getBlock();
			if (GroupManager.isBlockTypeProtected(block)) {
				List<Lock> locks = groupManager.getLocksAffectedByBlock(block.getLocation());
				if (locks.isEmpty()) {
					return null;
				}

				return locks.get(0);
			}
		}

		return null;
	}
}
