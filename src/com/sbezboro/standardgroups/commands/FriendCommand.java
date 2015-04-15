package com.sbezboro.standardgroups.commands;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.commands.BaseCommand;
import com.sbezboro.standardplugin.commands.SubCommand;
import com.sbezboro.standardplugin.model.StandardPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class FriendCommand extends SubCommand {

	public FriendCommand(StandardPlugin plugin, BaseCommand command) {
		super(plugin, command, "friend");

		addHelp(ChatColor.YELLOW + "/g friend <name>" + ChatColor.RESET + " - friend another group");
	}

	@Override
	public boolean handle(CommandSender sender, String[] args) {
		if (args.length != 1) {
			sender.sendMessage("You must specify which group name you would like to friend.");
			return false;
		}
		
		StandardPlayer player = plugin.getStandardPlayer(sender);
		
		GroupManager groupManager = StandardGroups.getPlugin().getGroupManager();
		groupManager.friendGroup(player, args[0]);
		
		return true;
	}
	
}
