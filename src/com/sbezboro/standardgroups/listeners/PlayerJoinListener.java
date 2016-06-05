package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import com.sbezboro.standardplugin.model.StandardPlayer;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener extends SubPluginEventListener<StandardGroups> implements Listener {
	
	public PlayerJoinListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		GroupManager groupManager = subPlugin.getGroupManager();
		
		StandardPlayer player = plugin.getStandardPlayer(event.getPlayer());
		
		Group group = groupManager.getPlayerGroup(player);
		
		if (group != null) {
			double power = group.getPower();
			if (power < GroupManager.LOCK_POWER_THRESHOLD) {
				player.sendMessage(ChatColor.DARK_RED + "Your group's locks are breakable due to low current power");
			} else if (power < GroupManager.BLOCK_POWER_THRESHOLD) {
				player.sendMessage(ChatColor.DARK_RED + "Your group's current power is dangerously low");
			} else if (power < 0.0) {
				player.sendMessage(ChatColor.RED + "Your group's current power is negative");
			}
		}
		
		groupManager.enableCommandCooldown(new String(player.getUuidString()));
	}	
}
