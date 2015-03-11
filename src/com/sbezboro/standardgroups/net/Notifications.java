package com.sbezboro.standardgroups.net;

import com.sbezboro.http.HttpRequestManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.model.StandardPlayer;
import com.sbezboro.standardplugin.net.NotificationHttpRequest;

import java.util.HashMap;
import java.util.Map;


public class Notifications {

	public static void createKickedFromGroupNotification(StandardPlayer kickedPlayer, Group group,
														 StandardPlayer kickerPlayer) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("group_name", group.getName());

		if (kickerPlayer != null) {
			data.put("kicker_uuid", kickerPlayer.getUuidString());
		}

		HttpRequestManager.getInstance().startRequest(
				new NotificationHttpRequest("kicked_from_group", kickedPlayer.getUuidString(), data, null)
		);
	}

	public static void createKickedFromGroupNotification(StandardPlayer kickedPlayer, Group group) {
		Notifications.createKickedFromGroupNotification(kickedPlayer, group, null);
	}

	public static void createGroupDestroyedNotification(StandardPlayer player, Group group,
														StandardPlayer destroyerPlayer) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("group_name", group.getName());

		if (destroyerPlayer != null) {
			data.put("destroyer_uuid", destroyerPlayer.getUuidString());
		}

		HttpRequestManager.getInstance().startRequest(
				new NotificationHttpRequest("group_destroyed", player.getUuidString(), data, null)
		);
	}

	public static void createGroupDestroyedNotification(StandardPlayer player, Group group) {
		Notifications.createGroupDestroyedNotification(player, group, null);
	}

	public static void createGroupKickImminentNotification(StandardPlayer player, Group group) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("group_name", group.getName());

		HttpRequestManager.getInstance().startRequest(
				new NotificationHttpRequest("group_kick_imminent", player.getUuidString(), data, null)
		);
	}
}
