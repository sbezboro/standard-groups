package com.sbezboro.standardgroups.listeners;

import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import com.sbezboro.standardplugin.model.StandardPlayer;

public class PlayerInteractListener extends SubPluginEventListener<StandardGroups> implements Listener {
	
	@SuppressWarnings("serial")
	private static final HashSet<Material> PROTECTED_BLOCKS = new HashSet<Material>() {{
		add(Material.CHEST);
		add(Material.WOODEN_DOOR);
		add(Material.IRON_DOOR);
		add(Material.TRAP_DOOR);
		add(Material.ENDER_CHEST);
		add(Material.HOPPER);
		add(Material.FURNACE);
		add(Material.DISPENSER);
		add(Material.DROPPER);
		add(Material.IRON_TRAP_DOOR);
		add(Material.ENCHANTMENT_TABLE);
		add(Material.TRAPPED_CHEST);
		add(Material.JUKEBOX);
		add(Material.BREWING_STAND);
		add(Material.ANVIL);
	}};
	
	public PlayerInteractListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler
	public void onPlayerInteract(final PlayerInteractEvent event) {
		Block clickedBlock = event.getClickedBlock();
		
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && PROTECTED_BLOCKS.contains(clickedBlock.getType())) {
			StandardPlayer player = plugin.getStandardPlayer(event.getPlayer());
			
			Location location = clickedBlock.getLocation();
			
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
	
}
