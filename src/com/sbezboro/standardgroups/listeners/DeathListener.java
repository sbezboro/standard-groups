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
		
		float powerLoss = 0.0f;
		float locationModifier = 1.0f;
		Location location = victimPlayer.getLocation();
		Group locationGroup = groupManager.getGroupByLocation(location);

		if (killerPlayer != null) {			
			Group killerGroup = groupManager.getPlayerGroup(killerPlayer);
			
			if (killerGroup != null) {
				float killerNonAltPlayerCount = (float)(killerGroup.getNonAltPlayerCount());
				if (killerNonAltPlayerCount == 0.0f) {
					powerLoss = 0.0f;
				} else {
					powerLoss = 3.5f - (1.0f / killerNonAltPlayerCount - (1.0f * (killerGroup.getMaxPower() / 20.0f)));
				}
			} else {
				powerLoss = 1.5f;
			}
			
			if (locationGroup == null) {
				locationModifier = 1.0f;
			} else if (groupManager.playerInGroup(victimPlayer, locationGroup)) {
				locationModifier = 0.6666667f;
			} else if (groupManager.playerInGroup(killerPlayer, locationGroup)) {
				powerLoss = 3.5f - (1.0f / (float)(victimGroup.getPlayerCount())) - (1.0f * (victimGroup.getMaxPower() / 20.0f));
				locationModifier = 2.0f;
			}
		
			if (killerPlayer.hasTitle("Alt")) {
				powerLoss = 0.0f;
			}
		} else {
			if (victimPlayer.isInPvp()) {
				killerPlayer = victimPlayer.getLastAttacker();
				Group killerGroup = groupManager.getPlayerGroup(killerPlayer);
			
				if (killerGroup != null) {
					float killerNonAltPlayerCount = (float)(killerGroup.getNonAltPlayerCount());
					if (killerNonAltPlayerCount == 0) {
						powerLoss = 0.0f;
					} else {
						powerLoss = 3.5f - (1.0f / killerNonAltPlayerCount) - (1.0f * (killerGroup.getMaxPower() / 20.0f));
					}
				} else {
					powerLoss = 1.5f;
				}
				
				if (locationGroup == null) {
					locationModifier = 1.0f;
				} else if (groupManager.playerInGroup(victimPlayer, locationGroup)) {
					locationModifier = 0.6666667f;
				} else if (groupManager.playerInGroup(killerPlayer, locationGroup)) {
					powerLoss = 3.5f - (1.0f / (float)(victimGroup.getPlayerCount())) - (1.0f * (victimGroup.getMaxPower() / 20.0f));
					locationModifier = 2.0f;
				}
		
				if (killerPlayer.hasTitle("Alt")) {
					powerLoss = 0.0f;
				}
			} else {
				powerLoss = 1.5f;
				if (locationGroup == null || groupManager.playerInGroup(victimPlayer, locationGroup)) {
					locationModifier = 1.0f;
				} else {
					locationModifier = 3.0f;
				}
				float currentPower = victimGroup.getPower();
				if (currentPower >= 0.0f && currentPower - powerLoss * locationModifier < 0.0f) {
					victimGroup.addPower(-currentPower);
					return;
				} else if (currentPower < 0.0f) {
					return;
				}
			}
		}
		
		victimGroup.addPower(-powerLoss * locationModifier);
	}
}
