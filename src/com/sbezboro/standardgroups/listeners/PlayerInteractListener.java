package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardgroups.model.Lock;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import com.sbezboro.standardplugin.model.StandardPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;

public class PlayerInteractListener extends SubPluginEventListener<StandardGroups> implements Listener {

	@SuppressWarnings("serial")
	private static final HashSet<Material> WOODEN_BOATS = new HashSet<Material>() {{
		add(Material.BOAT);
		add(Material.BOAT_ACACIA);
		add(Material.BOAT_BIRCH);
		add(Material.BOAT_DARK_OAK);
		add(Material.BOAT_JUNGLE);
		add(Material.BOAT_SPRUCE);
	}};
	
	public PlayerInteractListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerInteract(final PlayerInteractEvent event) {
		Block clickedBlock = event.getClickedBlock();
		ItemStack itemStack = event.getItem();
		GroupManager groupManager = subPlugin.getGroupManager();
		StandardPlayer player = plugin.getStandardPlayer(event.getPlayer());
		
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK ||
				(event.getAction() == Action.LEFT_CLICK_BLOCK && clickedBlock.getType().equals(Material.DRAGON_EGG))) {
			if (GroupManager.isBlockTypeProtected(clickedBlock)) {
				checkPlayerAccess(player, clickedBlock.getLocation(), event);
			} else if (itemStack != null && (
					itemStack.getType() == Material.EXPLOSIVE_MINECART ||
					itemStack.getType() == Material.HOPPER_MINECART ||
					itemStack.getType() == Material.INK_SACK ||
					WOODEN_BOATS.contains(itemStack.getType()))) {
				checkPlayerAccess(player, clickedBlock.getLocation(), event);
			}
		} else if (event.getAction() == Action.PHYSICAL) {
			Block block = event.getClickedBlock();

			if (block != null && block.getType() == Material.SOIL) {
				checkPlayerAccess(player, clickedBlock.getLocation(), event);
			}
		} else if (event.getAction() == Action.LEFT_CLICK_BLOCK &&
				event.getBlockFace() == BlockFace.UP &&
				clickedBlock.getRelative(BlockFace.UP).getType() == Material.FIRE) {
			checkPlayerAccess(player, clickedBlock.getLocation(), event);
		}
		
		Group group = groupManager.getPlayerGroup(player);
		
		if (group == null) {
			return;
		}
		
		String uuid = new String(player.getUuidString());
		
		// Autocommands (/g autolock)
		if (event.getAction() == Action.LEFT_CLICK_BLOCK && group.hasAutoCommand(uuid)) {
			String[] autoCommandArgs = group.getAutoCommandArgs(uuid);
			
			if (autoCommandArgs == null) {
				return;
			}
			
			if (autoCommandArgs[0].equalsIgnoreCase("lock")) {
				if (clickedBlock.getType() == Material.DRAGON_EGG) {
					return;
				}
				
				if (autoCommandArgs.length == 1) {
					groupManager.lock(player, clickedBlock);
				}
				else if (autoCommandArgs.length == 2) {
					if (autoCommandArgs[1].equalsIgnoreCase("info")) {
						groupManager.lockInfo(player, clickedBlock);
					} else if (autoCommandArgs[1].equalsIgnoreCase("public")) {
						groupManager.togglePublicLock(player, clickedBlock);
					}
				}
				else if (autoCommandArgs.length == 3) {
					if (autoCommandArgs[1].equalsIgnoreCase("add")) {
						StandardPlayer otherPlayer = plugin.matchPlayer(autoCommandArgs[2]);

						if (otherPlayer == null) {
							player.sendMessage("That player doesn't exist");
						} else {
							groupManager.addLockMember(player, clickedBlock, otherPlayer);
						}
					} else if (autoCommandArgs[1].equalsIgnoreCase("remove")) {
						StandardPlayer otherPlayer = plugin.matchPlayer(autoCommandArgs[2]);

						if (otherPlayer == null) {
							player.sendMessage("That player doesn't exist");
						} else {
							groupManager.removeLockMember(player, clickedBlock, otherPlayer);
						}
					}
				}
			} else if (autoCommandArgs[0].equalsIgnoreCase("unlock")) {
				if (clickedBlock.getType() == Material.DRAGON_EGG) {
					return;
				}
				
				groupManager.unlock(player, clickedBlock);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerEntityInteract(final PlayerInteractEntityEvent event) {
		Entity entity = event.getRightClicked();

		if (GroupManager.isEntityTypeProtected(entity)) {
			StandardPlayer player = plugin.getStandardPlayer(event.getPlayer());

			checkPlayerAccess(player, entity.getLocation(), event);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityDamage(final EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();

		if (GroupManager.isEntityTypeProtected(entity)) {
			StandardPlayer player = plugin.getStandardPlayer(event.getDamager());

			if (player != null) {
				checkPlayerAccess(player, entity.getLocation(), event);
			}
		}
	}

	private void checkPlayerAccess(StandardPlayer player, Location location, Cancellable event) {
		GroupManager groupManager = subPlugin.getGroupManager();

		Group group = groupManager.getGroupByLocation(location);

		if (group != null) {
			// Allow special block use in safe or neutral area
			if ((group.isSafeArea() || group.isNeutralArea()) &&
					groupManager.isWhitelistedSafeareaBlock(location.getBlock())) {
				return;
			}

			List<Lock> locks = groupManager.getLocksAffectedByBlock(group, location);
			Lock lock = null;

			if (!locks.isEmpty()) {
				lock = locks.get(0);
			}

			if (lock != null && lock.isPublic()) {
				player.sendMessage(ChatColor.GREEN + "Using public block in the territory of " + group.getName());
			} else if (groupManager.playerInGroup(player, group)) {
				if (lock != null) {
					if (lock.isOwner(player)) {
						player.sendMessage(ChatColor.YELLOW + "Using locked block that you own");
					} else if (lock.hasAccess(player)) {
						player.sendMessage(ChatColor.YELLOW + "Using locked block that you have access to");
					} else {
						event.setCancelled(true);
						player.sendMessage(ChatColor.RED + "This block is locked and you do not have access to it.");
					}
				}
			} else {
				Group playerGroup = groupManager.getPlayerGroup(player);
				if (lock != null) {
					if (lock.hasAccess(player) && playerGroup != null) {
						if (group.isMutualFriendship(playerGroup)) {
							player.sendMessage(ChatColor.YELLOW + "Using locked block of a friended group that you have access to");
						} else {
							event.setCancelled(true);
							player.sendMessage(ChatColor.RED + "Your group needs to be friends with " + group.getName() +
									" in order to share locks.");
						}
					} else {
						event.setCancelled(true);
						player.sendMessage(ChatColor.RED + "This block is locked and you do not have access to it.");
					}
				} else if (!groupManager.isGroupsAdmin(player)) {
					event.setCancelled(true);
					player.sendMessage(ChatColor.RED + "Cannot use this in the territory of " + group.getName());
				}
			}
		}
	}
	
}
