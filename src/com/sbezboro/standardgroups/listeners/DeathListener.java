package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import com.sbezboro.standardplugin.model.StandardPlayer;
import com.sbezboro.standardplugin.util.MiscUtil;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener extends SubPluginEventListener<StandardGroups> implements Listener {

	public DeathListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	// Handle group power loss upon player death
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent event) {
		GroupManager groupManager = subPlugin.getGroupManager();
		
		StandardPlayer victimPlayer = plugin.getStandardPlayer(event.getEntity());
		
		Group victimGroup = groupManager.getPlayerGroup(victimPlayer);
		if (victimGroup == null) {
			return;
		}
		
		// Friends of the victim suffer power loss as well
		List<Group> victimFriends = victimGroup.getMutuallyFriendlyGroups();

		EntityDamageEvent damageEvent = victimPlayer.getLastDamageCause();
		LivingEntity entity = MiscUtil.getLivingEntityFromDamageEvent(damageEvent);
		StandardPlayer killerPlayer = plugin.getStandardPlayer(entity);
		
		double powerLoss = 0.0; // Base value
		double locationModifier = 1.0; // Depends on who owns the land the death happens on
		double manualModifier = 1.0; // From /g adjustmaxpower
		double configModifier = subPlugin.getPowerDamageModifier();
		Location location = victimPlayer.getLocation();
		Group locationGroup = groupManager.getGroupByLocation(location);

		// Death through direct PVP
		if (killerPlayer != null) {
			Group killerGroup = groupManager.getPlayerGroup(killerPlayer);
			
			// Base value
			if (killerGroup != null) {
				powerLoss = 2.0;
				manualModifier = killerGroup.getPowerDamageModifier();
			} else {
				powerLoss = 1.0;
			}
			
			// Consider location
			if (locationGroup == null) {
				locationModifier = 1.0;
			} else if (groupManager.playerInGroup(victimPlayer, locationGroup)) {
				locationModifier = 0.75;
				victimPlayer.sendMessage("You lost less power because you were killed on your own land");
			} else if (groupManager.playerInGroup(killerPlayer, locationGroup)) {
				manualModifier = Math.max(victimGroup.getPowerDamageModifier(), manualModifier);
				locationModifier = 1.5;
				victimPlayer.sendMessage("You lost more power because you were killed on your enemy's land");
			}
		
			// Spawnkill protection
			if (victimPlayer.lastDeathBySpawnkill()) {
				if (victimGroup.getPower() > -8.0) {
					locationModifier = 0.25;
					victimPlayer.sendMessage("You lost very little power because you appear to be spawncamped");
				} else {
					locationModifier = 0.0;
					victimPlayer.sendMessage("You did not lose any more power because you appear to be spawncamped");
				}
			}
			
			// Take care of friend groups
			if (!victimFriends.isEmpty()) {
				for (Group friend : victimFriends) {
					if (friend.getPower() >= -7.5) {
						friend.addPower(-powerLoss * locationModifier * manualModifier * configModifier * 0.3);
						double power = friend.getPower();
						ChatColor powerColor = (power < -10.0 ? ChatColor.DARK_RED : (power < 0.0 ? ChatColor.RED : ChatColor.RESET));
						friend.sendGroupMessage("Your power is now " + powerColor + friend.getPowerRounded() +
								ChatColor.RESET + " / " + friend.getMaxPowerRounded());
						
						if (killerGroup != null && powerLoss * locationModifier > 0.0) {
							friend.addPvpPowerLoss(killerGroup.getUid(), powerLoss * locationModifier * manualModifier * configModifier * 0.3);
						}
					}
				}
			}
			
			if (killerGroup != null && powerLoss * locationModifier > 0.0) {
				victimGroup.addPvpPowerLoss(killerGroup.getUid(), powerLoss * locationModifier * manualModifier * configModifier);
			}
		} else {
			// Death through indirect PVP
			if (victimPlayer.lastDeathInPvp()) {
				killerPlayer = plugin.getStandardPlayerByUUID(victimPlayer.getLastAttackerUuid());
				Group killerGroup = groupManager.getPlayerGroup(killerPlayer);
			
				// Base value
				if (killerGroup != null) {
					powerLoss = 2.0;
					manualModifier = killerGroup.getPowerDamageModifier();
				} else {
					powerLoss = 1.0;
				}
				
				// Consider location
				if (locationGroup == null) {
					locationModifier = 1.0;
				} else if (groupManager.playerInGroup(victimPlayer, locationGroup)) {
					locationModifier = 0.75;
					victimPlayer.sendMessage("You lost less power because you were killed on your own land");
				} else if (groupManager.playerInGroup(killerPlayer, locationGroup)) {
					manualModifier = Math.max(victimGroup.getPowerDamageModifier(), manualModifier);
					locationModifier = 1.5;
					victimPlayer.sendMessage("You lost more power because you were killed on your enemy's land");
				}
		
				// Spawnkill protection
				if (victimPlayer.lastDeathBySpawnkill()) {
					if (victimGroup.getPower() > -8.0) {
						locationModifier = 0.25;
						victimPlayer.sendMessage("You lost very little power because you appear to be spawncamped");
					} else {
						locationModifier = 0.0;
						victimPlayer.sendMessage("You did not lose any more power because you appear to be spawncamped");
					}
				}
				
				// Take care of friend groups
				if (!victimFriends.isEmpty()) {
					for (Group friend : victimFriends) {
						if (friend.getPower() >= -7.5) {
							friend.addPower(-powerLoss * locationModifier * manualModifier * configModifier * 0.3);
							double power = friend.getPower();
							ChatColor powerColor = (power < -10.0 ? ChatColor.DARK_RED : (power < 0.0 ? ChatColor.RED : ChatColor.RESET));
							friend.sendGroupMessage("Your power is now " + powerColor + friend.getPowerRounded() +
									ChatColor.RESET + " / " + friend.getMaxPowerRounded());
							
							if (killerGroup != null && powerLoss * locationModifier > 0.0) {
								friend.addPvpPowerLoss(killerGroup.getUid(), powerLoss * locationModifier * manualModifier * configModifier * 0.3);
							}
						}
					}
				}
				
				if (killerGroup != null && powerLoss * locationModifier > 0.0) {
					victimGroup.addPvpPowerLoss(killerGroup.getUid(), powerLoss * locationModifier * manualModifier * configModifier);
				}
			} 
			
			// Death through PVE
			else {
				powerLoss = 1.0;
				double currentPower = victimGroup.getPower();
				if (currentPower <= 0.0) {
					victimPlayer.sendMessage("You did not lose any more power because it already is negative");
					return;
				}
				
				if (locationGroup == null || groupManager.playerInGroup(victimPlayer, locationGroup)) {
					locationModifier = 1.0;
				} else {
					locationModifier = 3.0;
					victimPlayer.sendMessage("You lost more power because you died on someone else's land");
				}
				if (currentPower > 0.0 && currentPower - powerLoss * locationModifier * configModifier < 0.0) {
					victimGroup.addPower(-currentPower);
					locationModifier = 0.0;
					victimPlayer.sendMessage("Your power loss was capped so your power would not fall below zero");
				}
			}
		}
		
		// Apply power loss to victim
		victimGroup.addPower(-powerLoss * locationModifier * manualModifier * configModifier);
		double power = victimGroup.getPower();
		ChatColor powerColor = (power < -10.0 ? ChatColor.DARK_RED : (power < 0.0 ? ChatColor.RED : ChatColor.RESET));
		victimGroup.sendGroupMessage("Your power is now " + powerColor + victimGroup.getPowerRounded() +
				ChatColor.RESET + " / " + victimGroup.getMaxPowerRounded());
	}
}
