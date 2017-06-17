package com.sbezboro.standardgroups.commands;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.commands.BaseCommand;
import com.sbezboro.standardplugin.commands.SubCommand;
import com.sbezboro.standardplugin.model.StandardPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class GroupMessageCommand extends SubCommand {

	public GroupMessageCommand(StandardPlugin plugin, BaseCommand command) {
		super(plugin, command, "gm");

		addHelp(ChatColor.YELLOW + "/g gm <message>" + ChatColor.RESET + " - set a message for your group members to see upon login");
		addHelp(ChatColor.YELLOW + "/g gm off" + ChatColor.RESET + " - clear message shown to group members upon login");
	}

	@Override
	public boolean handle(CommandSender sender, String[] args) {
		StandardPlayer player = plugin.getStandardPlayer(sender);
		
		if (player == null) {
			command.showPlayerOnlyMessage(sender);
			return false;
		}
		
		GroupManager groupManager = StandardGroups.getPlugin().getGroupManager();
		if (args.length == 0) {
			groupManager.disableGroupMessage(player);
		} else {
			groupManager.setGroupMessage(player, String.join(" ", args));
		}
		
		return true;
	}
	
}
