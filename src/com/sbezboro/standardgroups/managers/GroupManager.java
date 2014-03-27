package com.sbezboro.standardgroups.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

import com.sbezboro.standardgroups.model.Lock;
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

public class GroupManager extends BaseManager {

	@SuppressWarnings("serial")
	private static final HashSet<Material> PROTECTED_BLOCKS = new HashSet<Material>() {{
		add(Material.CHEST);
		add(Material.WOODEN_DOOR);
		add(Material.FENCE_GATE);
		add(Material.IRON_DOOR);
		add(Material.TRAP_DOOR);
		add(Material.ENDER_CHEST);
		add(Material.HOPPER);
		add(Material.FURNACE);
		add(Material.DISPENSER);
		add(Material.DROPPER);
		add(Material.ENCHANTMENT_TABLE);
		add(Material.TRAPPED_CHEST);
		add(Material.JUKEBOX);
		add(Material.BREWING_STAND);
		add(Material.ANVIL);
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
	}};

	@SuppressWarnings("serial")
	private static final HashSet<EntityType> PROTECTED_ENTITIES = new HashSet<EntityType>() {{
		add(EntityType.ITEM_FRAME);
	}};

	private final Pattern groupNamePat = Pattern.compile("^[a-zA-Z_]*$");
	private final String groupNamePatExplanation = "Group names can only contain letters and underscores.";
	
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
	
	public Group getGroupByLocation(Location location) {
		return locationToGroupMap.get(Claim.getLocationKey(location));
	}
	
	public Claim getClaimByLocation(Location location) {
		return locationToClaimMap.get(Claim.getLocationKey(location));
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

	public Lock getLockAffectedByBlock(Group group, Location location) {
		Block targetBlock = location.getBlock();
		Block aboveBlock = targetBlock.getRelative(BlockFace.UP);
		Block belowBlock = targetBlock.getRelative(BlockFace.DOWN);

		Lock lock = group.getLock(location);

		// Check for surrounding block locks that may be affected by the target block:
		// 1. Blocks above and below for potential doors
		// 2. Blocks adjacent for potential double chests
		if (lock == null) {
			Block testBlock = null;

			if (aboveBlock.getType() == Material.WOODEN_DOOR) {
				testBlock = aboveBlock;
			} else if (belowBlock.getType() == Material.WOODEN_DOOR) {
				testBlock = belowBlock;
			} else if (targetBlock.getType() == Material.CHEST) {
				Block[] testBlocks = new Block[] {
						targetBlock.getRelative(BlockFace.NORTH),
						targetBlock.getRelative(BlockFace.EAST),
						targetBlock.getRelative(BlockFace.SOUTH),
						targetBlock.getRelative(BlockFace.WEST)
				};

				for (Block block : testBlocks) {
					if (block.getType() == Material.CHEST) {
						testBlock = block;
						continue;
					}
				}
			}

			if (testBlock != null) {
				lock = group.getLock(testBlock.getLocation());
			}
		}

		return lock;
	}
	
	public void createGroup(StandardPlayer player, String groupName) {
		if (getPlayerGroup(player) != null) {
			player.sendMessage("You must leave your existing group first before creating a new one.");
			return;
		}
		
		if (storage.getGroupByName(groupName) != null) {
			player.sendMessage("That group name is already taken");
			return;
		}
		
		if (!groupNamePat.matcher(groupName).matches()) {
			player.sendMessage(groupNamePatExplanation);
			return;
		}
		
		int minLength = StandardGroups.getPlugin().getGroupNameMinLength();
		int maxLength = StandardGroups.getPlugin().getGroupNameMaxLength();
		
		if (groupName.length() < minLength || groupName.length() > maxLength) {
			player.sendMessage("The group name must be between " + minLength + " and " + maxLength + " characters long.");
			return;
		}
		
		Group group = storage.createGroup(groupName, player);
		usernameToGroupMap.put(player.getName(), group);
		
		StandardPlugin.broadcast(ChatColor.YELLOW + player.getDisplayName(false) + " has created a new group called " + groupName + ".");
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
				player.sendMessage(StandardGroups.getPlugin().getServer().getPluginCommand("groups").getPermissionMessage());
				return;
			}
		}

		for (String username : group.getMembers()) {
			usernameToGroupMap.remove(username);
		}
		
		for (Claim claim : group.getClaims()) {
			locationToGroupMap.remove(claim.getLocationKey());
			locationToClaimMap.remove(claim.getLocationKey());
		}
		
		storage.destroyGroup(group);
		
		if (player == null) {
			StandardPlugin.broadcast(ChatColor.YELLOW + "A server admin has destroyed the group " + group.getName() + ".");
		} else {
			StandardPlugin.broadcast(ChatColor.YELLOW + player.getDisplayName(false) + " has destroyed the group " + group.getName() + ".");
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
		
		invitedPlayer.sendMessage(ChatColor.YELLOW + player.getDisplayName(false) + " has invited you to join their group " + group.getName() + ". To join, type /g join " + group.getName());
	}

	public void joinGroup(StandardPlayer player, String groupName) {
		if (getPlayerGroup(player) != null) {
			player.sendMessage("You must leave your existing group first before joining a different one.");
			return;
		}

		Group group = matchGroup(groupName);
		
		if (group == null) {
			player.sendMessage("That group does not exist.");
			return;
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
				locationToClaimMap.remove(claim.getLocationKey());
			}
			
			storage.destroyGroup(group);
			
			StandardPlugin.broadcast(ChatColor.YELLOW + player.getDisplayName(false) + " has destroyed the group " + group.getName() + ".");
		}
	}

	public void claim(StandardPlayer player) {
		Group group = getPlayerGroup(player);
		
		if (group == null) {
			player.sendMessage("You must be in a group before you can claim land.");
			return;
		}
		
		Group testGroup = getGroupByLocation(player.getLocation());
		
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
		Group group = getPlayerGroup(player);
		
		if (group == null) {
			player.sendMessage("You must be in a group before you can unclaim land.");
			return;
		}

		Claim claim = getClaimByLocation(player.getLocation());
		
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
		
		if (storage.getGroupByName(name) != null) {
			player.sendMessage("That group name is already taken");
			return;
		}
		
		if (!groupNamePat.matcher(name).matches()) {
			player.sendMessage(groupNamePatExplanation);
			return;
		}
		
		int minLength = StandardGroups.getPlugin().getGroupNameMinLength();
		int maxLength = StandardGroups.getPlugin().getGroupNameMaxLength();
		
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

	public void groupInfo(StandardPlayer player, String groupName) {
		Group group;
		
		if (groupName == null) {
			group = getPlayerGroup(player);
			
			if (group == null) {
				player.sendMessage("You must be in a group before you can use that command.");
				return;
			}
		} else {
			group = matchGroup(groupName);
			
			if (group == null) {
				String message = "That group doesn't exist.";
				
				if (player == null) {
					Bukkit.getConsoleSender().sendMessage(message);
				} else {
					player.sendMessage(message);
				}
				
				return;
			}
		}
		
		String members = "";
		String delim = "";
		for (StandardPlayer member : group.getPlayers()) {
			members += delim + member.getDisplayName() + ChatColor.RESET;
		    delim = ", ";
		}
		
		player.sendMessage("Group: " + group.getName());
		player.sendMessage("==============================");
		player.sendMessage("Established: " + MiscUtil.friendlyTimestamp(group.getEstablished()));
		player.sendMessage("Land: " + group.getClaims().size());
		player.sendMessage("Land limit: " + group.getMaxClaims());
		player.sendMessage("Members: " + members);
	}

	public void lock(StandardPlayer player, Block block) {
		Group group = getPlayerGroup(player);

		if (group == null) {
			player.sendMessage("You must be in a group before you can lock things.");
			return;
		}

		if (!PROTECTED_BLOCKS.contains(block.getType())) {
			player.sendMessage("You can't lock this block.");
			return;
		}

		Location location = block.getLocation();

		Claim claim = locationToClaimMap.get(Claim.getLocationKey(location));

		if (claim == null) {
			player.sendMessage("You can only lock things in your group's territory.");
			return;
		}

		if (group.getLock(location) != null) {
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

		Claim claim = locationToClaimMap.get(Claim.getLocationKey(location));

		if (claim == null) {
			player.sendMessage("You can only unlock things in your group's territory.");
			return;
		}

		Lock lock = group.getLock(location);

		if (lock == null) {
			player.sendMessage("No lock exists on this block.");
			return;
		}

		if (!lock.isOwner(player)) {
			player.sendMessage("You are not the owner of this lock.");
			return;
		}

		group.unlock(lock);

		player.sendMessage(ChatColor.YELLOW + "You have released the lock on this block.");
	}
}
