package com.sbezboro.standardgroups.commands;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.commands.BaseCommand;
import com.sbezboro.standardplugin.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class RenameCommand extends SubCommand {

	public RenameCommand(StandardPlugin plugin, BaseCommand command) {
		super(plugin, command, "rename");

		addHelp(ChatColor.YELLOW + "/g rename <name>" + ChatColor.RESET + " - rename your group");
	}

	@Override
	public boolean handle(CommandSender sender, String[] args) {
		GroupManager groupManager = StandardGroups.getPlugin().getGroupManager();

		String groupName = null;

		if (args.length == 1) {
			if (sender instanceof ConsoleCommandSender) {
				command.showPlayerOnlyMessage(sender);
				return false;
			}
		} else if (args.length == 2) {
			groupName = args[1];
		} else {
			sender.sendMessage("You must specify a name to rename your group to.");
			return false;
		}

		groupManager.rename(sender, args[0], groupName);
		
		return true;
	}

}
