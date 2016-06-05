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
	
	// This command allows group leaders to exchange their max power for more power damage and vice versa

	public AdjustMaxPowerCommand(StandardPlugin plugin, BaseCommand command) {
		super(plugin, command, "adjustmaxpower");

		addHelp(ChatColor.YELLOW + "/g adjustmaxpower" + ChatColor.RESET + " - lets you control your group's max power and power damage");
	}

	@Override
	public boolean handle(CommandSender sender, String[] args) {
		// No additional arguments given, display basic info
		if (args.length == 0) {
			sender.sendMessage(new String[] { "This command lets you control your group's maximum power.",
					"If you lower your max power, you will deal more power damage when killing players.",
					"If you choose to raise it, your power damage will reduce.",
					"Enter " + ChatColor.GOLD + "/g adjustmaxpower <value>" + ChatColor.RESET + " to learn more." });
		}
		
		// Number given, display the effects this adjustment would have and ask for confirmation
		else if (args.length == 1) {
			double adjustment;
			
			try {
				adjustment = Double.parseDouble(args[0]);
				GroupManager groupManager = StandardGroups.getPlugin().getGroupManager();
				StandardPlayer player = plugin.getStandardPlayer(sender);
				if (player == null) {
					command.showPlayerOnlyMessage(sender);
					return false;
				}
				groupManager.adjustMaxPowerInfo(player, adjustment);
			}
			catch (NumberFormatException e) {
				sender.sendMessage("\"" + args[0] + "\" is not a valid number");
				return false;
			}
		}
		
		// Number and "confirm" given, activate the adjustment
		else if (args.length == 2) {
			double adjustment;
			
			try {
				adjustment = Double.parseDouble(args[0]);
				
				if (args[1].equals("confirm")) {
					GroupManager groupManager = StandardGroups.getPlugin().getGroupManager();
					StandardPlayer player = plugin.getStandardPlayer(sender);
					if (player == null) {
						command.showPlayerOnlyMessage(sender);
						return false;
					}
					groupManager.adjustMaxPower(player, adjustment);
				} else {
					command.showUsageInfo(sender);
					return false;
				}
			}
			catch (NumberFormatException e) {
				sender.sendMessage("\"" + args[0] + "\" is not a valid number");
				return false;
			}
		}
		
		else {
			command.showUsageInfo(sender);
			return false;
		}
		
		return true;
	}

}
