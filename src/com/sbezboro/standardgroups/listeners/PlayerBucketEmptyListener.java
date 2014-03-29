package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import com.sbezboro.standardplugin.model.StandardPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

public class PlayerBucketEmptyListener extends SubPluginEventListener<StandardGroups> implements Listener {
	public PlayerBucketEmptyListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerBukkitEmpty(PlayerBucketEmptyEvent event) {
		StandardPlayer player = plugin.getStandardPlayer(event.getPlayer());

		GroupManager groupManager = subPlugin.getGroupManager();

		Block block = event.getBlockClicked().getRelative(event.getBlockFace());

		Location location = block.getLocation();

		Group group = groupManager.getGroupByLocation(location);

		if (group != null) {
			if (!groupManager.playerInGroup(player, group) && !groupManager.isGroupsAdmin(player)) {
				player.sendMessage(ChatColor.RED + "Cannot empty bucket in the territory of " + group.getName());
				event.setCancelled(true);
			}
		}
	}

}
