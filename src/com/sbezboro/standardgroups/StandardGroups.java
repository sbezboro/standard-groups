package com.sbezboro.standardgroups;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPlugin;
import com.sbezboro.standardplugin.commands.ICommand;


public class StandardGroups extends JavaPlugin implements SubPlugin {
	private static StandardGroups instance;
	
	private StandardPlugin basePlugin;
	
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
		
		basePlugin = StandardPlugin.getPlugin();
		basePlugin.registerSubPlugin(this);

		reloadPlugin();

		registerCommands();
		registerEvents();
	}

	@Override
	public void onDisable() {
		super.onDisable();

		Bukkit.getScheduler().cancelTasks(this);
	}

	public void reloadPlugin() {
		reloadConfig();
	}

	private void registerCommands() {
		List<ICommand> commands = new ArrayList<ICommand>();

		for (ICommand command : commands) {
			command.register();
		}
	}

	private void registerEvents() {
		PluginManager pluginManager = getServer().getPluginManager();
	}
	
	@Override
	public String getSubPluginName() {
		return "StandardGroups";
	}
}
