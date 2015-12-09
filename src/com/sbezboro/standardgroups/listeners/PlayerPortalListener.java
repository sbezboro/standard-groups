package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import com.sbezboro.standardplugin.model.StandardPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerPortalListener extends SubPluginEventListener<StandardGroups> implements Listener {
	public PlayerPortalListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerPortal(PlayerPortalEvent event) {
		StandardPlayer player = plugin.getStandardPlayer(event.getPlayer());
		PlayerTeleportEvent.TeleportCause cause = event.getCause();
		Location to = event.getTo();

		Group group = subPlugin.getGroupManager().getGroupByLocation(to);

		if (
			cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL &&
			group != null &&
			shouldPreventPortalInGroupLand(player, group)
		) {
			player.sendMessage("Portal destination in group land. Please shift portal location!");
			event.setCancelled(true);
		}
	}

	private boolean shouldPreventPortalInGroupLand(StandardPlayer player, Group group) {
		return group.isNeutralArea() || group.isSafeArea() || !group.isMember(player);
	}
}
