package com.sbezboro.standardgroups;

import com.sbezboro.standardgroups.commands.GroupsCommand;
import com.sbezboro.standardgroups.listeners.*;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.managers.MapManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardgroups.persistence.storages.GroupStorage;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPlugin;
import com.sbezboro.standardplugin.commands.ICommand;
import com.sbezboro.standardplugin.model.StandardPlayer;
import com.sbezboro.standardplugin.persistence.PersistedPropertyDefinition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class StandardGroups extends JavaPlugin implements SubPlugin {
	private static StandardGroups instance;
	
	private StandardPlugin basePlugin;

	private StandardConfig config;
	
	private GroupStorage groupStorage;
	private GroupManager groupManager;

	private MapManager mapManager;
	
	public StandardGroups() {
		instance = this;
	}

	public static StandardGroups getPlugin() {
		return instance;
	}

	@Override
	public void onLoad() {
		super.onLoad();
	}

	@Override
	public void onEnable() {
		super.onEnable();
		
		getConfig().options().copyDefaults(true);
		
		basePlugin = StandardPlugin.getPlugin();
		basePlugin.registerSubPlugin(this);
		
		config = new StandardConfig(basePlugin);
		
		groupStorage = new GroupStorage(basePlugin);
		groupManager = new GroupManager(basePlugin, this, groupStorage);

		mapManager = new MapManager(basePlugin, this);

		reloadPlugin();

		registerEvents();
	}

	@Override
	public void onDisable() {
		super.onDisable();

		Bukkit.getScheduler().cancelTasks(this);

		mapManager.unload();
	}

	@Override
	public void reloadPlugin() {
		config.reload();
		
		reloadConfig();
	}

	@Override
	public Map<String, Object> additionalServerStatus(boolean minimal) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		if (minimal) {
			return result;
		}

		ArrayList<Map<String, Object>> groups = new ArrayList<Map<String, Object>>();

		for (Group group : groupManager.getGroups()) {
			groups.add(group.getInfo());
		}

		result.put("groups", groups);

		return result;
	}

	@Override
	public String formatWebChatName(StandardPlayer sender, StandardPlayer receiver, String name) {
		Group senderGroup = null;

		if (sender != null) {
			senderGroup = groupManager.getPlayerGroup(sender);
		}

		if (senderGroup == null) {
			return name;
		}

		Group receiverGroup = null;
		if (receiver != null) {
			receiverGroup = groupManager.getPlayerGroup(receiver);
		}

		String identifier = groupManager.getGroupIdentifier(sender);
		identifier += "[" + senderGroup.getName() + "]";

		if (receiverGroup != null &&
				receiverGroup == senderGroup) {
			name = name.replace(name, ChatColor.GREEN + identifier + " " + ChatColor.AQUA + name);
		} else if (receiverGroup != null &&
				receiverGroup.isMutualFriendship(senderGroup)) {
			name = name.replace(name, ChatColor.DARK_AQUA + identifier + " " + ChatColor.AQUA + name);
		} else if (receiver != null) {
			name = name.replace(name, ChatColor.YELLOW + identifier + " " + ChatColor.AQUA + name);
		} else {
			// null receiver means console, don't use color for group name
			name = name.replace(name, ChatColor.RESET + identifier + " " + ChatColor.AQUA + name);
		}

		return name;
	}

	@Override
	public List<PersistedPropertyDefinition> extraPlayerPropertyDefinitions() {
		// TODO: add some properties
		return new ArrayList<PersistedPropertyDefinition>();
	}

	@Override
	public List<ICommand> getCommands() {
		List<ICommand> commands = new ArrayList<ICommand>();
		commands.add(new GroupsCommand(basePlugin, this));
		return commands;
	}

	private void registerEvents() {
		PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new BlockBreakListener(basePlugin, this), this);
		pluginManager.registerEvents(new BlockFadeListener(basePlugin, this), this);
		pluginManager.registerEvents(new BlockPistonExtendListener(basePlugin, this), this);
		pluginManager.registerEvents(new BlockPistonRetractListener(basePlugin, this), this);
		pluginManager.registerEvents(new BlockPlaceListener(basePlugin, this), this);
		pluginManager.registerEvents(new CreatureSpawnListener(basePlugin, this), this);
		pluginManager.registerEvents(new DeathListener(basePlugin, this), this);
		pluginManager.registerEvents(new EntityBlockFormListener(basePlugin, this), this);
		pluginManager.registerEvents(new EntityBreakDoorListener(basePlugin, this), this);
		pluginManager.registerEvents(new EntityChangeBlockListener(basePlugin, this), this);
		pluginManager.registerEvents(new EntityExplodeListener(basePlugin, this), this);
		pluginManager.registerEvents(new EntityTargetListener(basePlugin, this), this);
		pluginManager.registerEvents(new HangingBreakListener(basePlugin, this), this);
		pluginManager.registerEvents(new HangingPlaceListener(basePlugin, this), this);
		pluginManager.registerEvents(new InventoryMoveListener(basePlugin, this), this);
		pluginManager.registerEvents(new LiquidFlowListener(basePlugin, this), this);
		pluginManager.registerEvents(new PlayerChatListener(basePlugin, this), this);
		pluginManager.registerEvents(new PlayerBucketEmptyListener(basePlugin, this), this);
		pluginManager.registerEvents(new PlayerBucketFillListener(basePlugin, this), this);
		pluginManager.registerEvents(new PlayerInteractListener(basePlugin, this), this);
		pluginManager.registerEvents(new PlayerDamageListener(basePlugin, this), this);
		pluginManager.registerEvents(new PlayerJoinListener(basePlugin, this), this);
		pluginManager.registerEvents(new PlayerMoveListener(basePlugin, this), this);
		pluginManager.registerEvents(new PlayerTeleportListener(basePlugin, this), this);
		pluginManager.registerEvents(new PotionSplashListener(basePlugin, this), this);
	}
	
	public int getGroupNameMinLength() {
		return config.getGroupNameMinLength();
	}
	
	public int getGroupNameMaxLength() {
		return config.getGroupNameMaxLength();
	}

	public int getGroupLandGrowth() {
		return config.getGroupLandGrowth();
	}

	public int getGroupLandGrowthDays() {
		return config.getGroupLandGrowthDays();
	}

	public int getGroupLandGrowthLimit() {
		return config.getGroupLandGrowthLimit();
	}

	public int getGroupStartingLand() {
		return config.getGroupStartingLand();
	}

	public int getGroupAutoKickDays() {
		return config.getGroupAutoKickDays();
	}

	public int getMaxLocksPerChunk() {
		return config.getMaxLocksPerChunk();
	}
	
	public double getGroupPowerGrowth() {
		return config.getGroupPowerGrowth();
	}
	
	public double getGroupPowerMinValue() {
		return config.getGroupPowerMinValue();
	}
	
	public double getGroupPowerMaxValue() {
		return config.getGroupPowerMaxValue();
	}

	public double getPowerDamageModifier() {
		return config.getPowerDamageModifier();
	}

	public int getSpawnClaimCost() {
		return config.getSpawnClaimCost();
	}

	public boolean getPreventLavaGriefing() {
		return config.getPreventLavaGriefing();
	}

	public boolean getPreventWaterGriefing() {
		return config.getPreventWaterGriefing();
	}

	public GroupManager getGroupManager() {
		return groupManager;
	}

	public MapManager getMapManager() {
		return mapManager;
	}

	@Override
	public String getSubPluginName() {
		return "StandardGroups";
	}
}
