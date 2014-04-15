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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class BlockPlaceListener extends SubPluginEventListener<StandardGroups> implements Listener {

	public BlockPlaceListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockPlace(final org.bukkit.event.block.BlockPlaceEvent event) {
		StandardPlayer player = plugin.getStandardPlayer(event.getPlayer());
		
		Location location = event.getBlock().getLocation();
		
		GroupManager groupManager = subPlugin.getGroupManager();
		
		Group group = groupManager.getGroupByLocation(location);

		if (group != null) {
			if (groupManager.playerInGroup(player, group)) {
				// Sanity check
				Lock lock = group.getLock(location);
				if (lock != null) {
					subPlugin.getLogger().severe("REMOVING STALE LOCK! " + MiscUtil.locationFormat(location));
					group.unlock(lock);
				}
			} else if (!groupManager.isGroupsAdmin(player)) {
				event.setCancelled(true);
				if (group.isSafearea()) {
					player.sendMessage(ChatColor.RED + "Cannot place blocks in the safearea");
				} else {
					player.sendMessage(ChatColor.RED + "Cannot place blocks in the territory of " + group.getName());
				}
			}
		}
	}

}
