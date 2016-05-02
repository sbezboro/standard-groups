package com.sbezboro.standardgroups.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.commands.BaseCommand;
import com.sbezboro.standardplugin.commands.SubCommand;
import com.sbezboro.standardplugin.model.StandardPlayer;

import java.util.ArrayList;
import java.util.List;

public class LeaveCommand extends SubCommand {

	public LeaveCommand(StandardPlugin plugin, BaseCommand command) {
		super(plugin, command, "leave");

		addHelp(ChatColor.YELLOW + "/g leave" + ChatColor.RESET + " - leave a group you are in");
	}

	@Override
	public boolean handle(CommandSender sender, String[] args) {
		StandardPlayer player = plugin.getStandardPlayer(sender);
		
		if (sender == null) {
			command.showPlayerOnlyMessage(sender);
			return false;
		}
		
		GroupManager groupManager = StandardGroups.getPlugin().getGroupManager();
		
		if (groupManager.hasCommandCooldown(new String(player.getUuidString()), true)) {
			groupManager.enableCommandCooldown(new String(player.getUuidString()));
			return false;
		}
		
		groupManager.leaveGroup(player);
		
		groupManager.enableCommandCooldown(new String(player.getUuidString()));
		
		return true;
	}

}
