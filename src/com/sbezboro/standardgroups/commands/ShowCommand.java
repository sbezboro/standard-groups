package com.sbezboro.standardgroups.commands;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.commands.BaseCommand;
import com.sbezboro.standardplugin.commands.SubCommand;
import com.sbezboro.standardplugin.model.StandardPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ShowCommand extends SubCommand {

	public ShowCommand(StandardPlugin plugin, BaseCommand command) {
		super(plugin, command, "show");
	}

	@Override
	public boolean handle(CommandSender sender, String[] args) {
		StandardPlayer player = plugin.getStandardPlayer(sender);
		
		GroupManager groupManager = StandardGroups.getPlugin().getGroupManager();

		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("claims")) {
				groupManager.showClaims(player);
			} else {
				showHelp(sender);
				return false;
			}
		} else {
			showHelp(sender);
			return false;
		}

		return true;
	}

	@Override
	public void showHelp(CommandSender sender) {
		sender.sendMessage(ChatColor.YELLOW + "/g show claims" + ChatColor.RESET + " - show a list of claimed chunks for your group");
	}
	
}
