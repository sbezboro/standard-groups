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

public class ChatCommand extends SubCommand {

	public ChatCommand(StandardPlugin plugin, BaseCommand command) {
		super(plugin, command, "chat", new ArrayList<String>() {{
			add("c");
		}});
	}

	@Override
	public boolean handle(CommandSender sender, String[] args) {
		StandardPlayer player = plugin.getStandardPlayer(sender);
		
		GroupManager groupManager = StandardGroups.getPlugin().getGroupManager();
		groupManager.toggleChat(player);
		
		return true;
	}

	@Override
	public void showHelp(CommandSender sender) {
		sender.sendMessage(ChatColor.YELLOW + "/g chat" + ChatColor.RESET + " - switch chat modes");
	}
	
}
