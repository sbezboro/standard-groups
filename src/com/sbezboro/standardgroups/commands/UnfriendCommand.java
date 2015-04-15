package com.sbezboro.standardgroups.commands;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.commands.BaseCommand;
import com.sbezboro.standardplugin.commands.SubCommand;
import com.sbezboro.standardplugin.model.StandardPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class UnfriendCommand extends SubCommand {

	public UnfriendCommand(StandardPlugin plugin, BaseCommand command) {
		super(plugin, command, "unfriend");

		addHelp(ChatColor.YELLOW + "/g unfriend <name>" + ChatColor.RESET + " - unfriend another group");
	}

	@Override
	public boolean handle(CommandSender sender, String[] args) {
		if (args.length != 1) {
			sender.sendMessage("You must specify which group name you would like to unfriend.");
			return false;
		}
		
		StandardPlayer player = plugin.getStandardPlayer(sender);
		
		GroupManager groupManager = StandardGroups.getPlugin().getGroupManager();
		groupManager.unfriendGroup(player, args[0]);
		
		return true;
	}
	
}
