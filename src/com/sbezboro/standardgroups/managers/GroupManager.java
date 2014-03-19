package com.sbezboro.standardgroups.managers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;

import com.sbezboro.standardgroups.model.Claim;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardgroups.persistence.storages.GroupStorage;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.managers.BaseManager;
import com.sbezboro.standardplugin.model.StandardPlayer;

public class GroupManager extends BaseManager {
	private GroupStorage storage;
	
	private Map<String, Group> usernameToGroupMap;
	private Map<String, Claim> locationToClaimMap;
	private Map<String, Group> locationToGroupMap;

	public GroupManager(StandardPlugin plugin, GroupStorage storage) {
		super(plugin);
		
		this.storage = storage;
		this.storage.loadObjects();
		
		usernameToGroupMap = new HashMap<String, Group>();
		locationToClaimMap = new HashMap<String, Claim>();
		locationToGroupMap = new HashMap<String, Group>();
		
		for (Group group : storage.getGroups()) {
			for (String username : group.getMembers()) {
				usernameToGroupMap.put(username, group);
			}
			
			for (Claim claim : group.getClaims()) {
				locationToGroupMap.put(claim.getLocationKey(), group);
				locationToClaimMap.put(claim.getLocationKey(), claim);
			}
		}
	}
	
	public void createGroup(StandardPlayer player, String groupName) {
		if (usernameToGroupMap.containsKey(player.getName())) {
			player.sendMessage("You must leave your existing group first before creating a new one.");
			return;
		}
		
		if (storage.getGroupByName(groupName) != null) {
			player.sendMessage("That group name is already taken");
			return;
		}
		
		Group group = storage.createGroup(groupName, player);
		usernameToGroupMap.put(player.getName(), group);
		
		StandardPlugin.broadcast(ChatColor.YELLOW + player.getDisplayName(false) + " has created a new group called " + groupName + ".");
	}
	
	public void destroyGroup(StandardPlayer player) {
		Group group = usernameToGroupMap.get(player.getName());
		
		if (group == null) {
			player.sendMessage("You can't destroy a group if you aren't in one.");
			return;
		}

		for (String username : group.getMembers()) {
			usernameToGroupMap.remove(username);
		}
		
		storage.destroyGroup(group);
		
		StandardPlugin.broadcast(ChatColor.YELLOW + player.getDisplayName(false) + " has destroyed the group " + group.getName() + ".");
	}

	public void invitePlayer(StandardPlayer player, String invitedUsername) {
		Group group = usernameToGroupMap.get(player.getName());
		
		if (group == null) {
			player.sendMessage("You must be in a group before you can invite players.");
			return;
		}
		
		StandardPlayer invitedPlayer = plugin.matchPlayer(invitedUsername);
		
		if (invitedPlayer == null) {
			player.sendMessage("That player doesn't exist.");
			return;
		}
		
		if (group.isInvited(invitedPlayer.getName())) {
			player.sendMessage("That player has already been invited to your group.");
			return;
		}
		
		if (group.isMember(invitedPlayer.getName())) {
			player.sendMessage("That player is already a member of your group.");
			return;
		}

		group.invite(invitedPlayer.getName());

		for (StandardPlayer other : group.getPlayers()) {
			if (player == other) {
				player.sendMessage(ChatColor.YELLOW + "You have invited " + invitedPlayer.getDisplayName(false) + " to join your group.");
			} else if (other.isOnline()) {
				other.sendMessage(ChatColor.YELLOW + player.getDisplayName(false) + " has invited " + invitedPlayer.getDisplayName(false) + " to join your group.");
			}
		}
		
		invitedPlayer.sendMessage(ChatColor.YELLOW + player.getDisplayName(false) + " has invited you to join their group " + group.getName() + ".");
	}

	public void joinGroup(StandardPlayer player, String groupName) {
		if (usernameToGroupMap.containsKey(player.getName())) {
			player.sendMessage("You must leave your existing group first before joining a different one.");
			return;
		}

		Group group = storage.getGroupByName(groupName);
		
		if (group == null) {
			player.sendMessage("That group does not exist.");
			return;
		}
		
		if (!group.isInvited(player.getName())) {
			for (StandardPlayer other : group.getPlayers()) {
				if (other.isOnline()) {
					other.sendMessage(ChatColor.YELLOW + player.getDisplayName(false) + " wants to join your group. Invite them by typing /g invite " + player.getDisplayName(false));
				}
			}
			
			player.sendMessage("You have not been invited to join this group yet.");
			return;
		}

		for (StandardPlayer other : group.getPlayers()) {
			if (other.isOnline()) {
				other.sendMessage(ChatColor.YELLOW + player.getDisplayName(false) + " has joined your group.");
			}
		}
		
		usernameToGroupMap.put(player.getName(), group);
		
		group.addMember(player);
		group.removeInvite(player.getName());
		
		player.sendMessage(ChatColor.YELLOW + "You have successfully joined the group " + group.getName() + ".");
	}

	public void leaveGroup(StandardPlayer player) {
		Group group = usernameToGroupMap.get(player.getName());
		
		if (group == null) {
			player.sendMessage("You can't leave a group if you aren't in one.");
			return;
		}
		
		usernameToGroupMap.remove(player.getName());
		
		group.removeMember(player);
		
		if (group.getMembers().size() > 0) {
			for (StandardPlayer other : group.getPlayers()) {
				if (other.isOnline()) {
					other.sendMessage(ChatColor.YELLOW + player.getDisplayName(false) + " has left your group.");
				}
			}
			
			player.sendMessage(ChatColor.YELLOW + "You have left the group " + group.getName() + ".");
		} else {
			storage.destroyGroup(group);
			
			StandardPlugin.broadcast(ChatColor.YELLOW + player.getDisplayName(false) + " has destroyed the group " + group.getName() + ".");
		}
	}

	public void claim(StandardPlayer player) {
		Group group = usernameToGroupMap.get(player.getName());
		
		if (group == null) {
			player.sendMessage("You must be in a group before you can claim land.");
			return;
		}
		
		Group testGroup = locationToGroupMap.get(Claim.getLocationKey(player.getLocation()));
		
		if (testGroup == group) {
			player.sendMessage("You already own this land.");
			return;
		}
		
		if (testGroup != null) {
			player.sendMessage("This land is already claimed.");
			return;
		}
		
		if (group.getClaims().size() >= group.getMaxClaims()) {
			player.sendMessage("Your group cannot claim any more land at the moment.");
			return;
		}
		
		Claim claim = group.claim(player, player.getLocation());
		
		locationToGroupMap.put(claim.getLocationKey(), group);
		locationToClaimMap.put(claim.getLocationKey(), claim);
		
		for (StandardPlayer other : group.getPlayers()) {
			if (player == other) {
				player.sendMessage(ChatColor.YELLOW + "Land claimed.");
			} else if (other.isOnline()) {
				other.sendMessage(ChatColor.YELLOW + player.getDisplayName(false) + " has claimed land at " + claim.getX() + ", " + claim.getZ() + ".");
			}
		}
	}

	public void unclaim(StandardPlayer player) {
		Group group = usernameToGroupMap.get(player.getName());
		
		if (group == null) {
			player.sendMessage("You must be in a group before you can unclaim land.");
			return;
		}

		Claim claim = locationToClaimMap.get(Claim.getLocationKey(player.getLocation()));
		
		if (claim == null || claim.getGroup() != group) {
			player.sendMessage("You don't own this land.");
			return;
		}
		
		group.unclaim(claim);
		
		locationToGroupMap.remove(claim.getLocationKey());
		locationToClaimMap.remove(claim.getLocationKey());
		
		for (StandardPlayer other : group.getPlayers()) {
			if (player == other) {
				player.sendMessage(ChatColor.YELLOW + "Land unclaimed.");
			} else if (other.isOnline()) {
				other.sendMessage(ChatColor.YELLOW + player.getDisplayName(false) + " has unclaimed land at " + claim.getX() + ", " + claim.getZ() + ".");
			}
		}
	}

}
