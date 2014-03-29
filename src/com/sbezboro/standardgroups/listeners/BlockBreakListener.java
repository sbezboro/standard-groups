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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener extends SubPluginEventListener<StandardGroups> implements Listener {
	
	public BlockBreakListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		StandardPlayer player = plugin.getStandardPlayer(event.getPlayer());
		
		Location location = event.getBlock().getLocation();
		
		GroupManager groupManager = subPlugin.getGroupManager();
		
		Group group = groupManager.getGroupByLocation(location);
		
		if (group != null) {
			if (groupManager.playerInGroup(player, group)) {
				Lock lock = groupManager.getLockAffectedByBlock(group, location);

				if (lock != null) {
					if (lock.isOwner(player)) {
						player.sendMessage(ChatColor.YELLOW + "Your lock on that block has been broken.");
						group.unlock(lock);
					} else if (lock.hasAccess(player)) {
						event.setCancelled(true);
						player.sendMessage(ChatColor.RED + "There is a lock on this block that you don't own.");
					} else {
						event.setCancelled(true);
						player.sendMessage(ChatColor.RED + "This block is locked and you can not break it.");
					}
				}
			} else {
				event.setCancelled(true);
				player.sendMessage(ChatColor.RED + "Cannot break blocks in the territory of " + group.getName());
			}
		}
	}
	
}
