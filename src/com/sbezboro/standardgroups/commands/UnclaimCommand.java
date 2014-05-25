package com.sbezboro.standardgroups.commands;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.commands.BaseCommand;
import com.sbezboro.standardplugin.commands.SubCommand;
import com.sbezboro.standardplugin.model.StandardPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class UnclaimCommand extends SubCommand {

	public UnclaimCommand(StandardPlugin plugin, BaseCommand command) {
		super(plugin, command, "unclaim");

		addHelp(ChatColor.YELLOW + "/g unclaim" + ChatColor.RESET + " - unclaim land from your group");
		addHelp(ChatColor.YELLOW + "/g unclaim all" + ChatColor.RESET + " - unclaim all land from your group");
		addHelp(ChatColor.YELLOW + "/g unclaim <x z>" + ChatColor.RESET + " - unclaim a remote chunk from your group");
	}

	@Override
	public boolean handle(CommandSender sender, String[] args) {
		StandardPlayer player = plugin.getStandardPlayer(sender);
		
		GroupManager groupManager = StandardGroups.getPlugin().getGroupManager();

		if (args.length == 0) {
			groupManager.unclaim(player);
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("all")) {
				groupManager.unclaimAll(player);
			} else {
				showHelp(sender);
				return false;
			}
		} else if (args.length == 2) {
			int x;
			int z;

			try {
				x = Integer.parseInt(args[0].replaceAll(",", ""));
				z = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage("Chunk coordinates must be integers");
				return false;
			}

			groupManager.unclaim(player, x, z);
		} else {
			showHelp(sender);
			return false;
		}

		return true;
	}

}
