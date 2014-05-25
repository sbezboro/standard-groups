package com.sbezboro.standardgroups.commands;

import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.commands.BaseCommand;
import com.sbezboro.standardplugin.commands.SubCommand;
import com.sbezboro.standardplugin.util.PaginatedOutput;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;

public class HelpCommand extends SubCommand {
	
	private Collection<SubCommand> otherCommands;

	public HelpCommand(StandardPlugin plugin, BaseCommand command, Collection<SubCommand> otherCommands) {
		super(plugin, command, "help");
		
		this.otherCommands = otherCommands;
	}

	@Override
	public boolean handle(CommandSender sender, String[] args) {
		Set<SubCommand> commands = new TreeSet<SubCommand>(otherCommands);
		int page;

		if (args.length == 1) {
			try {
				page = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				page = 1;
			}
		} else {
			page = 1;
		}

		PaginatedOutput paginatedOutput = new PaginatedOutput("Groups Help", page);

		for (SubCommand subCommand : commands) {
			subCommand.showHelp(paginatedOutput);
		}

		paginatedOutput.show(sender);
		
		return true;
	}

}
