package com.sbezboro.standardgroups.listeners;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import com.sbezboro.standardplugin.model.StandardPlayer;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener extends SubPluginEventListener<StandardGroups> implements Listener {
	
	@SuppressWarnings("serial")
	private static final HashSet<Material> PROTECTED_BLOCKS = new HashSet<Material>() {{
		add(Material.CHEST);
		add(Material.WOODEN_DOOR);
		add(Material.FENCE_GATE);
		add(Material.IRON_DOOR);
		add(Material.TRAP_DOOR);
		add(Material.ENDER_CHEST);
		add(Material.HOPPER);
		add(Material.FURNACE);
		add(Material.DISPENSER);
		add(Material.DROPPER);
		add(Material.ENCHANTMENT_TABLE);
		add(Material.TRAPPED_CHEST);
		add(Material.JUKEBOX);
		add(Material.BREWING_STAND);
		add(Material.ANVIL);
		add(Material.DRAGON_EGG);
		add(Material.NOTE_BLOCK);
		add(Material.CAULDRON);
		add(Material.REDSTONE_COMPARATOR);
		add(Material.REDSTONE_COMPARATOR_OFF);
		add(Material.REDSTONE_COMPARATOR_ON);
		add(Material.DIODE);
		add(Material.DIODE_BLOCK_OFF);
		add(Material.DIODE_BLOCK_ON);
	}};

	@SuppressWarnings("serial")
	private static final HashSet<EntityType> PROTECTED_ENTITIES = new HashSet<EntityType>() {{
		add(EntityType.ITEM_FRAME);
	}};
	
	public PlayerInteractListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteract(final PlayerInteractEvent event) {
		Block clickedBlock = event.getClickedBlock();
		ItemStack itemStack = event.getItem();
		
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK ||
				(event.getAction() == Action.LEFT_CLICK_BLOCK && clickedBlock.getType().equals(Material.DRAGON_EGG))) {
			if (PROTECTED_BLOCKS.contains(clickedBlock.getType()) ||
					(itemStack != null && itemStack.getType() == Material.INK_SACK)) {
				StandardPlayer player = plugin.getStandardPlayer(event.getPlayer());

				checkLocation(player, clickedBlock.getLocation(), event);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerEntityInteract(final PlayerInteractEntityEvent event) {
		Entity entity = event.getRightClicked();

		if (PROTECTED_ENTITIES.contains(entity.getType())) {
			StandardPlayer player = plugin.getStandardPlayer(event.getPlayer());

			checkLocation(player, entity.getLocation(), event);
		}
	}

	@EventHandler
	public void onEntityDamage(final EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();

		if (PROTECTED_ENTITIES.contains(entity.getType())) {
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
