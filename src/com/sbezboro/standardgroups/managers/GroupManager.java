package com.sbezboro.standardgroups.managers;

import java.util.*;
import java.util.regex.Pattern;

import com.sbezboro.standardgroups.model.Lock;
import com.sbezboro.standardgroups.tasks.GroupRemovalTask;
import com.sbezboro.standardgroups.tasks.LandGrowthCheckTask;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.model.Claim;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardgroups.persistence.storages.GroupStorage;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.managers.BaseManager;
import com.sbezboro.standardplugin.model.StandardPlayer;
import com.sbezboro.standardplugin.util.MiscUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.material.Bed;
import org.bukkit.material.TrapDoor;

public class GroupManager extends BaseManager {

	@SuppressWarnings("serial")
	private static final HashSet<Material> PROTECTED_BLOCKS = new HashSet<Material>() {{
		add(Material.CHEST);
		add(Material.WOODEN_DOOR);
		add(Material.FENCE_GATE);
		add(Material.TRAP_DOOR);
		add(Material.ENDER_CHEST);
		add(Material.HOPPER);
		add(Material.FURNACE);
		add(Material.BURNING_FURNACE);
		add(Material.DISPENSER);
		add(Material.DROPPER);
		add(Material.ENCHANTMENT_TABLE);
		add(Material.TRAPPED_CHEST);
		add(Material.JUKEBOX);
		add(Material.BREWING_STAND);
		add(Material.DRAGON_EGG);
		add(Material.NOTE_BLOCK);
		add(Material.CAULDRON);
		add(Material.REDSTONE_COMPARATOR);
		add(Material.REDSTONE_COMPARATOR_OFF);
		add(Material.REDSTONE_COMPARATOR_ON);
		add(Material.DIODE);
		add(Material.DIODE_BLOCK_OFF);
		add(Material.DIODE_BLOCK_ON);
		add(Material.BEACON);
		add(Material.BED_BLOCK);
	}};

	@SuppressWarnings("serial")
	private static final HashSet<EntityType> PROTECTED_ENTITIES = new HashSet<EntityType>() {{
		add(EntityType.ITEM_FRAME);
	}};

	private final Pattern groupNamePat = Pattern.compile("^[a-zA-Z_]*$");
	private final String groupNamePatExplanation = "Group names can only contain letters and underscores.";

	private StandardGroups subPlugin;
	
	private GroupStorage storage;
	
	private Map<String, Group> usernameToGroupMap;
	private Map<String, Group> locationToGroupMap;

	private LandGrowthCheckTask landGrowthCheckTask;
	private GroupRemovalTask groupRemovalTask;

	public GroupManager(StandardPlugin plugin, StandardGroups subPlugin, GroupStorage storage) {
		super(plugin);

		this.subPlugin = subPlugin;
		
		this.storage = storage;
		this.storage.loadObjects();
		
		usernameToGroupMap = new HashMap<String, Group>();
		locationToGroupMap = new HashMap<String, Group>();

		landGrowthCheckTask = new LandGrowthCheckTask(plugin, subPlugin);
		landGrowthCheckTask.runTaskTimer(subPlugin, 1200, 12000);

		groupRemovalTask = new GroupRemovalTask(plugin, subPlugin);
		groupRemovalTask.runTaskTimer(subPlugin, 2400, 24000);
		
		for (Group group : storage.getGroups()) {
			for (String username : group.getMembers()) {
				if (usernameToGroupMap.containsKey(username)) {
					plugin.getLogger().severe("Duplicate member for " + group.getName() + " - " + username);
				}

				usernameToGroupMap.put(username, group);
			}
			
			for (Claim claim : group.getClaims()) {
				locationToGroupMap.put(claim.getLocationKey(), group);
			}
		}
	}

	public Group getSafearea() {
		return storage.getGroupByName(Group.SAFE_AREA);
	}

	public List<Group> getGroups() {
		List<Group> groups = storage.getGroups();
		groups.remove(getSafearea());
		return groups;
	}

	public Group getGroupByName(String name) {
		Group group = storage.getGroupByName(name);
		if (group != null) {
			return group;
		}

		for (Group otherGroup : storage.getGroups()) {
			if (otherGroup.getName().equalsIgnoreCase(name)) {
				return otherGroup;
			}
		}

		return null;
	}
	
	public Group getGroupByLocation(Location location) {
		return locationToGroupMap.get(Claim.getLocationKey(location));
	}
	
	public Group getPlayerGroup(StandardPlayer player) {
		return usernameToGroupMap.get(player.getName());
	}
	
	public boolean playerInGroup(StandardPlayer player, Group group) {
		return getPlayerGroup(player) == group;
	}

	public Group matchGroup(String name) {
		for (Group group : storage.getGroups()) {
			if (group.getName().toLowerCase().startsWith(name.toLowerCase())) {
				return group;
			}
		}

		return null;
	}

	public static boolean isBlockTypeProtected(Block block) {
		return PROTECTED_BLOCKS.contains(block.getType());
	}

	public static boolean isEntityTypeProtected(Entity entity) {
		return PROTECTED_ENTITIES.contains(entity.getType());
	}

	public boolean isGroupsAdmin(StandardPlayer player) {
		return player.hasPermission("standardgroups.groups.admin");
	}

	public String getGroupIdentifier(StandardPlayer player) {
		Group group = getPlayerGroup(player);

		if (group != null) {
			if (group.isLeader(player)) {
				return "[L] ";
			}
			if (group.isModerator(player)) {
				return "[M] ";
			}
			return "[G] ";
		}

		return "";
	}

	private Block[] getAdjacentBlocks(Block block) {
		return new Block[] {
				block.getRelative(BlockFace.NORTH),
				block.getRelative(BlockFace.EAST),
				block.getRelative(BlockFace.SOUTH),
				block.getRelative(BlockFace.WEST)
		};
	}

	public List<Lock> getLocksAffectedByBlock(Location location) {
		Group group = getGroupByLocation(location);
		return getLocksAffectedByBlock(group, location);
	}

	public List<Lock> getLocksAffectedByBlock(Group group, Location location) {
		ArrayList<Block> affectedBlocks = new ArrayList<Block>();
		ArrayList<Lock> affectedLocks = new ArrayList<Lock>();

		if (group == null) {
			return affectedLocks;
		}

		Block targetBlock = location.getBlock();

		Block aboveBlock = targetBlock.getRelative(BlockFace.UP);
		Block belowBlock = targetBlock.getRelative(BlockFace.DOWN);

		Lock lock = group.getLock(targetBlock.getLocation());

		// Check for surrounding block locks that may be affected by the target block:
		// 1. Blocks above for potential doors
		// 2. Blocks adjacent for potential double chests
		// 3. Blocks adjacent for potential beds
		// 4. Blocks adjacent for potential trap doors
		// 5. Block above for potential dragon egg
		if (lock == null) {
			Block[] adjacentBlocks = getAdjacentBlocks(targetBlock);

			if (targetBlock.getType() == Material.WOODEN_DOOR) {
				if (aboveBlock.getType() == Material.WOODEN_DOOR) {
					affectedBlocks.add(aboveBlock);
				} else if (belowBlock.getType() == Material.WOODEN_DOOR) {
					affectedBlocks.add(belowBlock);
				}
			} else if (targetBlock.getType() == Material.CHEST) {
				for (Block block : adjacentBlocks) {
					if (block.getType() == Material.CHEST) {
						affectedBlocks.add(block);
					}
				}
			} else if (targetBlock.getType() == Material.BED_BLOCK) {
				Bed bed = (Bed) targetBlock.getState().getData();

				if (bed.isHeadOfBed()) {
					affectedBlocks.add(targetBlock.getRelative(bed.getFacing().getOppositeFace()));
				} else {
					affectedBlocks.add(targetBlock.getRelative(bed.getFacing()));
				}
			} else {
				for (Block block : adjacentBlocks) {
					if (block.getType() == Material.TRAP_DOOR) {
						TrapDoor trapDoor = (TrapDoor) block.getState().getData();

						if (trapDoor.getAttachedFace() == block.getFace(targetBlock)) {
							affectedBlocks.add(block);
						}
					}
				}

				if (aboveBlock.getType() == Material.WOODEN_DOOR) {
					affectedBlocks.add(aboveBlock);
					affectedBlocks.add(aboveBlock.getRelative(BlockFace.UP));
				} else if (aboveBlock.getType() == Material.DRAGON_EGG) {
					affectedBlocks.add(aboveBlock);
				}
			}

			for (Block block : affectedBlocks) {
				Lock otherLock = group.getLock(block.getLocation());
				if (otherLock != null) {
					affectedLocks.add(otherLock);
				}
			}
		} else {
			affectedLocks.add(lock);
		}

		return affectedLocks;
	}

	public boolean isOwnerOfAllLocks(StandardPlayer player, List<Lock> locks) {
		for (Lock lock : locks) {
			if (!lock.isOwner(player)) {
				return false;
			}
		}

		return true;
	}

	public boolean hasAccessToAllLocks(StandardPlayer player, List<Lock> locks) {
		for (Lock lock : locks) {
			if (!lock.hasAccess(player)) {
				return false;
			}
		}

		return true;
	}
	
	public void createGroup(StandardPlayer player, String groupName) {
		if (getPlayerGroup(player) != null) {
			player.sendMessage("You must leave your existing group first before creating a new one.");
			return;
		}
		
		if (getGroupByName(groupName) != null) {
			player.sendMessage("That group name is already taken");
			return;
		}
		
		if (!groupNamePat.matcher(groupName).matches()) {
			player.sendMessage(groupNamePatExplanation);
			return;
		}
		
		int minLength = subPlugin.getGroupNameMinLength();
		int maxLength = subPlugin.getGroupNameMaxLength();
		
		if (groupName.length() < minLength || groupName.length() > maxLength) {
			player.sendMessage("The group name must be between " + minLength + " and " + maxLength + " characters long.");
			return;
		}
		
		Group group = storage.createGroup(groupName, player);
		usernameToGroupMap.put(player.getName(), group);
		
		StandardPlugin.broadcast(ChatColor.YELLOW + player.getDisplayName(false) + " has created a new group.");
	}
	
	public void destroyGroup(CommandSender sender, String groupName) {
		Group group;

		StandardPlayer player = plugin.getStandardPlayer(sender);
		
		// No group name means destroying own group
		if (groupName == null) {
			group = getPlayerGroup(player);
			
			if (group == null) {
				player.sendMessage("You can't destroy a group if you aren't in one.");
				return;
			}

			if (!group.isLeader(player)) {
				player.sendMessage("You can only destroy a group if you are the leader.");
				return;
			}
		// Trying to destroy another group by name
		} else {
			// Check if console or admin player
			if (player == null || isGroupsAdmin(player)) {
				group = matchGroup(groupName);
				
				if (group == null) {
					sender.sendMessage("That group doesn't exist.");
					return;
				}
			} else {
				player.sendMessage(subPlugin.getServer().getPluginCommand("groups").getPermissionMessage());
				return;
			}
		}

		for (String username : group.getMembers()) {
			usernameToGroupMap.remove(username);
		}
		
		for (Claim claim : group.getClaims()) {
			locationToGroupMap.remove(claim.getLocationKey());
		}
		
		storage.destroyGroup(group);
		
		if (player == null) {
			StandardPlugin.broadcast(ChatColor.YELLOW + "A server admin has destroyed the group " + group.getName() + ".");
		} else {
			StandardPlugin.broadcast(ChatColor.YELLOW + player.getDisplayName(false) + " has destroyed their group.");
		}
	}

	public void setLeader(StandardPlayer player, String username) {
		Group group = getPlayerGroup(player);

		if (group == null) {
			player.sendMessage("You must be in a group before you can invite players.");
			return;
		}

		if (!group.isLeader(player)) {
			player.sendMessage("Only the group leader can designate another leader.");
			return;
		}

		StandardPlayer leaderPlayer = plugin.matchPlayer(username);

		if (leaderPlayer == null) {
			player.sendMessage("That player doesn't exist.");
			return;
		}

		if (!group.isMember(leaderPlayer)) {
			player.sendMessage("That player isn't part of your group.");
			return;
		}

		if (player == leaderPlayer) {
			player.sendMessage("You are already the leader.");
			return;
		}

		group.setLeader(leaderPlayer);

		for (StandardPlayer other : group.getPlayers()) {
			if (!other.isOnline()) {
				continue;
			}

			if (player == other) {
				player.sendMessage(ChatColor.YELLOW + "You have given leadership to " + leaderPlayer.getDisplayName(false) + ".");
			} else if (other == leaderPlayer) {
				leaderPlayer.sendMessage(ChatColor.YELLOW + player.getDisplayName(false) + " has given you leadership of the group.");
			} else {
				other.sendMessage(ChatColor.YELLOW + player.getDisplayName(false) + " has given leadership to " + leaderPlayer.getDisplayName(false) + ".");
			}
		}
	}

	public void invitePlayer(StandardPlayer player, String invitedUsername) {
		Group group = getPlayerGroup(player);
		
		if (group == null) {
			player.sendMessage("You must be in a group before you can invite players.");
			return;
		}
		
		StandardPlayer invitedPlayer = plugin.matchPlayer(invitedUsername);
		
		if (invitedPlayer == null) {
			player.sendMessage("That player doesn't exist.");
			return;
		}

		if (!group.isModerator(player) && !group.isLeader(player)) {
			player.sendMessage("Only the group leader or a moderator can invite players.");
			return;
		}
		
		if (group.isInvited(invitedPlayer)) {
			player.sendMessage("That player has already been invited to your group.");
			return;
		}
		
		if (group.isMember(invitedPlayer)) {
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

		if (invitedPlayer.isOnline()) {
			invitedPlayer.sendMessage(ChatColor.YELLOW + player.getDisplayName(false) + " has invited you to join their group " + group.getName() + ". To join, type /g join " + group.getName());
		}
	}

	public void uninvitePlayer(StandardPlayer player, String uninvitedUsername) {
		Group group = getPlayerGroup(player);

		if (group == null) {
			player.sendMessage("You can't uninvite players if you aren't in a group.");
			return;
		}

		StandardPlayer uninvitedPlayer = plugin.matchPlayer(uninvitedUsername);

		if (!group.isModerator(player) && !group.isLeader(player)) {
			player.sendMessage("You must be either the group leader or a moderator to be able to uninvite players.");
			return;
		}

		if (uninvitedPlayer == null) {
			player.sendMessage("That player doesn't exist.");
			return;
		}

		if (!group.isInvited(uninvitedPlayer)) {
			player.sendMessage("That player hasn't been invited to your group yet.");
			return;
		}

		group.removeInvite(uninvitedPlayer.getName());

		for (StandardPlayer other : group.getPlayers()) {
			if (player == other) {
				player.sendMessage(ChatColor.YELLOW + "You have revoked the invitation for " + uninvitedPlayer.getDisplayName(false) + ".");
			} else if (other.isOnline()) {
				other.sendMessage(ChatColor.YELLOW + player.getDisplayName(false) + " has revoked the invitation for " + uninvitedPlayer.getDisplayName(false) + ".");
			}
		}

		if (uninvitedPlayer.isOnline()) {
			uninvitedPlayer.sendMessage(ChatColor.YELLOW + player.getDisplayName(false) + " has revoked your invitation to the group " + group.getName() + "!");
		}
	}

	public void autoKickPlayer(StandardPlayer kickedPlayer) {
		Group group = getPlayerGroup(kickedPlayer);

		if (group.isLeader(kickedPlayer) && group.getMembers().size() > 1) {
			StandardPlayer newLeader;

			if (!group.getModerators().isEmpty()) {
				newLeader = plugin.getStandardPlayer(group.getModerators().get(0));
			} else {
				List<StandardPlayer> players = group.getPlayers();

				if (players.get(0) == kickedPlayer) {
					newLeader = players.get(1);
				} else {
					newLeader = players.get(0);
				}
			}

			group.setLeader(newLeader);

			if (newLeader.isOnline()) {
				newLeader.sendMessage(ChatColor.YELLOW + "You have been designated as the new group leader.");
			}
		}

		group.removeMember(kickedPlayer);
		usernameToGroupMap.remove(kickedPlayer.getName());

		if (group.getMembers().size() == 0) {
			for (Claim claim : group.getClaims()) {
				locationToGroupMap.remove(claim.getLocationKey());
			}

			storage.destroyGroup(group);

			StandardPlugin.broadcast(ChatColor.YELLOW + "The group " + group.getName() + " has been destroyed automatically.");
		}
	}

	public void kickPlayer(StandardPlayer player, String kickedUsername) {
		Group group = getPlayerGroup(player);

		if (group == null) {
			player.sendMessage("You can't kick members from a group if you aren't in one.");
			return;
		}

		StandardPlayer kickedPlayer = plugin.matchPlayer(kickedUsername);

		if (!group.isModerator(player) && !group.isLeader(player)) {
			player.sendMessage("You must be either the group leader or a moderator to be able to kick members.");
			return;
		}

		if (kickedPlayer == null) {
			player.sendMessage("That player doesn't exist.");
			return;
		}

		if (!group.isMember(kickedPlayer)) {
			player.sendMessage("That player isn't part of your group.");
			return;
		}

		if (player == kickedPlayer) {
			player.sendMessage("You cannot kick yourself.");
			return;
		}

		if (group.isLeader(kickedPlayer)) {
			player.sendMessage("You cannot kick the group leader.");
			return;
		}

		if (group.isModerator(player) && group.isModerator(kickedPlayer)) {
			player.sendMessage("Only the group leader can kick a moderator.");
			return;
		}

		group.removeMember(kickedPlayer);
		usernameToGroupMap.remove(kickedPlayer.getName());

		for (StandardPlayer other : group.getPlayers()) {
			if (player == other) {
				player.sendMessage(ChatColor.YELLOW + "You have kicked " + kickedPlayer.getDisplayName(false) + " from your group.");
			} else if (other.isOnline()) {
				other.sendMessage(ChatColor.YELLOW + player.getDisplayName(false) + " has kicked " + kickedPlayer.getDisplayName(false) + " from your group.");
			}
		}

		if (kickedPlayer.isOnline()) {
			kickedPlayer.sendMessage(ChatColor.YELLOW + player.getDisplayName(false) + " has kicked you from the group " + group.getName() + "!");
		}
	}

	public void joinGroup(StandardPlayer player, String usernameOrGroup) {
		if (getPlayerGroup(player) != null) {
			player.sendMessage("You must leave your existing group first before joining a different one.");
			return;
		}

		Group group = matchGroup(usernameOrGroup);

		if (group == null) {
			StandardPlayer other = plugin.matchPlayer(usernameOrGroup);
			if (other == null) {
				player.sendMessage("No group or player by that name.");
				return;
			} else {
				group = getPlayerGroup(other);

				if (group == null) {
					player.sendMessage("The player " + other.getDisplayName(false) + " is not in a group.");
					return;
				}
			}
		}
		
		if (!group.isInvited(player)) {
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
		Group group = getPlayerGroup(player);
		
		if (group == null) {
			player.sendMessage("You can't leave a group if you aren't in one.");
			return;
		}

		if (group.isLeader(player) && group.getMembers().size() > 1) {
			StandardPlayer newLeader;

			if (!group.getModerators().isEmpty()) {
				newLeader = plugin.getStandardPlayer(group.getModerators().get(0));
			} else {
				List<StandardPlayer> players = group.getPlayers();

				if (players.get(0) == player) {
					newLeader = players.get(1);
				} else {
					newLeader = players.get(0);
				}
			}

			group.setLeader(newLeader);

			if (newLeader.isOnline()) {
				newLeader.sendMessage(ChatColor.YELLOW + "You have been designated as the new group leader.");
			}
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
			for (Claim claim : group.getClaims()) {
				locationToGroupMap.remove(claim.getLocationKey());
			}
			
			storage.destroyGroup(group);

			StandardPlugin.broadcast(ChatColor.YELLOW + player.getDisplayName(false) + " has destroyed their group.");
		}
	}

	private void claim(StandardPlayer player, Group group, Location location) {
		Group testGroup = getGroupByLocation(location);

		if (testGroup == group) {
			player.sendMessage("You already own this land.");
			return;
		}

		if (group.getClaims().size() >= group.getMaxClaims()) {
			player.sendMessage("Your group cannot claim any more land at the moment.");
			return;
		}

		if (testGroup != null) {
			// Admins can overclaim if necessary
			if (isGroupsAdmin(player)) {
				Claim claim = testGroup.getClaim(player.getLocation());
				testGroup.unclaim(claim);
				locationToGroupMap.remove(claim.getLocationKey());
			} else {
				player.sendMessage("This land is already claimed.");
				return;
			}
		}

		Claim claim = group.claim(player, location);
		locationToGroupMap.put(claim.getLocationKey(), group);

		for (StandardPlayer other : group.getPlayers()) {
			if (player == other) {
				player.sendMessage(ChatColor.YELLOW + "Land claimed.");
			} else if (other.isOnline()) {
				other.sendMessage(ChatColor.YELLOW + player.getDisplayName(false) + " has claimed land at " + claim.getX() + ", " + claim.getZ() + ".");
			}
		}

		if (isGroupsAdmin(player)) {
			player.sendMessage(ChatColor.YELLOW + "Land claimed for " + group.getName());
		}
	}

	public void claim(StandardPlayer player, String groupName, int width) {
		Group group;

		// No group name means claiming for own group
		if (groupName == null) {
			group = getPlayerGroup(player);

			if (group == null) {
				player.sendMessage("You must be in a group before you can claim land.");
				return;
			}

			if (!group.isModerator(player) && !group.isLeader(player)) {
				player.sendMessage("Only the group leader or a moderator can claim land.");
				return;
			}
		// Trying to claim for another group
		} else {
			// Check if admin player
			if (isGroupsAdmin(player)) {
				group = matchGroup(groupName);

				if (group == null) {
					player.sendMessage("That group doesn't exist.");
					return;
				}
			} else {
				player.sendMessage(subPlugin.getServer().getPluginCommand("groups").getPermissionMessage());
				return;
			}
		}

		Location location;
		Location playerLocation = player.getLocation();

		int x = 0;
		int z = 0;
		int dx = 0;
		int dz = -1;

		boolean hit = false;
		int limit = 1;
		int start = 0;

		// Claim in a spiral
		for (int i = 0; i < width * width; ++i) {
			location = new Location(playerLocation.getWorld(),
					playerLocation.getBlockX() + (x << 4),
					playerLocation.getBlockY(),
					playerLocation.getBlockZ() + (z << 4));

			claim(player, group, location);

			if (i - start == limit) {
				start = i;

				if (hit) {
					limit++;
					hit = false;
				} else {
					hit = true;
				}

				int t = dx;
				dx = -dz;
				dz = t;
			}

			x += dx;
			z += dz;
		}
	}

	private void unclaim(StandardPlayer player, Location location) {
		Group group;

		if (isGroupsAdmin(player)) {
			group = getGroupByLocation(location);

			if (group == null) {
				player.sendMessage("No group owns this land.");
				return;
			}
		} else {
			group = getPlayerGroup(player);

			if (group == null) {
				player.sendMessage("You must be in a group before you can unclaim land.");
				return;
			}

			if (!group.isModerator(player) && !group.isLeader(player)) {
				player.sendMessage("Only the group leader or a moderator can unclaim land.");
				return;
			}

			if (getGroupByLocation(location) != group) {
				if (location == player.getLocation()) {
					player.sendMessage("You don't own this land.");
				} else {
					player.sendMessage("You don't own that chunk.");
				}

				return;
			}
		}

		Claim claim = group.getClaim(location);
		group.unclaim(claim);
		locationToGroupMap.remove(claim.getLocationKey());

		for (StandardPlayer other : group.getPlayers()) {
			if (player == other) {
				player.sendMessage(ChatColor.YELLOW + "Land unclaimed.");
			} else if (other.isOnline()) {
				other.sendMessage(ChatColor.YELLOW + player.getDisplayName(false) + " has unclaimed land at " + claim.getX() + ", " + claim.getZ() + ".");
			}
		}

		if (isGroupsAdmin(player)) {
			player.sendMessage(ChatColor.YELLOW + "Land unclaimed from " + group.getName());
		}
	}

	public void unclaim(StandardPlayer player) {
		unclaim(player, player.getLocation());
	}

	public void unclaim(StandardPlayer player, int x, int z) {
		Location location = new Location(player.getWorld(), x << 4, 0, z << 4);

		unclaim(player, location);
	}

	public void unclaimAll(StandardPlayer player) {
		Group group = getPlayerGroup(player);

		if (group == null) {
			player.sendMessage("You must be in a group before you can unclaim land.");
			return;
		}

		if (!group.isLeader(player)) {
			player.sendMessage("Only the group leader can unclaim all land.");
			return;
		}

		if (group.getClaims().isEmpty()) {
			player.sendMessage(ChatColor.YELLOW + "You don't own any land to unclaim.");
		} else {
			for (Claim claim : new ArrayList<Claim>(group.getClaims())) {
				group.unclaim(claim);
				locationToGroupMap.remove(claim.getLocationKey());
			}

			for (StandardPlayer other : group.getPlayers()) {
				if (player == other) {
					player.sendMessage(ChatColor.YELLOW + "All land unclaimed.");
				} else if (other.isOnline()) {
					other.sendMessage(ChatColor.YELLOW + player.getDisplayName(false) + " has unclaimed all of your group's land.");
				}
			}
		}
	}

	public void rename(StandardPlayer player, String name) {
		Group group = getPlayerGroup(player);
		
		if (group == null) {
			player.sendMessage("You must be in a group before you can rename one.");
			return;
		}
		
		if (name.equals(group.getName())) {
			player.sendMessage("Your group is already named that.");
			return;
		}
		
		if (getGroupByName(name) != null) {
			player.sendMessage("That group name is already taken");
			return;
		}
		
		if (!groupNamePat.matcher(name).matches()) {
			player.sendMessage(groupNamePatExplanation);
			return;
		}
		
		int minLength = subPlugin.getGroupNameMinLength();
		int maxLength = subPlugin.getGroupNameMaxLength();
		
		if (name.length() < minLength || name.length() > maxLength) {
			player.sendMessage("The group name must be between " + minLength + " and " + maxLength + " characters long.");
			return;
		}

		group.rename(name);
		
		for (StandardPlayer other : group.getPlayers()) {
			if (player == other) {
				player.sendMessage(ChatColor.YELLOW + "Group renamed.");
			} else if (other.isOnline()) {
				other.sendMessage(ChatColor.YELLOW + player.getDisplayName(false) + " has renamed the group to " + name + ".");
			}
		}
	}

	public void groupInfo(CommandSender sender, String usernameOrGroup) {
		StandardPlayer player = plugin.getStandardPlayer(sender);
		Group group;
		
		if (usernameOrGroup == null) {
			group = getPlayerGroup(player);
			
			if (group == null) {
				player.sendMessage("You must be in a group before you can use that command.");
				return;
			}
		} else {
			group = matchGroup(usernameOrGroup);

			if (group != null && group.isSafearea()) {
				group = null;
			}
			
			if (group == null) {
				StandardPlayer other = plugin.matchPlayer(usernameOrGroup);
				if (other == null) {
					sender.sendMessage("No group or player by that name.");
					return;
				} else {
					group = getPlayerGroup(other);

					if (group == null) {
						sender.sendMessage("The player " + other.getDisplayName(false) + " is not in a group.");
						return;
					}
				}
			}
		}

		ArrayList<String> onlineMembers = new ArrayList<String>();
		ArrayList<String> offlineMembers = new ArrayList<String>();

		for (StandardPlayer member : group.getPlayers()) {
			if (member.isOnline()) {
				onlineMembers.add(getGroupIdentifier(member) + member.getDisplayName());
			} else {
				offlineMembers.add(getGroupIdentifier(member) + member.getDisplayName());
			}
		}

		sender.sendMessage(ChatColor.GOLD + "============== " + ChatColor.YELLOW + "Group: " + group.getNameWithRelation(player) + ChatColor.GOLD + " ==============");
		sender.sendMessage(ChatColor.YELLOW + "Established: " + ChatColor.RESET + MiscUtil.friendlyTimestamp(group.getEstablished()));
		sender.sendMessage(ChatColor.YELLOW + "Land count: " + ChatColor.RESET + group.getClaims().size());
		sender.sendMessage(ChatColor.YELLOW + "Land limit: " + ChatColor.RESET + group.getMaxClaims());
		sender.sendMessage(ChatColor.YELLOW + "Online members: " + ChatColor.RESET + StringUtils.join(onlineMembers, ChatColor.RESET + ", "));
		sender.sendMessage(ChatColor.YELLOW + "Offline members: " + ChatColor.RESET + StringUtils.join(offlineMembers, ChatColor.RESET + ", "));
	}

	public void lock(StandardPlayer player, Block block) {
		Group group = getPlayerGroup(player);

		if (group == null) {
			player.sendMessage("You must be in a group before you can lock things.");
			return;
		}

		if (!PROTECTED_BLOCKS.contains(block.getType())) {
			player.sendMessage("This block isn't lockable.");
			return;
		}

		Location location = block.getLocation();

		Group testGroup = getGroupByLocation(location);

		if (testGroup != group) {
			player.sendMessage("You can only lock things in your group's territory.");
			return;
		}

		List<Lock> locks = getLocksAffectedByBlock(group, location);

		if (!locks.isEmpty()) {
			player.sendMessage("A lock already exists on this block.");
			return;
		}

		group.lock(player, location);

		player.sendMessage(ChatColor.YELLOW + "You have locked this block for yourself.");
	}

	public void unlock(StandardPlayer player, Block block) {
		Group group = getPlayerGroup(player);

		if (group == null) {
			player.sendMessage("You must be in a group before you can unlock things.");
			return;
		}

		Location location = block.getLocation();

		Group testGroup = getGroupByLocation(location);

		if (testGroup != group) {
			player.sendMessage("You can only unlock things in your group's territory.");
			return;
		}

		if (!PROTECTED_BLOCKS.contains(block.getType())) {
			player.sendMessage("This block isn't lockable.");
			return;
		}

		List<Lock> locks = getLocksAffectedByBlock(group, location);

		if (locks.isEmpty()) {
			player.sendMessage("No lock exists on this block.");
			return;
		}

		Lock lock = locks.get(0);

		if (!lock.isOwner(player)) {
			if (group.isLeader(player)) {
				if (getPlayerGroup(lock.getOwner()) == group) {
					player.sendMessage("The player who owns this lock is still in your group.");
					return;
				}
			} else {
				player.sendMessage("You are not the owner of this lock.");
				return;
			}
		}

		group.unlock(lock);

		player.sendMessage(ChatColor.YELLOW + "You have released the lock on this block.");
	}

	public void addLockMember(StandardPlayer player, Block block, StandardPlayer otherPlayer) {
		Group group = getPlayerGroup(player);

		if (group == null) {
			player.sendMessage("You must be in a group before you can add a member to a lock.");
			return;
		}

		if (group != getPlayerGroup(otherPlayer)) {
			player.sendMessage("That player isn't part of your group.");
			return;
		}

		Location location = block.getLocation();

		List<Lock> locks = getLocksAffectedByBlock(group, location);

		if (locks.isEmpty()) {
			player.sendMessage("No lock exists on this block.");
			return;
		}

		Lock lock = locks.get(0);

		if (!lock.isOwner(player)) {
			player.sendMessage("You are not the owner of this lock.");
			return;
		}

		if (lock.hasAccess(otherPlayer)) {
			player.sendMessage("That player already has access.");
			return;
		}

		group.addLockMember(lock, otherPlayer);

		player.sendMessage(ChatColor.YELLOW + "You have given " + otherPlayer.getDisplayName(false) + " access to this lock.");
	}

	public void removeLockMember(StandardPlayer player, Block block, StandardPlayer otherPlayer) {
		Group group = getPlayerGroup(player);

		if (group == null) {
			player.sendMessage("You must be in a group before you revoke access to locks.");
			return;
		}

		Location location = block.getLocation();

		List<Lock> locks = getLocksAffectedByBlock(group, location);

		if (locks.isEmpty()) {
			player.sendMessage("No lock exists on this block.");
			return;
		}

		Lock lock = locks.get(0);

		if (!lock.isOwner(player) && !group.isLeader(player)) {
			player.sendMessage("You are not the owner of this lock.");
			return;
		}

		if (!lock.hasAccess(otherPlayer)) {
			player.sendMessage("That player already doesn't have access.");
			return;
		}

		if (lock.isOwner(otherPlayer)) {
			unlock(player, block);
			return;
		}

		group.removeLockMember(lock, otherPlayer);

		player.sendMessage(ChatColor.YELLOW + "You have revoked access to this lock from " + otherPlayer.getDisplayName(false) + ".");
	}

	public void lockInfo(StandardPlayer player, Block block) {
		Location location = block.getLocation();

		List<Lock> locks = getLocksAffectedByBlock(location);

		if (locks.isEmpty()) {
			player.sendMessage("No lock exists on this block.");
			return;
		}

		Lock lock = locks.get(0);

		String members = "";
		String delim = "";
		for (StandardPlayer member : lock.getMembers()) {
			if (member != lock.getOwner()) {
				members += delim + member.getDisplayName() + ChatColor.RESET;
				delim = ", ";
			}
		}

		player.sendMessage(ChatColor.GOLD + "============== " + ChatColor.YELLOW + "Lock Info" + ChatColor.GOLD + " ==============");
		player.sendMessage(ChatColor.YELLOW + "Location: " + ChatColor.RESET + MiscUtil.locationFormat(lock.getLocation()));
		player.sendMessage(ChatColor.YELLOW + "Owner: " + ChatColor.RESET + lock.getOwner().getDisplayName());
		player.sendMessage(ChatColor.YELLOW + "Public: " + ChatColor.RESET + (lock.isPublic() ? "Yes" : "No"));

		if (!members.isEmpty()) {
			player.sendMessage(ChatColor.YELLOW + "Members: " + ChatColor.RESET + members);
		}
	}

	public void togglePublicLock(StandardPlayer player, Block block) {
		Group group = getPlayerGroup(player);

		if (group == null) {
			player.sendMessage("You must be in a group before you can set lock status.");
			return;
		}

		Location location = block.getLocation();

		Group testGroup = getGroupByLocation(location);

		if (testGroup != group) {
			player.sendMessage("You can only lock things in your group's territory.");
			return;
		}

		List<Lock> locks = getLocksAffectedByBlock(group, location);

		Lock lock;

		if (locks.isEmpty()) {
			lock = group.lock(player, location);
		} else {
			lock = locks.get(0);

			if (!lock.isOwner(player)) {
				player.sendMessage("You are not the owner of this lock.");
				return;
			}
		}

		boolean isPublic = group.togglePublicLock(lock);

		if (isPublic) {
			player.sendMessage(ChatColor.YELLOW + "This lock is now public, anyone can access it!");
		} else {
			player.sendMessage(ChatColor.YELLOW + "This lock is no longer public.");
		}
	}

	public void groupList(CommandSender sender) {
		StandardPlayer player = plugin.getStandardPlayer(sender);

		sender.sendMessage(ChatColor.GOLD + "============== " + ChatColor.YELLOW + "Active Groups" + ChatColor.GOLD + " ==============");

		List<Group> list = storage.getGroups();
		Collections.sort(list);

		for (Group group : list) {
			if (group.isSafearea()) {
				continue;
			}

			int online = group.getOnlineCount();

			sender.sendMessage(group.getNameWithRelation(player) + " - " + ChatColor.WHITE + online + " online, " +  group.getMembers().size() + " total members");
		}
	}

	public void addModerator(StandardPlayer player, String username) {
		Group group = getPlayerGroup(player);

		if (group == null) {
			player.sendMessage("You can't set moderators for a group if you aren't in one.");
			return;
		}

		if (!group.isLeader(player)) {
			player.sendMessage("You can only designate moderators for your group if you are the leader.");
			return;
		}

		StandardPlayer moderatorPlayer = plugin.matchPlayer(username);

		if (moderatorPlayer == null) {
			player.sendMessage("That player doesn't exist.");
			return;
		}

		if (player == moderatorPlayer) {
			player.sendMessage("You can't set yourself as a moderator.");
			return;
		}

		if (!group.isMember(moderatorPlayer)) {
			player.sendMessage("That player isn't part of your group.");
			return;
		}

		if (group.isModerator(moderatorPlayer)) {
			player.sendMessage("That player is already a moderator.");
			return;
		}

		group.addModerator(moderatorPlayer);

		for (StandardPlayer other : group.getPlayers()) {
			if (!other.isOnline()) {
				continue;
			}

			if (player == other) {
				player.sendMessage(ChatColor.YELLOW + "You have set " + moderatorPlayer.getDisplayName(false) + " as a group moderator.");
			} else if (other == moderatorPlayer) {
				moderatorPlayer.sendMessage(ChatColor.YELLOW + player.getDisplayName(false) + " has set you as a group moderator.");
			} else {
				other.sendMessage(ChatColor.YELLOW + player.getDisplayName(false) + " has set " + moderatorPlayer.getDisplayName(false) + " as a group moderator.");
			}
		}
	}

	public void removeModerator(StandardPlayer player, String username) {
		Group group = getPlayerGroup(player);

		if (group == null) {
			player.sendMessage("You can't remove moderators from a group if you aren't in one.");
			return;
		}

		if (!group.isLeader(player)) {
			player.sendMessage("Only the group leader can remove moderators from the group.");
			return;
		}

		StandardPlayer moderatorPlayer = plugin.matchPlayer(username);

		if (moderatorPlayer == null) {
			player.sendMessage("That player doesn't exist.");
			return;
		}

		if (!group.isMember(moderatorPlayer)) {
			player.sendMessage("That player isn't part of your group.");
			return;
		}

		if (!group.isModerator(moderatorPlayer)) {
			player.sendMessage("That player isn't a moderator.");
			return;
		}

		group.removeModerator(moderatorPlayer);

		for (StandardPlayer other : group.getPlayers()) {
			if (!other.isOnline()) {
				continue;
			}

			if (player == other) {
				player.sendMessage(ChatColor.YELLOW + "You have removed " + moderatorPlayer.getDisplayName(false) + " as a group moderator.");
			} else if (other == moderatorPlayer) {
				moderatorPlayer.sendMessage(ChatColor.YELLOW + player.getDisplayName(false) + " has removed you as a group moderator.");
			} else {
				other.sendMessage(ChatColor.YELLOW + player.getDisplayName(false) + " has removed " + moderatorPlayer.getDisplayName(false) + " as a group moderator.");
			}
		}
	}

	public void toggleChat(StandardPlayer player) {
		Group group = getPlayerGroup(player);

		if (group == null) {
			player.sendMessage("You can't change chat modes if you aren't in a group.");
			return;
		}

		boolean groupChat = group.toggleChat(player);

		if (groupChat) {
			player.sendMessage(ChatColor.YELLOW + "You are now in group chat.");
		} else {
			player.sendMessage(ChatColor.YELLOW + "You are now in public chat.");
		}
	}

	public void toggleMap(StandardPlayer player) {
		MapManager mapManager = subPlugin.getMapManager();

		boolean enabled = mapManager.toggleMap(player);

		if (enabled) {
			player.sendMessage(ChatColor.YELLOW + "Map enabled.");
		} else {
			player.sendMessage(ChatColor.YELLOW + "Map disabled");
		}
	}

	public void showClaims(StandardPlayer player) {
		Group group = getPlayerGroup(player);

		if (group == null) {
			player.sendMessage("You can't show claims if you aren't in a group.");
			return;
		}

		if (group.getClaims().isEmpty()) {
			player.sendMessage(ChatColor.YELLOW + "Your group has not claimed any land yet.");
		} else {
			player.sendMessage(ChatColor.GOLD + "============== " + ChatColor.YELLOW + group.getNameWithRelation(player) + " Claims" + ChatColor.GOLD + " ==============");
			for (Claim claim : group.getClaims()) {
				player.sendMessage(ChatColor.YELLOW + "(" + claim.getX() + ", " + claim.getZ() + ")");
			}
		}
	}
}
