package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import com.sbezboro.standardplugin.listeners.EventListener;
import com.sbezboro.standardplugin.model.StandardPlayer;
import com.sbezboro.standardplugin.util.MiscUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.events.DeathEvent;
import com.sbezboro.standardplugin.events.KillEvent;

public class DeathListener extends SubPluginEventListener<StandardGroups> implements Listener {

	public DeathListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent event) {
		GroupManager groupManager = subPlugin.getGroupManager();
		
		StandardPlayer victimPlayer = plugin.getStandardPlayer(event.getEntity());
		
		Group victimGroup = groupManager.getPlayerGroup(victimPlayer);
		if (victimGroup == null) {
			return;
		}

		EntityDamageEvent damageEvent = victimPlayer.getLastDamageCause();
		LivingEntity entity = MiscUtil.getLivingEntityFromDamageEvent(damageEvent);
		StandardPlayer killerPlayer = plugin.getStandardPlayer(entity);
		
		double powerLoss = 0.0;
		double locationModifier = 1.0;
		Location location = victimPlayer.getLocation();
		Group locationGroup = groupManager.getGroupByLocation(location);

		if (killerPlayer != null) {			
			Group killerGroup = groupManager.getPlayerGroup(killerPlayer);
			
			if (killerGroup != null) {
				double killerNonAltPlayerCount = (double)(killerGroup.getNonAltPlayerCount());
				if (killerNonAltPlayerCount == 0.0) {
					powerLoss = 0.0;
				} else {
					powerLoss = 3.5 - (1.0 / killerNonAltPlayerCount - (1.0 * (killerGroup.getMaxPower() / 20.0)));
				}
			} else {
				powerLoss = 1.5;
			}
			
			if (locationGroup == null) {
				locationModifier = 1.0;
			} else if (groupManager.playerInGroup(victimPlayer, locationGroup)) {
				locationModifier = 0.6666667;
			} else if (groupManager.playerInGroup(killerPlayer, locationGroup)) {
				powerLoss = 3.5 - (1.0 / (double)(victimGroup.getPlayerCount())) - (1.0 * (victimGroup.getMaxPower() / 20.0));
				locationModifier = 2.0;
			}
		
			if (killerPlayer.hasTitle("Alt")) {
				powerLoss = 0.0;
			}
		} else {
			if (victimPlayer.isInPvp()) {
				killerPlayer = victimPlayer.getLastAttacker();
				Group killerGroup = groupManager.getPlayerGroup(killerPlayer);
			
				if (killerGroup != null) {
					double killerNonAltPlayerCount = (double)(killerGroup.getNonAltPlayerCount());
					if (killerNonAltPlayerCount == 0) {
						powerLoss = 0.0;
					} else {
						powerLoss = 3.5 - (1.0 / killerNonAltPlayerCount) - (1.0 * (killerGroup.getMaxPower() / 20.0));
					}
				} else {
					powerLoss = 1.5;
				}
				
				if (locationGroup == null) {
					locationModifier = 1.0;
				} else if (groupManager.playerInGroup(victimPlayer, locationGroup)) {
					locationModifier = 0.6666667;
				} else if (groupManager.playerInGroup(killerPlayer, locationGroup)) {
					powerLoss = 3.5 - (1.0 / (double)(victimGroup.getPlayerCount())) - (1.0 * (victimGroup.getMaxPower() / 20.0));
					locationModifier = 2.0;
				}
		
				if (killerPlayer.hasTitle("Alt")) {
					powerLoss = 0.0;
				}
			} else {
				powerLoss = 1.5;
				if (locationGroup == null || groupManager.playerInGroup(victimPlayer, locationGroup)) {
					locationModifier = 1.0;
				} else {
					locationModifier = 3.0;
				}
				double currentPower = victimGroup.getPower();
				if (currentPower >= 0.0 && currentPower - powerLoss * locationModifier < 0.0) {
					victimGroup.addPower(-currentPower);
					return;
				} else if (currentPower < 0.0) {
					return;
				}
			}
		}
		
		victimGroup.addPower(-powerLoss * locationModifier);
	}
}
