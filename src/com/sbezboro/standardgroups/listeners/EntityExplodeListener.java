package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;

public class EntityExplodeListener extends SubPluginEventListener<StandardGroups> implements Listener {

	public EntityExplodeListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityExplode(final EntityExplodeEvent event) {
		GroupManager groupManager = subPlugin.getGroupManager();

		for (Block block : new ArrayList<Block>(event.blockList())) {
			Group group = groupManager.getGroupByLocation(block.getLocation());

			if (group != null) {
				if (group.isSafeArea() || group.isNeutralArea()) {
					event.setCancelled(true);
					return;
				}
				
				if (!groupManager.getLocksAffectedByBlock(block.getLocation()).isEmpty()) {
					event.blockList().remove(block);
				}
			}
		}
	}

}
