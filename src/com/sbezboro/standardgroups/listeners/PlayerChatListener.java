package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import com.sbezboro.standardplugin.model.StandardPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener extends SubPluginEventListener<StandardGroups> implements Listener {

	public PlayerChatListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEarlyPlayerChat(AsyncPlayerChatEvent event) {
		StandardPlayer player = plugin.getStandardPlayer(event.getPlayer());

		GroupManager groupManager = subPlugin.getGroupManager();

		Group chatPlayerGroup = groupManager.getPlayerGroup(player);

		String format = event.getFormat();
		String message = event.getMessage();

		event.setCancelled(true);

		if (chatPlayerGroup != null && chatPlayerGroup.isGroupChat(player)) {
			format = ChatColor.GREEN + "(To group) " + ChatColor.AQUA + "%s" + ChatColor.RESET + ": %s";

			Bukkit.getConsoleSender().sendMessage(String.format(
					format.replace("(To group)", "(To group " + chatPlayerGroup.getName() + ")"),
					player.getDisplayName(), message));

			for (StandardPlayer otherPlayer : chatPlayerGroup.getPlayers()) {
				if (otherPlayer.isOnline()) {
					otherPlayer.sendMessage(String.format(format, player.getDisplayName(), message));
				}
			}
		} else if (chatPlayerGroup != null && chatPlayerGroup.isFriendChat(player)) {
			format = ChatColor.DARK_AQUA + "(To friends) " + ChatColor.AQUA + "%s" + ChatColor.RESET + ": %s";

			Bukkit.getConsoleSender().sendMessage(String.format(
					format.replace("(To friends)", "(To friends of " + chatPlayerGroup.getName() + ")"),
					player.getDisplayName(), message));
	
			for (StandardPlayer otherPlayer : chatPlayerGroup.getPlayers()) {
				if (otherPlayer.isOnline()) {
					otherPlayer.sendMessage(String.format(format, player.getDisplayName(), message));
				}
			}
			for (Group friend : chatPlayerGroup.getMutuallyFriendlyGroups()) {
				for (StandardPlayer otherPlayer : friend.getPlayers()) {
					if (otherPlayer.isOnline()) {
						otherPlayer.sendMessage(String.format(format, player.getDisplayName(), message));
					}
				}
			}
		} else {
			String identifier = groupManager.getGroupIdentifier(player);
			if (chatPlayerGroup != null) {
				identifier += "[" + chatPlayerGroup.getName() + "] ";
			}

			format = format.replace("[GROUP]", identifier);

			Bukkit.getConsoleSender().sendMessage(String.format(format, player.getDisplayName(), message));

			for (Player recipient : event.getRecipients()) {
				StandardPlayer onlinePlayer = plugin.getStandardPlayer(recipient);

				String playerFormat = format;
				Group onlinePlayerGroup = groupManager.getPlayerGroup(onlinePlayer);

				if (onlinePlayerGroup != null &&
						onlinePlayerGroup == chatPlayerGroup) {
					playerFormat = playerFormat.replace("[", String.valueOf(ChatColor.GREEN) + "[");
				} else if (onlinePlayerGroup != null &&
						chatPlayerGroup != null &&
						onlinePlayerGroup.isMutualFriendship(chatPlayerGroup)) {
					playerFormat = playerFormat.replace("[", String.valueOf(ChatColor.DARK_AQUA) + "[");
				} else {
					playerFormat = playerFormat.replace("[", String.valueOf(ChatColor.YELLOW) + "[");
				}

				onlinePlayer.sendMessage(String.format(playerFormat, player.getDisplayName(), message));
			}
		}
	}
	
}
