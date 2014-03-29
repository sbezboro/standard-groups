package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import com.sbezboro.standardplugin.model.StandardPlayer;
import org.bukkit.ChatColor;
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
		if (damager == null && damagerEntity instanceof Arrow) {
			damager = plugin.getStandardPlayer(((Arrow) damagerEntity).getShooter());
		}

		// Player attacking
		if (damager != null) {
			StandardPlayer victim = plugin.getStandardPlayer(event.getEntity());

			if (damager == victim) {
				return;
			}

			// Player victim
			if (victim != null) {
				GroupManager groupManager = subPlugin.getGroupManager();

				if (groupManager.getGroupByLocation(victim.getLocation()).isSafearea()) {
					damager.sendMessage(ChatColor.YELLOW + "You can't harm players in the safearea");
					event.setCancelled(true);
				} else if (groupManager.getPlayerGroup(damager) == groupManager.getPlayerGroup(victim)) {
					damager.sendMessage(ChatColor.YELLOW + "You can't harm a fellow group member.");
					event.setCancelled(true);
				}
			}
		}
	}

}
