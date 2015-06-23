package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import com.sbezboro.standardplugin.listeners.EventListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.EntityBlockFormEvent;

public class EntityBlockFormListener extends SubPluginEventListener<StandardGroups> implements Listener {


	public EntityBlockFormListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityBlockFormEvent(EntityBlockFormEvent event) {
		Entity entity = event.getEntity();

		if (entity.getType() == EntityType.SNOWMAN &&
				event.getNewState().getType() == Material.SNOW) {
			Group group = subPlugin.getGroupManager().getGroupByLocation(entity.getLocation());

			if (group != null && (group.isSafeArea() || group.isNeutralArea())) {
				event.setCancelled(true);
			}
		}
	}
}
