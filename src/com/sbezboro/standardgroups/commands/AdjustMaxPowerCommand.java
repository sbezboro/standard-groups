package com.sbezboro.standardgroups.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.commands.BaseCommand;
import com.sbezboro.standardplugin.commands.SubCommand;
import com.sbezboro.standardplugin.model.StandardPlayer;

public class AdjustMaxPowerCommand extends SubCommand {

	//
	// simplify thing by disabling this command, so max power is always 10
	//

	// This command allows group leaders to exchange their max power for more power damage and vice versa

	public AdjustMaxPowerCommand(StandardPlugin plugin, BaseCommand command) {
		super(plugin, command, "adjustmaxpower");

		// addHelp(ChatColor.YELLOW + "/g adjustmaxpower <value>" + ChatColor.RESET + " - lets you control your group's max power and power damage");
	}

	@Override
	public boolean handle(CommandSender sender, String[] args) {
		sender.sendMessage(new String[]{"This command has been removed in SS 10"});
		return true;
	}

}
