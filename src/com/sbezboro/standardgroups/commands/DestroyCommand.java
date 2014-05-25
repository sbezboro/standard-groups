package com.sbezboro.standardgroups.commands;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.commands.BaseCommand;
import com.sbezboro.standardplugin.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.ArrayList;
import java.util.List;

public class DestroyCommand extends SubCommand {

	public DestroyCommand(StandardPlugin plugin, BaseCommand command) {
		super(plugin, command, "destroy");

		addHelp(ChatColor.YELLOW + "/g destroy" + ChatColor.RESET + " - destroy a group you own");
	}

	@Override
	public boolean handle(CommandSender sender, String[] args) {
		GroupManager groupManager = StandardGroups.getPlugin().getGroupManager();

		String name = null;
		
		if (args.length == 0) {
			if (sender instanceof ConsoleCommandSender) {
				command.showPlayerOnlyMessage(sender);
				return false;
			}
		} else if (args.length == 1) {
			name = args[0];
		} else {
			command.showUsageInfo(sender);
			return false;
		}
		
		groupManager.destroyGroup(sender, name);
		
		return true;
	}

}
