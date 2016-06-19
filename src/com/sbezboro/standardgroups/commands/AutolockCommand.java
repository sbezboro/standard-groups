package com.sbezboro.standardgroups.commands;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardgroups.model.Group;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.commands.BaseCommand;
import com.sbezboro.standardplugin.commands.SubCommand;
import com.sbezboro.standardplugin.model.StandardPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class AutolockCommand extends SubCommand {

	// Alternative way for locking a lot of blocks. Particularly useful with the new command cooldown.
	// The hand of the player becomes "charged" and the respective
	// /g lock command will be executed on every block the player punches
	
	public AutolockCommand(StandardPlugin plugin, BaseCommand command) {
		super(plugin, command, "autolock");

		addHelp(ChatColor.YELLOW + "/g autolock [add/remove/public/info/unlock]" + ChatColor.RESET + " - lock several blocks by punching them");
		addHelp(ChatColor.YELLOW + "/g autolock off" + ChatColor.RESET + " - disable autolocking when punching");
	}

	@Override
	public boolean handle(CommandSender sender, String[] args) {
		StandardPlayer player = plugin.getStandardPlayer(sender);
		
		GroupManager groupManager = StandardGroups.getPlugin().getGroupManager();
		
		if (player == null) {
			command.showPlayerOnlyMessage(sender);
			return false;
		}

		String uuid = new String(player.getUuidString());

		Group group = groupManager.getPlayerGroup(player);
		
		if (group == null) {
			player.sendMessage("You must be in a group before you can use this command");
			return false;
		}
		
		String[] autoCommandArgs = new String[args.length + 1];
		autoCommandArgs[0] = "lock";
		if (args.length >= 1) {
			System.arraycopy(args, 0, autoCommandArgs, 1, args.length);
		}

		if (args.length == 0) {
			group.enableAutoCommand(uuid, autoCommandArgs);
			return true;
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("off")) {
				group.disableAutoCommand(uuid);
				return true;
			}
			
			else if (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("public")) {
				group.enableAutoCommand(uuid, autoCommandArgs);
				return true;
			} else if (args[0].equalsIgnoreCase("unlock")) {
				autoCommandArgs = new String[] { "unlock" };
				group.enableAutoCommand(uuid, autoCommandArgs);
				return true;
			}
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
				StandardPlayer otherPlayer = plugin.matchPlayer(args[1]);

				if (otherPlayer == null) {
					sender.sendMessage("That player doesn't exist");
					return true;
				}

				group.enableAutoCommand(uuid, autoCommandArgs);
				return true;
			}
		}

		command.showUsageInfo(sender);
		return false;
	}

}
