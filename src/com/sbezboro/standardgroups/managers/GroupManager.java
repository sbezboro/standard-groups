package com.sbezboro.standardgroups.managers;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.model.Claim;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardgroups.model.Lock;
import com.sbezboro.standardgroups.net.Notifications;
import com.sbezboro.standardgroups.persistence.storages.GroupStorage;
import com.sbezboro.standardgroups.tasks.GroupRemovalTask;
import com.sbezboro.standardgroups.tasks.LandGrowthCheckTask;
import com.sbezboro.standardgroups.tasks.PowerRestorationTask;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.managers.BaseManager;
import com.sbezboro.standardplugin.model.StandardPlayer;
import com.sbezboro.standardplugin.util.MiscUtil;
import com.sbezboro.standardplugin.util.PaginatedOutput;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.material.Bed;
import org.bukkit.material.TrapDoor;

import java.util.*;
import java.util.regex.Pattern;

public class GroupManager extends BaseManager {

	@SuppressWarnings("serial")
	private static final HashSet<Material> WOODEN_DOOR_BLOCKS = new HashSet<Material>() {{
		add(Material.WOODEN_DOOR);
		add(Material.SPRUCE_DOOR);
		add(Material.ACACIA_DOOR);
		add(Material.BIRCH_DOOR);
		add(Material.DARK_OAK_DOOR);
		add(Material.JUNGLE_DOOR);
	}};

	@SuppressWarnings("serial")
	private static final HashSet<Material> FENCE_GATE_BLOCKS = new HashSet<Material>() {{
		add(Material.FENCE_GATE);
		add(Material.SPRUCE_FENCE_GATE);
		add(Material.ACACIA_FENCE_GATE);
		add(Material.BIRCH_FENCE_GATE);
		add(Material.DARK_OAK_FENCE_GATE);
		add(Material.JUNGLE_FENCE_GATE);
	}};

	@SuppressWarnings("serial")
	private static final HashSet<Material> PROTECTED_BLOCKS = new HashSet<Material>() {{
		addAll(WOODEN_DOOR_BLOCKS);
		addAll(FENCE_GATE_BLOCKS);
		add(Material.CHEST);
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
		add(Material.LEVER);
		add(Material.STONE_BUTTON);
	}};

	@SuppressWarnings("serial")
	private static final HashSet<EntityType> PROTECTED_ENTITIES = new HashSet<EntityType>() {{
		add(EntityType.ITEM_FRAME);
		add(EntityType.ARMOR_STAND);
	}};

	@SuppressWarnings("serial")
	private static final HashSet<Material> WHITELISTED_SAFEAREA_BLOCKS = new HashSet<Material>() {{
		addAll(WOODEN_DOOR_BLOCKS);
		addAll(FENCE_GATE_BLOCKS);
		add(Material.NOTE_BLOCK);
		add(Material.STONE_BUTTON);
		add(Material.LEVER);
		add(Material.TRAP_DOOR);
	}};
	
	@SuppressWarnings("serial")
	private static final Map<Material, Float> MATERIAL_POWER_THRESHOLDS = new EnumMap<Material, Float>(Material.class) {{
		// not breakable
		put(Material.BED_BLOCK, -999.0f);
		// -11.0f
		put(Material.BEACON, -11.0f);
		put(Material.DRAGON_EGG, -11.0f);
		// -9.0f
		put(Material.OBSIDIAN, -9.0f);
		// -7.0f
		put(Material.ENCHANTMENT_TABLE, -7.0f);
		put(Material.ENDER_CHEST, -7.0f);
		put(Material.MOB_SPAWNER, -7.0f);
		put(Material.TNT, -7.0f);
		// -6.0f
		put(Material.COAL_BLOCK, -6.0f);
		put(Material.DIAMOND_BLOCK, -6.0f);
		put(Material.EMERALD_BLOCK, -6.0f);
		put(Material.ENDER_STONE, -6.0f);
		put(Material.GOLD_BLOCK, -6.0f);
		put(Material.IRON_BLOCK, -6.0f);
		put(Material.LAPIS_BLOCK, -6.0f);
		put(Material.QUARTZ_BLOCK, -6.0f);
		put(Material.QUARTZ_STAIRS, -6.0f);
		put(Material.REDSTONE_BLOCK, -6.0f);
		// -5.0f
		put(Material.ACTIVATOR_RAIL, -5.0f);
		put(Material.ANVIL, -5.0f);
		put(Material.CAULDRON, -5.0f);
		put(Material.DETECTOR_RAIL, -5.0f);
		put(Material.COAL_ORE, -5.0f);
		put(Material.DIAMOND_ORE, -5.0f);
		put(Material.EMERALD_ORE, -5.0f);
		put(Material.GOLD_ORE, -5.0f);
		put(Material.GOLD_PLATE, -5.0f);
		put(Material.IRON_DOOR_BLOCK, -5.0f);
		put(Material.IRON_FENCE, -5.0f);
		put(Material.IRON_ORE, -5.0f);
		put(Material.IRON_PLATE, -5.0f);
		put(Material.IRON_TRAPDOOR, -5.0f);
		put(Material.LAPIS_ORE, -5.0f);
		put(Material.POWERED_RAIL, -5.0f);
		put(Material.RAILS, -5.0f);
		put(Material.QUARTZ_ORE, -5.0f);
		put(Material.REDSTONE_ORE, -5.0f);
		// -4.0f
		put(Material.BRICK, -4.0f);
		put(Material.BRICK_STAIRS, -4.0f);
		put(Material.BURNING_FURNACE, -4.0f);
		put(Material.COBBLE_WALL, -4.0f);
		put(Material.COBBLESTONE, -4.0f);
		put(Material.COBBLESTONE_STAIRS, -4.0f);
		put(Material.FURNACE, -4.0f);
		put(Material.HARD_CLAY, -4.0f);
		put(Material.MONSTER_EGG, -4.0f);
		put(Material.MONSTER_EGGS, -4.0f);
		put(Material.MOSSY_COBBLESTONE, -4.0f);
		put(Material.NETHER_BRICK, -4.0f);
		put(Material.NETHER_BRICK_STAIRS, -4.0f);
		put(Material.NETHER_FENCE, -4.0f);
		put(Material.NETHERRACK, -4.0f);
		put(Material.PRISMARINE, -4.0f);
		put(Material.RED_SANDSTONE, -4.0f);
		put(Material.RED_SANDSTONE_STAIRS, -4.0f);
		put(Material.SANDSTONE, -4.0f);
		put(Material.SANDSTONE_STAIRS, -4.0f);
		put(Material.SMOOTH_BRICK, -4.0f);
		put(Material.SMOOTH_STAIRS, -4.0f);
		put(Material.STAINED_CLAY, -4.0f);
		put(Material.STONE, -4.0f);
		put(Material.STONE_SLAB2, -4.0f);
		// -3.0f
		put(Material.BOOKSHELF, -3.0f);
		put(Material.CHEST, -3.0f);
		put(Material.DAYLIGHT_DETECTOR, -3.0f);
		put(Material.DAYLIGHT_DETECTOR_INVERTED, -3.0f);
		put(Material.DIODE, -3.0f);
		put(Material.DIODE_BLOCK_OFF, -3.0f);
		put(Material.DIODE_BLOCK_ON, -3.0f);
		put(Material.DISPENSER, -3.0f);
		put(Material.DOUBLE_STEP, -3.0f);
		put(Material.DOUBLE_STONE_SLAB2, -3.0f);
		put(Material.DROPPER, -3.0f);
		put(Material.HOPPER, -3.0f);
		put(Material.JUKEBOX, -3.0f);
		put(Material.NOTE_BLOCK, -3.0f);
		put(Material.PISTON_BASE, -3.0f);
		put(Material.PISTON_EXTENSION, -3.0f);
		put(Material.PISTON_MOVING_PIECE, -3.0f);
		put(Material.PISTON_STICKY_BASE, -3.0f);
		put(Material.REDSTONE_COMPARATOR, -3.0f);
		put(Material.REDSTONE_COMPARATOR_OFF, -3.0f);
		put(Material.REDSTONE_COMPARATOR_ON, -3.0f);
		put(Material.REDSTONE_LAMP_OFF, -3.0f);
		put(Material.REDSTONE_LAMP_ON, -3.0f);
		put(Material.STEP, -3.0f);
		put(Material.STONE_BUTTON, -3.0f);
		put(Material.STONE_PLATE, -3.0f);
		put(Material.TRAPPED_CHEST, -3.0f);
		// -2.0f
		put(Material.ACACIA_DOOR, -2.0f);
		put(Material.ACACIA_FENCE, -2.0f);
		put(Material.ACACIA_FENCE_GATE, -2.0f);
		put(Material.ACACIA_STAIRS, -2.0f);
		put(Material.BIRCH_DOOR, -2.0f);
		put(Material.BIRCH_FENCE, -2.0f);
		put(Material.BIRCH_FENCE_GATE, -2.0f);
		put(Material.BIRCH_WOOD_STAIRS, -2.0f);
		put(Material.DARK_OAK_DOOR, -2.0f);
		put(Material.DARK_OAK_FENCE, -2.0f);
		put(Material.DARK_OAK_FENCE_GATE, -2.0f);
		put(Material.DARK_OAK_STAIRS, -2.0f);
		put(Material.FENCE, -2.0f);
		put(Material.FENCE_GATE, -2.0f);
		put(Material.JUNGLE_DOOR, -2.0f);
		put(Material.JUNGLE_FENCE, -2.0f);
		put(Material.JUNGLE_FENCE_GATE, -2.0f);
		put(Material.JUNGLE_WOOD_STAIRS, -2.0f);
		put(Material.LOG, -2.0f);
		put(Material.LOG_2, -2.0f);
		put(Material.SLIME_BLOCK, -2.0f);
		put(Material.SPONGE, -2.0f);
		put(Material.SPRUCE_DOOR, -2.0f);
		put(Material.SPRUCE_FENCE, -2.0f);
		put(Material.SPRUCE_FENCE_GATE, -2.0f);
		put(Material.SPRUCE_WOOD_STAIRS, -2.0f);
		put(Material.TRAP_DOOR, -2.0f);
		put(Material.WOOD, -2.0f);
		put(Material.WOOD_DOOR, -2.0f);
		put(Material.WOOD_DOUBLE_STEP, -2.0f);
		put(Material.WOOD_PLATE, -2.0f);
		put(Material.WOOD_STAIRS, -2.0f);
		put(Material.WOOD_STEP, -2.0f);
		put(Material.WOODEN_DOOR, -2.0f);
		put(Material.WORKBENCH, -2.0f);
		// -1.0f
		put(Material.ARMOR_STAND, -1.0f);
		put(Material.BREWING_STAND, -1.0f);
		put(Material.CLAY, -1.0f);
		put(Material.DIRT, -1.0f);
		put(Material.GLASS, -1.0f);
		put(Material.GLOWSTONE, -1.0f);
		put(Material.GRASS, -1.0f);
		put(Material.GRAVEL, -1.0f);
		put(Material.ICE, -1.0f);
		put(Material.MYCEL, -1.0f);
		put(Material.PACKED_ICE, -1.0f);
		put(Material.SAND, -1.0f);
		put(Material.SNOW_BLOCK, -1.0f);
		put(Material.SOIL, -1.0f);
		put(Material.SOUL_SAND, -1.0f);
		put(Material.STAINED_GLASS, -1.0f);
		put(Material.STAINED_GLASS_PANE, -1.0f);
		put(Material.THIN_GLASS, -1.0f);
		put(Material.WEB, -1.0f);
		put(Material.WOOL, -1.0f);
		// -0.5f
		put(Material.CACTUS, -0.5f);
		put(Material.CARPET, -0.5f);
		put(Material.CARROT, -0.5f);
		put(Material.COCOA, -0.5f);
		put(Material.CROPS, -0.5f);
		put(Material.HAY_BLOCK, -0.5f);
		put(Material.JACK_O_LANTERN, -0.5f);
		put(Material.LADDER, -0.5f);
		put(Material.MELON_BLOCK, -0.5f);
		put(Material.MELON_STEM, -0.5f);
		put(Material.NETHER_STALK, -0.5f);
		put(Material.NETHER_WARTS, -0.5f);
		put(Material.POTATO, -0.5f);
		put(Material.PUMPKIN, -0.5f);
		put(Material.PUMPKIN_STEM, -0.5f);
		put(Material.SEEDS, -0.5f);
		put(Material.SIGN, -0.5f);
		put(Material.SIGN_POST, -0.5f);
		put(Material.SKULL, -0.5f);
		put(Material.SNOW, -0.5f);
		put(Material.STANDING_BANNER, -0.5f);
		put(Material.SUGAR_CANE_BLOCK, -0.5f);
		put(Material.WALL_BANNER, -0.5f);
		put(Material.WALL_SIGN, -0.5f);
		// 0.0f
		put(Material.BROWN_MUSHROOM, 0.0f);
		put(Material.CAKE, 0.0f);
		put(Material.DEAD_BUSH, 0.0f);
		put(Material.DOUBLE_PLANT, 0.0f);
		put(Material.FLOWER_POT, 0.0f);
		put(Material.HUGE_MUSHROOM_1, 0.0f);
		put(Material.HUGE_MUSHROOM_2, 0.0f);
		put(Material.LEAVES, 0.0f);
		put(Material.LEAVES_2, 0.0f);
		put(Material.LEVER, 0.0f);
		put(Material.LONG_GRASS, 0.0f);
		put(Material.RED_MUSHROOM, 0.0f);
		put(Material.RED_ROSE, 0.0f);
		put(Material.REDSTONE_TORCH_OFF, 0.0f);
		put(Material.REDSTONE_TORCH_ON, 0.0f);
		put(Material.REDSTONE_WIRE, 0.0f);
		put(Material.SAPLING, 0.0f);
		put(Material.TORCH, 0.0f);
		put(Material.TRIPWIRE, 0.0f);
		put(Material.TRIPWIRE_HOOK, 0.0f);
		put(Material.VINE, 0.0f);
		put(Material.WATER_LILY, 0.0f);
		put(Material.YELLOW_FLOWER, 0.0f);
	}};
	
	public static final float LOCK_POWER_THRESHOLD = -12.0f;
	
	public static final float ENTITY_POWER_THRESHOLD = 0.0f;

	public static final int GROUP_REMOVAL_TASK_PERIOD = 1200; //seconds

	private final Pattern groupNamePat = Pattern.compile("^[a-zA-Z_]*$");
	private final String groupNamePatExplanation = "Group names can only contain letters and underscores.";

	private StandardGroups subPlugin;
	
	private GroupStorage storage;
	
	private Map<String, Group> uuidToGroupMap;
	private Map<String, Group> locationToGroupMap;

	private LandGrowthCheckTask landGrowthCheckTask;
	private GroupRemovalTask groupRemovalTask;
	private PowerRestorationTask powerRestorationTask;

	public GroupManager(StandardPlugin plugin, StandardGroups subPlugin, GroupStorage storage) {
		super(plugin);

		this.subPlugin = subPlugin;
		
		this.storage = storage;
		this.storage.loadObjects();

		landGrowthCheckTask = new LandGrowthCheckTask(plugin, subPlugin);
		landGrowthCheckTask.runTaskTimer(subPlugin, 1200, 12000);

		groupRemovalTask = new GroupRemovalTask(plugin, subPlugin);
		groupRemovalTask.runTaskTimer(subPlugin, 2400, GROUP_REMOVAL_TASK_PERIOD * 20);
		
		powerRestorationTask = new PowerRestorationTask(plugin, subPlugin);
		powerRestorationTask.runTaskTimer(subPlugin, 1200, 1200);

		reload();
	}

	private void reload() {
		uuidToGroupMap = new HashMap<String, Group>();
		locationToGroupMap = new HashMap<String, Group>();

		for (Group group : storage.getGroups()) {
			for (String uuid : group.getMemberUuids()) {
				if (uuidToGroupMap.containsKey(uuid)) {
					plugin.getLogger().severe("Duplicate member for " + group.getName() + " - " + uuid);
				}

				uuidToGroupMap.put(uuid, group);
			}

			List<Claim> claimsToRemove = new ArrayList<Claim>();
			for (Claim claim : group.getClaims()) {
				locationToGroupMap.put(claim.getLocationKey(), group);

				if (claim.getWorld().getEnvironment() == World.Environment.THE_END) {
					claimsToRemove.add(claim);
				}
			}

			for (Claim claim : claimsToRemove) {
				plugin.getLogger().severe("Group " + group.getName() + " has a claim in the end. Removing it");
				group.unclaim(claim);
			}

			if (!group.getMemberUuids().isEmpty() && !group.getMemberUuids().contains(group.getLeaderUuid())) {
				StandardPlayer firstMember = plugin.getStandardPlayerByUUID(group.getMemberUuids().get(0));
				plugin.getLogger().severe("Group " + group.getName() + " has no leader! Switching leader to " +
						firstMember.getName());
				group.setLeader(firstMember);
			}

			List<String> friendedGroupUidsToRemove = new ArrayList<String>();
			for (String uid : group.getFriendedGroupUids()) {
				Group otherGroup = getGroupByUid(uid);

				if (otherGroup == null) {
					plugin.getLogger().severe("Group " + group.getName() + " has an invalid friended group " + uid);
					friendedGroupUidsToRemove.add(uid);
				} else {
					otherGroup.addGroupThatFriends(group);
				}
			}

			if (!friendedGroupUidsToRemove.isEmpty()) {
				group.getFriendedGroupUids().removeAll(friendedGroupUidsToRemove);
				group.save();
			}
		}
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

	public Group getGroupByUid(String uid) {
		return storage.getGroupByUid(uid);
	}
	
	public Group getGroupByLocation(Location location) {
		return locationToGroupMap.get(Claim.getLocationKey(location));
	}
	
	public Group getPlayerGroup(StandardPlayer player) {
		return uuidToGroupMap.get(player.getUuidString());
	}

	public Group getSafeArea() {
		return getGroupByName(Group.SAFE_AREA);
	}

	public Group getNeutralArea() {
		return getGroupByName(Group.NEUTRAL_AREA);
	}

	public List<Group> getGroups() {
		List<Group> groups = storage.getGroups();
		groups.remove(getSafeArea());
		groups.remove(getNeutralArea());
		return groups;
	}
	
	public boolean playerInGroup(StandardPlayer player, Group group) {
		return getPlayerGroup(player) == group;
	}

	public Group matchGroup(String name) {
		Group match = null;

		for (Group group : getGroups()) {
			if (group.getName().toLowerCase().startsWith(name.toLowerCase())) {
				// Return a group with a name that directly matches the query
				if (group.getName().equalsIgnoreCase(name)) {
					return group;
				}

				// Otherwise find the shortest length group name that the query is a prefix to
				if (match == null || group.getName().length() < match.getName().length()) {
					match = group;
				}
			}
		}

		return match;
	}

	public Group matchGroupByUsernameOrGroupName(CommandSender sender, String usernameOrGroupName) {
		Group group = matchGroup(usernameOrGroupName);

		if (group == null) {
			StandardPlayer player = plugin.matchPlayer(usernameOrGroupName);

			if (player == null) {
				sender.sendMessage("No group or player by that name.");
			} else {
				group = getPlayerGroup(player);

				if (group == null) {
					sender.sendMessage("The player " + player.getDisplayName(false) + " is not in a group.");
				}
			}
		}

		return group;
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
				return "[L]";
			}
			if (group.isModerator(player)) {
				return "[M]";
			}
			return "[G]";
		}

		return "";
	}

	private void handleGroupDeletion(Group group, StandardPlayer byPlayer, boolean automatically) {
		for (Claim claim : group.getClaims()) {
			locationToGroupMap.remove(claim.getLocationKey());
		}

		for (String uuid : group.getMemberUuids()) {
			uuidToGroupMap.remove(uuid);

			StandardPlayer kickedPlayer = plugin.getStandardPlayerByUUID(uuid);
			if (!kickedPlayer.isOnline()) {
				Notifications.createGroupDestroyedNotification(kickedPlayer, group, byPlayer);
			}
		}

		group.removeFriendships();

		storage.destroyGroup(group);

		if (automatically) {
			StandardPlugin.broadcast(ChatColor.YELLOW + "The group " + group.getName() + " has been destroyed automatically.");
		} else if (byPlayer == null) {
			StandardPlugin.broadcast(ChatColor.YELLOW + "A server admin has destroyed the group " + group.getName() + ".");
		} else {
			StandardPlugin.broadcast(ChatColor.YELLOW + byPlayer.getDisplayName(false) + " has destroyed their group.");
		}
	}

	private void handleMemberRemoval(Group group, StandardPlayer player) {
		if (group.isLeader(player) && group.getMemberUuids().size() > 1) {
			StandardPlayer newLeader;

			if (!group.getModeratorUuids().isEmpty()) {
				newLeader = plugin.getStandardPlayerByUUID(group.getModeratorUuids().get(0));
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

		uuidToGroupMap.remove(player.getUuidString());
		group.removeModerator(player);
		group.removeMember(player);
	}

	private boolean isWoodenDoor(Block block) {
		return WOODEN_DOOR_BLOCKS.contains(block.getType());
	}

	public boolean isWhitelistedSafeareaBlock(Block block) {
		return WHITELISTED_SAFEAREA_BLOCKS.contains(block.getType());
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

		Lock lock = group.getLock(targetBlock.getLocation());

		// Check for surrounding block locks that may be affected by the target block:
		// 1. Blocks above for potential doors
		// 2. Blocks adjacent for potential double chests
		// 3. Blocks adjacent for potential beds
		// 4. Blocks adjacent for potential trap doors
		// 5. Block above for potential dragon egg
		if (lock == null) {
			if (isWoodenDoor(targetBlock)) {
				Block aboveBlock = targetBlock.getRelative(BlockFace.UP);
				Block belowBlock = targetBlock.getRelative(BlockFace.DOWN);

				if (isWoodenDoor(aboveBlock)) {
					affectedBlocks.add(aboveBlock);
				} else if (isWoodenDoor(belowBlock)) {
					affectedBlocks.add(belowBlock);
				}
			} else if (targetBlock.getType() == Material.CHEST) {
				for (Block block : MiscUtil.getAdjacentBlocks(targetBlock)) {
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
			} else if (!PROTECTED_BLOCKS.contains(targetBlock.getType())) {
				for (Block block : MiscUtil.getAdjacentBlocks(targetBlock)) {
					if (block.getType() == Material.TRAP_DOOR) {
						TrapDoor trapDoor = (TrapDoor) block.getState().getData();

						if (trapDoor.getAttachedFace() == block.getFace(targetBlock)) {
							affectedBlocks.add(block);
						}
					}
				}

				Block aboveBlock = targetBlock.getRelative(BlockFace.UP);
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
	
	public float powerThresholdFor(Material material) {
		float threshold = MATERIAL_POWER_THRESHOLDS.get(material);
		if (threshold == null) {
			return 0.0f;
		}
		return threshold;
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
		uuidToGroupMap.put(player.getUuidString(), group);
		
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

		handleGroupDeletion(group, player, false);
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
			uninvitedPlayer.sendMessage(ChatColor.YELLOW + player.getDisplayName(false) +
					" has revoked your invitation to the group " + group.getName() + "!");
		}
	}

	public void autoKickPlayer(StandardPlayer kickedPlayer) {
		Group group = getPlayerGroup(kickedPlayer);

		handleMemberRemoval(group, kickedPlayer);

		if (group.getMemberUuids().isEmpty()) {
			handleGroupDeletion(group, null, true);

			Notifications.createGroupDestroyedNotification(kickedPlayer, group);
		}

		Notifications.createKickedFromGroupNotification(kickedPlayer, group);
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

		handleMemberRemoval(group, kickedPlayer);

		group.sendGroupMessage(ChatColor.YELLOW + player.getDisplayName(false) +
				" has kicked " + kickedPlayer.getDisplayName(false) + " from your group.", player);
		player.sendMessage(ChatColor.YELLOW + "You have kicked " + kickedPlayer.getDisplayName(false) +
				" from your group.");

		if (kickedPlayer.isOnline()) {
			kickedPlayer.sendMessage(ChatColor.YELLOW + player.getDisplayName(false) +
					" has kicked you from the group " + group.getName() + "!");
		} else {
			Notifications.createKickedFromGroupNotification(kickedPlayer, group, player);
		}
	}

	public void joinGroup(StandardPlayer player, String usernameOrGroupName) {
		if (getPlayerGroup(player) != null) {
			player.sendMessage("You must leave your existing group first before joining a different one.");
			return;
		}

		Group group = matchGroupByUsernameOrGroupName(player, usernameOrGroupName);

		if (group == null) {
			return;
		}
		
		if (group.isInvited(player)) {
			uuidToGroupMap.put(player.getUuidString(), group);

			group.addMember(player);
			group.removeInvite(player.getName());

			group.sendGroupMessage(ChatColor.YELLOW + player.getDisplayName(false) + " has joined your group.", player);
			player.sendMessage(ChatColor.YELLOW + "You have successfully joined the group " + group.getName() + ".");
		} else {
			group.sendGroupMessage(ChatColor.YELLOW + player.getDisplayName(false) +
					" wants to join your group. Invite them by typing /g invite " +
					player.getDisplayName(false));
			
			player.sendMessage("You have not been invited to join this group yet.");
		}
	}

	public void leaveGroup(StandardPlayer player) {
		Group group = getPlayerGroup(player);
		
		if (group == null) {
			player.sendMessage("You can't leave a group if you aren't in one.");
			return;
		}

		handleMemberRemoval(group, player);
		
		if (group.getMemberUuids().isEmpty()) {
			handleGroupDeletion(group, player, false);
		} else {
			group.sendGroupMessage(ChatColor.YELLOW + player.getDisplayName(false) + " has left your group.", player);
			player.sendMessage(ChatColor.YELLOW + "You have left the group " + group.getName() + ".");
		}
	}

	private void claim(StandardPlayer player, Group group, Location location) {
		Group testGroup = getGroupByLocation(location);

		if (location.getWorld().getEnvironment() == World.Environment.THE_END) {
			player.sendMessage(ChatColor.RED + "You can't claim in the end.");
			return;
		}

		if (plugin.isNearActiveEndPortal(location, 32)) {
			player.sendMessage(ChatColor.RED + "You can't seem to claim this land...");
			return;
		}

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
				Claim claim = testGroup.getClaim(location);
				testGroup.unclaim(claim);
				locationToGroupMap.remove(claim.getLocationKey());
			} else {
				player.sendMessage("This land is already claimed.");
				return;
			}
		}
		
		if (player.isInPvp() && !isGroupsAdmin(player)) {
			player.sendMessage(ChatColor.RED + "Cannot claim land while in combat!");
			return;
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
				if (groupName.equalsIgnoreCase(Group.SAFE_AREA)) {
					group = getSafeArea();
				} else if (groupName.equalsIgnoreCase(Group.NEUTRAL_AREA)) {
					group = getNeutralArea();
				} else {
					group = matchGroup(groupName);
				}

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

	public void rename(CommandSender sender, String name, String groupName) {
		Group group;

		StandardPlayer player = plugin.getStandardPlayer(sender);

		// No group name means renaming own group
		if (groupName == null) {
			group = getPlayerGroup(player);

			if (group == null) {
				player.sendMessage("You must be in a group before you can rename one.");
				return;
			}

			if (!group.isLeader(player) && !group.isModerator(player)) {
				player.sendMessage("Only the group leader or a moderator can rename a group.");
				return;
			}
		// Trying to rename another group by name
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
		
		if (name.equals(group.getName())) {
			sender.sendMessage("Your group is already named that.");
			return;
		}
		
		if (getGroupByName(name) != null) {
			sender.sendMessage("That group name is already taken");
			return;
		}
		
		if (!groupNamePat.matcher(name).matches()) {
			sender.sendMessage(groupNamePatExplanation);
			return;
		}
		
		int minLength = subPlugin.getGroupNameMinLength();
		int maxLength = subPlugin.getGroupNameMaxLength();
		
		if (name.length() < minLength || name.length() > maxLength) {
			sender.sendMessage("The group name must be between " + minLength + " and " + maxLength + " characters long.");
			return;
		}

		group.rename(name);

		sender.sendMessage(ChatColor.YELLOW + "Group renamed.");

		for (StandardPlayer other : group.getPlayers()) {
			if (player != null && player != other && other.isOnline()) {
				// Console renames are silent
				other.sendMessage(ChatColor.YELLOW + player.getDisplayName(false) + " has renamed the group to " + name + ".");
			}
		}
	}

	public void groupInfo(CommandSender sender, String usernameOrGroupName) {
		StandardPlayer player = plugin.getStandardPlayer(sender);
		Group group;
		
		if (usernameOrGroupName == null) {
			group = getPlayerGroup(player);
			
			if (group == null) {
				player.sendMessage("You must be in a group before you can use that command.");
				return;
			}
		} else {
			group = matchGroupByUsernameOrGroupName(sender, usernameOrGroupName);

			if (group == null) {
				return;
			}
		}

		List<String> onlineMembers = new ArrayList<String>();
		List<String> offlineMembers = new ArrayList<String>();
		List<String> friendlyGroupsNames = new ArrayList<String>();

		for (StandardPlayer member : group.getPlayers()) {
			if (member.isOnline()) {
				onlineMembers.add(getGroupIdentifier(member) + member.getDisplayName());
			} else {
				offlineMembers.add(getGroupIdentifier(member) + member.getDisplayName());
			}
		}

		for (Group friendlyGroup : group.getMutuallyFriendlyGroups()) {
			friendlyGroupsNames.add(friendlyGroup.getName());
		}

		boolean maxLandLimitReached = group.getMaxClaims() >= subPlugin.getGroupLandGrowthLimit();

		sender.sendMessage(ChatColor.GOLD + "============== " + ChatColor.YELLOW + "Group: " + group.getNameWithRelation(player) + ChatColor.GOLD + " ==============");
		sender.sendMessage(ChatColor.YELLOW + "Established: " + ChatColor.RESET + MiscUtil.friendlyTimestamp(group.getEstablished()));
		sender.sendMessage(ChatColor.YELLOW + "Land count: " + ChatColor.RESET + group.getClaims().size());
		sender.sendMessage(ChatColor.YELLOW + "Land limit: " + ChatColor.RESET + group.getMaxClaims() + (maxLandLimitReached ? " (max)" : ""));

		if (!maxLandLimitReached && (player == null || getPlayerGroup(player) == group)) {
			sender.sendMessage(ChatColor.YELLOW + "Next land growth: " + ChatColor.RESET +  MiscUtil.friendlyTimestamp(group.getNextGrowth()));
		}

		sender.sendMessage(ChatColor.YELLOW + "Power: " + ChatColor.RESET + group.getPowerRounded() + " / " + group.getMaxPowerRounded());
		sender.sendMessage(ChatColor.YELLOW + "Friends: " + ChatColor.RESET + StringUtils.join(friendlyGroupsNames, ChatColor.RESET + ", "));
		sender.sendMessage(ChatColor.YELLOW + "Online members: " + ChatColor.RESET + StringUtils.join(onlineMembers, ChatColor.RESET + ", "));
		sender.sendMessage(ChatColor.YELLOW + "Offline members: " + ChatColor.RESET + StringUtils.join(offlineMembers, ChatColor.RESET + ", "));
		sender.sendMessage(ChatColor.YELLOW + "Link: " + ChatColor.RESET + "standardsurvival.com/group/" + group.getName());
	}

	public void groupPower(CommandSender sender, String usernameOrGroupName) {
		StandardPlayer player = plugin.getStandardPlayer(sender);
		Group group;
		
		if (usernameOrGroupName == null) {
			group = getPlayerGroup(player);
			
			if (group == null) {
				player.sendMessage("You must be in a group before you can use that command.");
				return;
			}
		} else {
			group = matchGroupByUsernameOrGroupName(sender, usernameOrGroupName);

			if (group == null) {
				return;
			}
		}

		float power = group.getPowerRounded();
		float maxPower = group.getMaxPowerRounded();
		String powerColor = (power < -10.0f ? ChatColor.RED : (power < 0.0f ? ChatColor.LIGHT_RED : ChatColor.RESET));
		if (getPlayerGroup(player) == group) {
			sender.sendMessage(ChatColor.YELLOW + "Your group's current power is " +
					powerColor + group.getPowerRounded() + ChatColor.RESET + " / " + group.getMaxPowerRounded());
		}
		else {	
			sender.sendMessage(ChatColor.YELLOW +
					"The group " + group.getNameWithRelation(player) + ChatColor.YELLOW + " has power " +
					powerColor + group.getPowerRounded() + ChatColor.RESET + " / " + group.getMaxPowerRounded());
		}
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

		if (!group.canLock(location)) {
			player.sendMessage("There are too many locks in this claim already.");
			return;
		}
		
		if (group.getPower() < LOCK_POWER_THRESHOLD) {
			player.sendMessage(ChatColor.RED + "Your group's power is too low to lock more blocks.");
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

		Lock lock;

		if (locks.isEmpty()) {
			if (!group.canLock(location)) {
				player.sendMessage("There are too many locks in this claim already.");
				return;
			}
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

	public void groupList(CommandSender sender, int page) {
		StandardPlayer player = plugin.getStandardPlayer(sender);

		PaginatedOutput paginatedOutput = new PaginatedOutput("Active Groups", page);

		List<Group> list = getGroups();
		Collections.sort(list);

		for (Group group : list) {
			int online = group.getOnlineCount();

			paginatedOutput.addLine(group.getNameWithRelation(player) + ChatColor.WHITE + " - " +
					online + " / " + group.getPlayerCount() + " online members; " +
					group.getPowerRounded() + " / " + group.getMaxPowerRounded() + " power");
		}

		paginatedOutput.show(sender);
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

		if (!group.isModerator(player) && !group.isLeader(player)) {
			player.sendMessage("Only the group leader or a moderator can show claims.");
			return;
		}

		if (group.getClaims().isEmpty()) {
			player.sendMessage(ChatColor.YELLOW + "Your group has not claimed any land yet.");
		} else {
			player.sendMessage(ChatColor.GOLD + "============== " + ChatColor.YELLOW + group.getNameWithRelation(player) + " Claims (Chunk coords)" + ChatColor.GOLD + " ==============");
			for (Claim claim : group.getClaims()) {
				player.sendMessage(ChatColor.YELLOW + "([" + claim.getWorldDisplayName() + "] " + claim.getX() + ", " + claim.getZ() + ")");
			}
		}
	}

	public void friendGroup(StandardPlayer player, String usernameOrGroupName) {
		Group group = getPlayerGroup(player);

		if (group == null) {
			player.sendMessage("You must be in a group to friend other groups.");
			return;
		}

		if (!group.isLeader(player)) {
			player.sendMessage("Only the group leader can friend other groups.");
			return;
		}

		Group otherGroup = matchGroupByUsernameOrGroupName(player, usernameOrGroupName);

		if (otherGroup == null) {
			return;
		}

		if (group == otherGroup) {
			player.sendMessage("You can't friend your own group.");
			return;
		}

		if (group.isGroupFriended(otherGroup)) {
			player.sendMessage("Your group already considers the group " + otherGroup.getName() + " friendly.");
			return;
		}

		group.setFriend(otherGroup);

		if (otherGroup.isGroupFriended(group)) {
			player.sendMessage(ChatColor.YELLOW + "Your group is now friends with the group " +
					ChatColor.GOLD + otherGroup.getName() + ChatColor.YELLOW + "!");
			group.sendGroupMessage(ChatColor.YELLOW + player.getDisplayName() +
					ChatColor.YELLOW + " has accepted friendship with the group " +
					ChatColor.GOLD + otherGroup.getName() + ChatColor.YELLOW, player);
			otherGroup.sendGroupMessage(ChatColor.YELLOW + player.getDisplayName() +
					ChatColor.YELLOW + " from the group " + ChatColor.GOLD + group.getName() +
					ChatColor.YELLOW + " has accepted your group's friendship.");
		} else {
			player.sendMessage(ChatColor.YELLOW + "Your group now considers the group "
					+ ChatColor.GOLD + otherGroup.getName() + ChatColor.YELLOW +
					" friendly. They must still friend your group back.");
			group.sendGroupMessage(ChatColor.YELLOW + player.getDisplayName() +
					ChatColor.YELLOW + " has requested to be friends with the group " +
					ChatColor.GOLD + otherGroup.getName() + ChatColor.YELLOW, player);
			otherGroup.sendGroupMessage(ChatColor.YELLOW + player.getDisplayName() +
					ChatColor.YELLOW + " from the group " + ChatColor.GOLD + group.getName() +
					ChatColor.YELLOW + " wishes to be friendly with your group.");
		}
	}

	public void unfriendGroup(StandardPlayer player, String usernameOrGroupName) {
		Group group = getPlayerGroup(player);

		if (group == null) {
			player.sendMessage("You must be in a group to friend other groups.");
			return;
		}

		if (!group.isLeader(player)) {
			player.sendMessage("Only the group leader can friend other groups.");
			return;
		}

		Group otherGroup = matchGroupByUsernameOrGroupName(player, usernameOrGroupName);

		if (otherGroup == null) {
			return;
		}

		if (group == otherGroup) {
			player.sendMessage("You can't unfriend your own group.");
			return;
		}

		if (!group.isGroupFriended(otherGroup)) {
			player.sendMessage("Your group isn't friends with the group " + otherGroup.getName());
			return;
		}

		group.unfriend(otherGroup);

		if (otherGroup.isGroupFriended(group)) {
			otherGroup.unfriend(group);

			player.sendMessage(ChatColor.YELLOW + "The friendship between your group and " +
					ChatColor.GOLD + otherGroup.getName() + ChatColor.YELLOW +
					" has been broken.");
			group.sendGroupMessage(ChatColor.RED + player.getDisplayName() +
					ChatColor.RED + " has broken the friendship between your group and the group " +
					ChatColor.DARK_RED + otherGroup.getName() + ChatColor.RED + "!", player);
			otherGroup.sendGroupMessage(ChatColor.RED + player.getDisplayName() +
					ChatColor.RED + " from the group " + ChatColor.DARK_RED + group.getName() +
					ChatColor.RED + " has broken the friendship with your group!");
		} else {
			player.sendMessage(ChatColor.YELLOW + "You no longer consider the group " +
					ChatColor.GOLD + otherGroup.getName() + ChatColor.YELLOW + " friendly.");
			group.sendGroupMessage(ChatColor.RED + player.getDisplayName() +
					ChatColor.YELLOW + " no longer considers the group " +
					ChatColor.GOLD + otherGroup.getName() + ChatColor.YELLOW + " friendly", player);
			otherGroup.sendGroupMessage(ChatColor.YELLOW + player.getDisplayName() +
					ChatColor.YELLOW + " from the group " + ChatColor.GOLD + group.getName() +
					ChatColor.YELLOW + " no longer wants to be friends with your group.");
		}
	}

}
