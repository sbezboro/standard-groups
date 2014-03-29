package com.sbezboro.standardgroups.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.commands.BaseCommand;
import com.sbezboro.standardplugin.commands.SubCommand;
import com.sbezboro.standardplugin.model.StandardPlayer;

public class ClaimCommand extends SubCommand {

	public ClaimCommand(StandardPlugin plugin, BaseCommand command) {
		super(plugin, command, "claim");
	}

	@Override
	public boolean handle(CommandSender sender, String[] args) {
		StandardPlayer player = plugin.getStandardPlayer(sender);

		if (player == null) {
			command.showPlayerOnlyMessage(sender);
			return false;
		}
		
		GroupManager groupManager = StandardGroups.getPlugin().getGroupManager();

		String name = null;
		int width = 1;

		if (args.length == 1) {
			name = args[0];
		} else if (args.length == 2) {
			name = args[0];
			try {
				width = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage("Enter a valid width");
				return false;
			}
		} else {
			command.showUsageInfo(sender);
			return false;
		}

		groupManager.claim(player, name, width);
		
		return true;
	}

	@Override
	public void showHelp(CommandSender sender) {
		sender.sendMessage(ChatColor.YELLOW + "/g claim" + ChatColor.RESET + " - claim land for your group");
	}
	
}
