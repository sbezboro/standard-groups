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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener extends SubPluginEventListener<StandardGroups> implements Listener {

	public BlockPlaceListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockPlace(final BlockPlaceEvent event) {
		StandardPlayer player = plugin.getStandardPlayer(event.getPlayer());
		
		Location location = event.getBlock().getLocation();
		
		GroupManager groupManager = subPlugin.getGroupManager();
		
		Group victimGroup = groupManager.getGroupByLocation(location);
		Group attackerGroup = groupManager.getPlayerGroup(player);

		if (victimGroup != null) {
			// Player in group
			if (groupManager.playerInGroup(player, victimGroup)) {
				// Sanity check
				Lock lock = victimGroup.getLock(location);
				if (lock != null) {
					subPlugin.getLogger().severe("REMOVING STALE LOCK! " + MiscUtil.locationFormat(location));
					victimGroup.unlock(lock);
				}
			}
			
			// Player not in group, but group has low power, so blocks may be placeable
			else if (victimGroup.getPower() < 0.0) {
				if (groupManager.isGroupsAdmin(player)) {
					return;
				}

				if (StandardPlugin.BED_BLOCKS.contains(event.getBlock().getType())) {
					player.sendMessage(ChatColor.RED + "Cannot place beds in the territory of " + victimGroup.getName());
					event.setCancelled(true);
					return;
				}
				if (attackerGroup == null) {
					player.sendMessage(ChatColor.RED + "Cannot place blocks in the territory of " + victimGroup.getName());
					event.setCancelled(true);
					return;
				}
				String attackerGroupUid = attackerGroup.getUid();
				
				// Deny hoppers and dispensers from being placed (because of chests and water, respectively)
				Block targetBlock = event.getBlock();
				if (targetBlock.getType() == Material.HOPPER || targetBlock.getType() == Material.DISPENSER) {
					player.sendMessage(ChatColor.RED + "Cannot place this type of block in the territory of " + victimGroup.getName());
					event.setCancelled(true);
					return;
				}
				if (victimGroup.getPower() >= GroupManager.BLOCK_POWER_THRESHOLD) {
					player.sendMessage(ChatColor.GOLD + "Cannot yet place blocks in the territory of " + victimGroup.getName());
					event.setCancelled(true);
					return;
				}
				
				// Restore some of victim's power
				double powerRestoration;
				if (targetBlock.getType() == Material.TNT) {
					powerRestoration = 1.0;
				} else {
					powerRestoration = 0.25;
				}
				if (victimGroup.getPvpPowerLoss(attackerGroupUid) < powerRestoration) {
					player.sendMessage(ChatColor.GOLD + "Only recent killers can place blocks in the territory of " + victimGroup.getName());
					event.setCancelled(true);
					return;
				}
				victimGroup.addPower(powerRestoration);
				victimGroup.reducePvpPowerLoss(attackerGroupUid, powerRestoration);
			}
			

			// Unauthorized. Canceling
			else if (!groupManager.isGroupsAdmin(player)) {
				event.setCancelled(true);
				if (victimGroup.isSafeArea()) {
					player.sendMessage(ChatColor.RED + "Cannot place blocks in the safe area");
				} else if (victimGroup.isNeutralArea()) {
					player.sendMessage(ChatColor.RED + "Cannot place blocks in the neutral area");
				} else {
					player.sendMessage(ChatColor.RED + "Cannot place blocks in the territory of " + victimGroup.getName());
				}
			}
		}
	}

}
