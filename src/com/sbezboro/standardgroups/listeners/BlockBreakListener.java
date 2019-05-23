package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardgroups.model.Lock;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import com.sbezboro.standardplugin.model.StandardPlayer;

import net.minecraft.server.v1_14_R1.Enchantments;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;

public class BlockBreakListener extends SubPluginEventListener<StandardGroups> implements Listener {
	
	public BlockBreakListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		StandardPlayer player = plugin.getStandardPlayer(event.getPlayer());
		
		Location location = event.getBlock().getLocation();
		
		GroupManager groupManager = subPlugin.getGroupManager();
		
		Group victimGroup = groupManager.getGroupByLocation(location);
		Group attackerGroup = groupManager.getPlayerGroup(player);
		
		if (victimGroup != null) {
			Block playerBlock = event.getBlock().getWorld().getBlockAt(player.getLocation());

			// Allow players to break one block in front of portals to get out of portal traps
			if (StandardPlugin.BED_BLOCKS.contains(playerBlock.getType())) {
				Block targetBlock = event.getBlock();

				byte direction = playerBlock.getData();

				boolean canBreakNearPortal =
					victimGroup == groupManager.getGroupByLocation(player.getLocation()) &&
					targetBlock.getY() >= playerBlock.getY() &&
					targetBlock.getY() <= playerBlock.getY() + 1 && (
						(
							direction % 2 == 1 &&
							playerBlock.getX() == targetBlock.getX() &&
							Math.abs(playerBlock.getZ() - targetBlock.getZ()) == 1
						) || (
							direction % 2 == 0 &&
							playerBlock.getZ() == targetBlock.getZ() &&
							Math.abs(playerBlock.getX() - targetBlock.getX()) == 1
						)
				);

				if (canBreakNearPortal) {
					List<Lock> locks = groupManager.getLocksAffectedByBlock(victimGroup, location);
					for (Lock lock : locks) {
						victimGroup.unlock(lock);
					}

					event.setCancelled(false);
					return;
				}
			}
			
			// Player in group
			if (groupManager.playerInGroup(player, victimGroup)) {
				List<Lock> locks = groupManager.getLocksAffectedByBlock(victimGroup, location);

				if (!locks.isEmpty()) {
					if (groupManager.isOwnerOfAllLocks(player, locks)) {
						if (locks.size() == 1) {
							player.sendMessage(ChatColor.YELLOW + "Your lock on that block has been broken.");
						} else {
							player.sendMessage(ChatColor.YELLOW + "The locks associated with that block have been broken.");
						}

						for (Lock lock : locks) {
							victimGroup.unlock(lock);
						}
					} else {
						if (locks.size() == 1) {
							player.sendMessage(ChatColor.YELLOW + "There is a lock on this block that you don't own.");
						} else {
							player.sendMessage(ChatColor.YELLOW + "You don't own all the locks associated with that block.");
						}

						event.setCancelled(true);
					}
				}
			} 
			
			// Player not in group, but group has low power, so blocks may be breakable
			else if (victimGroup.getPower() < 0.0) {
				if (groupManager.isGroupsAdmin(player)) {
					return;
				}
				if (StandardPlugin.BED_BLOCKS.contains(event.getBlock().getType())) {
					player.sendMessage(ChatColor.RED + "Cannot break beds in the territory of " + victimGroup.getName());
					event.setCancelled(true);
					return;
				}
				if (attackerGroup == null) {
					player.sendMessage(ChatColor.RED + "Cannot break blocks in the territory of " + victimGroup.getName());
					event.setCancelled(true);
					return;
				}
				String attackerGroupUid = attackerGroup.getUid();
				
				double power = victimGroup.getPower();
				
				List<Lock> locks = groupManager.getLocksAffectedByBlock(victimGroup, location);

				// Locked blocks
				if (!locks.isEmpty()) {
					if (power < GroupManager.LOCK_POWER_THRESHOLD) {
						if (victimGroup.getPvpPowerLoss(attackerGroupUid) < 2.0) {
							player.sendMessage(ChatColor.GOLD + "Only recent killers can break locks in the territory of " + victimGroup.getName());
							event.setCancelled(true);
							return;
						}
						
						player.sendMessage(ChatColor.YELLOW + "You broke the enemy's lock on that block.");

						for (Lock lock : locks) {
							victimGroup.unlock(lock);
							// Restore some of victim's power
							// May in rare cases allow people to break more locks than they are supposed to (e.g. door on furnace)
							victimGroup.addPower(2.0);
							victimGroup.reducePvpPowerLoss(attackerGroupUid, 2.0);
						}
						
						victimGroup.sendGroupMessage(ChatColor.RED + "A lock of your group has been broken.");
					} else {
						player.sendMessage(ChatColor.GOLD + "Cannot yet break locks in the territory of " + victimGroup.getName());
						event.setCancelled(true);
						return;
					}
				}
				// No locks
				else {
					if (power >= GroupManager.BLOCK_POWER_THRESHOLD) {
						player.sendMessage(ChatColor.GOLD + "Cannot yet break blocks in the territory of " + victimGroup.getName());
						event.setCancelled(true);
						return;
					}

					// Restore some of victim's power
					double powerRestoration = 0.25;
					if (victimGroup.getPvpPowerLoss(attackerGroupUid) < powerRestoration) {
						player.sendMessage(ChatColor.GOLD + "Only recent killers can break blocks in the territory of " + victimGroup.getName());
						event.setCancelled(true);
						return;
					}
					victimGroup.addPower(powerRestoration);
					victimGroup.reducePvpPowerLoss(attackerGroupUid, powerRestoration);
				}
				
				// Prevent ice blocks from forming water when broken
				if (event.getBlock().getType() == Material.ICE && !player.getItemInHand().getEnchantments().containsKey(Enchantments.SILK_TOUCH)) {
					event.setCancelled(true);
					event.getBlock().setType(Material.AIR);
				}
			}
			
			// Unauthorized. Canceling
			else if (!groupManager.isGroupsAdmin(player)) {
				event.setCancelled(true);
				if (victimGroup.isSafeArea()) {
					player.sendMessage(ChatColor.RED + "Cannot break blocks in the safe area");
				} else if (victimGroup.isNeutralArea()) {
					player.sendMessage(ChatColor.RED + "Cannot break blocks in the neutral area");
				} else {
					player.sendMessage(ChatColor.RED + "Cannot break blocks in the territory of " + victimGroup.getName());
				}
			}
		}
	}

}
