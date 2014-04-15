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
		
		Group group = groupManager.getGroupByLocation(location);
		
		if (group != null) {
			if (groupManager.playerInGroup(player, group)) {
				List<Lock> locks = groupManager.getLocksAffectedByBlock(group, location);

				if (!locks.isEmpty()) {
					if (groupManager.isOwnerOfAllLocks(player, locks)) {
						if (locks.size() == 1) {
							player.sendMessage(ChatColor.YELLOW + "Your lock on that block has been broken.");
						} else {
							player.sendMessage(ChatColor.YELLOW + "The locks associated with that block have been broken.");
						}

						for (Lock lock : locks) {
							group.unlock(lock);
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
			} else if (!groupManager.isGroupsAdmin(player)) {
				event.setCancelled(true);
				if (group.isSafearea()) {
					player.sendMessage(ChatColor.RED + "Cannot break blocks in the safearea");
				} else {
					player.sendMessage(ChatColor.RED + "Cannot break blocks in the territory of " + group.getName());
				}
			}
		}
	}
	
}
