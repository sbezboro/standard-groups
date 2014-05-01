package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import com.sbezboro.standardplugin.model.StandardPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PlayerDamageListener extends SubPluginEventListener<StandardGroups> implements Listener {

	public PlayerDamageListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		Entity damagerEntity = event.getDamager();
		StandardPlayer damager = plugin.getStandardPlayer(damagerEntity);
		StandardPlayer victim = plugin.getStandardPlayer(event.getEntity());

		if (damager == null && damagerEntity instanceof Arrow) {
			damager = plugin.getStandardPlayer(((Arrow) damagerEntity).getShooter());
		}

		// Player attacking
		if (damager != null) {
			if (damager == victim) {
				return;
			}

			// Player victim
			if (victim != null) {
				GroupManager groupManager = subPlugin.getGroupManager();
				Group damagerLocationGroup = groupManager.getGroupByLocation(damager.getLocation());
				Group victimLocationGroup = groupManager.getGroupByLocation(victim.getLocation());
				Group damagerGroup = groupManager.getPlayerGroup(damager);
				Group victimGroup = groupManager.getPlayerGroup(victim);

				if (victimLocationGroup != null && victimLocationGroup.isSafearea()) {
					damager.sendMessage(ChatColor.YELLOW + "You can't harm players in the safearea");
					event.setCancelled(true);
					return;
				} else if (damagerLocationGroup != null && damagerLocationGroup.isSafearea()) {
					damager.sendMessage(ChatColor.YELLOW + "You can't harm players while in the safearea");
					event.setCancelled(true);
					return;
				}

				if (damagerGroup != null && damagerGroup == victimGroup) {
					damager.sendMessage(ChatColor.YELLOW + "You can't harm a fellow group member.");
					event.setCancelled(true);
					return;
				}
			}
		}

		if (victim != null) {
			GroupManager groupManager = subPlugin.getGroupManager();

			Group group = groupManager.getGroupByLocation(victim.getLocation());

			if (group != null && group.isSafearea()) {
				event.setCancelled(true);
			}
		}
	}

}
