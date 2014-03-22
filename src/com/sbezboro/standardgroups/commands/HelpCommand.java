package com.sbezboro.standardgroups.commands;

import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.commands.BaseCommand;
import com.sbezboro.standardplugin.commands.SubCommand;

public class HelpCommand extends SubCommand {
	
	private Collection<SubCommand> otherCommands;

	public HelpCommand(StandardPlugin plugin, BaseCommand command, Collection<SubCommand> otherCommands) {
		super(plugin, command, "help");
		
		this.otherCommands = otherCommands;
	}

	@Override
	public boolean handle(CommandSender sender, String[] args) {
		sender.sendMessage(ChatColor.AQUA + "Groups help:");
		for (SubCommand subCommand : otherCommands) {
			subCommand.showHelp(sender);
		}
		
		return true;
	}

	@Override
	public void showHelp(CommandSender sender) {
	}

}
