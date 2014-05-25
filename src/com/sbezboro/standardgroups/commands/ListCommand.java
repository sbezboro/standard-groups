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

public class ListCommand extends SubCommand {

	public ListCommand(StandardPlugin plugin, BaseCommand command) {
		super(plugin, command, "list");

		addHelp(ChatColor.YELLOW + "/g list [page]" + ChatColor.RESET + " - show all active groups");
	}

	@Override
	public boolean handle(CommandSender sender, String[] args) {
		GroupManager groupManager = StandardGroups.getPlugin().getGroupManager();

		if (args.length > 1) {
			command.showUsageInfo(sender);
			return false;
		} else if (args.length == 1) {
			int page;

			try {
				page = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				page = 1;
			}

			groupManager.groupList(sender, page);
		} else {
			groupManager.groupList(sender, 1);
		}
		

		
		return true;
	}

}
