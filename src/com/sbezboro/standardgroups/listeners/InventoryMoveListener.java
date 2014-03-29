package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Lock;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.InventoryHolder;

public class InventoryMoveListener  extends SubPluginEventListener<StandardGroups> implements Listener {
	public InventoryMoveListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryMoveItem(InventoryMoveItemEvent event) {
		if (isProtected(event.getSource().getHolder()) ||
				isProtected(event.getDestination().getHolder())) {
			event.setCancelled(true);
		}
	}

	private boolean isProtected(InventoryHolder holder) {
		GroupManager groupManager = subPlugin.getGroupManager();

		if (holder instanceof DoubleChest) {
			holder = ((DoubleChest) holder).getLeftSide();
		}

		if (holder instanceof BlockState) {
			Block block = ((BlockState) holder).getBlock();
			if (GroupManager.isBlockTypeProtected(block)) {
				Lock lock = groupManager.getLockAffectedByBlock(block.getLocation());

				if (lock != null) {
					return true;
				}
			}
		}

		return false;
	}
}
