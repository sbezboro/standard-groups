package com.sbezboro.standardgroups.commands;

import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.commands.BaseCommand;
import com.sbezboro.standardplugin.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class HelpCommand extends SubCommand {
	
	private Collection<SubCommand> otherCommands;

	public HelpCommand(StandardPlugin plugin, BaseCommand command, Collection<SubCommand> otherCommands) {
		super(plugin, command, "help");
		
		this.otherCommands = otherCommands;
	}

	@Override
	public boolean handle(CommandSender sender, String[] args) {
		sender.sendMessage(ChatColor.GOLD + "============== " + ChatColor.YELLOW + "Groups Help" + ChatColor.GOLD + " ==============");

		Set<SubCommand> commands = new TreeSet<SubCommand>(otherCommands);

		for (SubCommand subCommand : commands) {
			subCommand.showHelp(sender);
		}
		
		return true;
	}

	@Override
	public void showHelp(CommandSender sender) {
	}

}
