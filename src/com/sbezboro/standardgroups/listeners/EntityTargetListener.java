package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

public class EntityTargetListener extends SubPluginEventListener<StandardGroups> implements Listener {

	public EntityTargetListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityTarget(EntityTargetEvent event) {
		Entity target = event.getTarget();

		if (target == null) {
			return;
		}

		Location location = target.getLocation();

		GroupManager groupManager = subPlugin.getGroupManager();

		Group group = groupManager.getGroupByLocation(location);

		if (group != null && group.isSafeArea()) {
			event.setCancelled(true);
		}
	}

}
