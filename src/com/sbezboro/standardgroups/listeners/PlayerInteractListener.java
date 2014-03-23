package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import com.sbezboro.standardplugin.model.StandardPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener extends SubPluginEventListener<StandardGroups> implements Listener {
	
	public PlayerInteractListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteract(final PlayerInteractEvent event) {
		Block clickedBlock = event.getClickedBlock();
		ItemStack itemStack = event.getItem();
		
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK ||
				(event.getAction() == Action.LEFT_CLICK_BLOCK && clickedBlock.getType().equals(Material.DRAGON_EGG))) {
			if (GroupManager.isBlockTypeProtected(clickedBlock) ||
					(itemStack != null && itemStack.getType() == Material.INK_SACK)) {
				StandardPlayer player = plugin.getStandardPlayer(event.getPlayer());

				checkLocation(player, clickedBlock.getLocation(), event);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerEntityInteract(final PlayerInteractEntityEvent event) {
		Entity entity = event.getRightClicked();

		if (GroupManager.isEntityTypeProtected(entity)) {
			StandardPlayer player = plugin.getStandardPlayer(event.getPlayer());

			checkLocation(player, entity.getLocation(), event);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(final EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();

		if (GroupManager.isEntityTypeProtected(entity)) {
			StandardPlayer player = plugin.getStandardPlayer(event.getDamager());

			if (player != null) {
				checkLocation(player, entity.getLocation(), event);
			}
		}
	}

	private void checkLocation(StandardPlayer player, Location location, Cancellable event) {
		GroupManager groupManager = subPlugin.getGroupManager();

		Group group = groupManager.getGroupByLocation(location);

		if (group != null) {
			if (!groupManager.playerInGroup(player, group)) {
				event.setCancelled(true);
				player.sendMessage(ChatColor.RED + "Cannot use this in the territory of " + group.getName());
			}
		}
	}
	
}
