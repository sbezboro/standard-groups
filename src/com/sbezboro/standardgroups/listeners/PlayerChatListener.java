package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardgroups.model.Lock;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import com.sbezboro.standardplugin.model.StandardPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

public class PlayerChatListener extends SubPluginEventListener<StandardGroups> implements Listener {

	public PlayerChatListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(ignoreCancelled = true)
	public void onEarlyPlayerChat(AsyncPlayerChatEvent event) {
		StandardPlayer player = plugin.getStandardPlayer(event.getPlayer());

		GroupManager groupManager = subPlugin.getGroupManager();

		Group group = groupManager.getPlayerGroup(player);

		String format = event.getFormat();
		String message = event.getMessage();

		event.setCancelled(true);

		if (group != null && group.isGroupChat(player)) {
			format = ChatColor.GREEN + "(To group) " + ChatColor.AQUA + "%s" + ChatColor.RESET + ": %s";

			Bukkit.getConsoleSender().sendMessage(String.format(format, player.getDisplayName(), message));

			for (StandardPlayer otherPlayer : group.getPlayers()) {
				if (otherPlayer.isOnline()) {

					otherPlayer.sendMessage(String.format(format, player.getDisplayName(), message));
				}
			}
		} else {
			String identifier = groupManager.getGroupIdentifier(player);
			format = format.replace("[GROUP]", identifier);

			Bukkit.getConsoleSender().sendMessage(String.format(format, player.getDisplayName(), message));

			for (StandardPlayer onlinePlayer : plugin.getOnlinePlayers()) {
				String playerFormat = format;

				if (groupManager.getPlayerGroup(onlinePlayer) == group) {
					playerFormat = playerFormat.replace("[", String.valueOf(ChatColor.GREEN) + "[");
				} else {
					playerFormat = playerFormat.replace("[", String.valueOf(ChatColor.YELLOW) + "[");
				}

				onlinePlayer.sendMessage(String.format(playerFormat, player.getDisplayName(), message));
			}
		}
	}
	
}
