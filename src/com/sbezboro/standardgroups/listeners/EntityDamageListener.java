package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import com.sbezboro.standardplugin.model.StandardPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public class EntityDamageListener extends SubPluginEventListener<StandardGroups> implements Listener {

	public EntityDamageListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		StandardPlayer player = plugin.getStandardPlayer(event.getEntity());

		if (player != null) {
			Location location = player.getLocation();

			GroupManager groupManager = subPlugin.getGroupManager();

			Group group = groupManager.getGroupByLocation(location);

			if (group != null && group.isSafearea()) {
				event.setCancelled(true);
			}
		}


	}

}
