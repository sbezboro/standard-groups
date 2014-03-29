package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardgroups.model.Lock;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import com.sbezboro.standardplugin.model.StandardPlayer;
import com.sbezboro.standardplugin.util.MiscUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;
import java.util.List;

public class EntityExplodeListener extends SubPluginEventListener<StandardGroups> implements Listener {

	public EntityExplodeListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityExplode(final EntityExplodeEvent event) {
		GroupManager groupManager = subPlugin.getGroupManager();

		for (Block block : new ArrayList<Block>(event.blockList())) {
			Group group = groupManager.getGroupByLocation(block.getLocation());

			if (group != null && GroupManager.isBlockTypeProtected(block)) {
				event.blockList().remove(block);
			}
		}
	}

}
