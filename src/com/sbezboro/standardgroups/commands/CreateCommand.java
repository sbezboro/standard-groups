package com.sbezboro.standardgroups.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.commands.BaseCommand;
import com.sbezboro.standardplugin.commands.SubCommand;
import com.sbezboro.standardplugin.model.StandardPlayer;

public class CreateCommand extends SubCommand {

	public CreateCommand(StandardPlugin plugin, BaseCommand command) {
		super(plugin, command, "create");
	}

	@Override
	public boolean handle(CommandSender sender, String[] args) {
		if (args.length != 1) {
			sender.sendMessage("You must provide a name for your group.");
			return false;
		}
		
		StandardPlayer player = plugin.getStandardPlayer(sender);
		
		GroupManager groupManager = StandardGroups.getPlugin().getGroupManager();
		groupManager.createGroup(player, args[0]);
		
		return true;
	}

	@Override
	public void showHelp(CommandSender sender) {
		sender.sendMessage(ChatColor.YELLOW + "/g create <name>" + ChatColor.RESET + " - create a group");
	}

}
