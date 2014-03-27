package com.sbezboro.standardgroups;

import java.util.ArrayList;
import java.util.List;

import com.sbezboro.standardgroups.commands.GroupsCommand;
import com.sbezboro.standardgroups.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.persistence.storages.GroupStorage;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPlugin;
import com.sbezboro.standardplugin.commands.ICommand;


public class StandardGroups extends JavaPlugin implements SubPlugin {
	private static StandardGroups instance;
	
	private StandardPlugin basePlugin;

	private StandardConfig config;
	
	private GroupStorage groupStorage;
	private GroupManager groupManager;
	
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
		groupManager = new GroupManager(basePlugin, groupStorage);

		reloadPlugin();

		registerEvents();
	}

	@Override
	public void onDisable() {
		super.onDisable();

		Bukkit.getScheduler().cancelTasks(this);
	}

	@Override
	public void reloadPlugin() {
		config.reload();
		
		reloadConfig();
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
		pluginManager.registerEvents(new BlockPistonExtendListener(basePlugin, this), this);
		pluginManager.registerEvents(new BlockPlaceEvent(basePlugin, this), this);
		pluginManager.registerEvents(new PlayerInteractListener(basePlugin, this), this);
		pluginManager.registerEvents(new PlayerMoveListener(basePlugin, this), this);
		pluginManager.registerEvents(new HangingBreakListener(basePlugin, this), this);
		pluginManager.registerEvents(new HangingPlaceListener(basePlugin, this), this);
	}
	
	public int getGroupNameMinLength() {
		return config.getGroupNameMinLength();
	}
	
	public int getGroupNameMaxLength() {
		return config.getGroupNameMaxLength();
	}
	
	public GroupManager getGroupManager() {
		return groupManager;
	}
	
	@Override
	public String getSubPluginName() {
		return "StandardGroups";
	}
}
