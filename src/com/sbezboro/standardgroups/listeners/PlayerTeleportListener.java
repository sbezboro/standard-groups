package com.sbezboro.standardgroups.listeners;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginEventListener;
import com.sbezboro.standardplugin.model.StandardPlayer;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class PlayerTeleportListener extends SubPluginEventListener<StandardGroups> implements Listener {
	public PlayerTeleportListener(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		StandardPlayer player = plugin.getStandardPlayer(event.getPlayer());
		TeleportCause cause = event.getCause();
		Location to = event.getTo();

		Group group = subPlugin.getGroupManager().getGroupByLocation(to);

		if (cause == TeleportCause.CHORUS_FRUIT && group != null && !group.isMember(player)) {
			event.setCancelled(true);
		}
	}
}
