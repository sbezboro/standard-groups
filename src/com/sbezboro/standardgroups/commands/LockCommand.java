package com.sbezboro.standardgroups.commands;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.commands.BaseCommand;
import com.sbezboro.standardplugin.commands.SubCommand;
import com.sbezboro.standardplugin.model.StandardPlayer;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class LockCommand extends SubCommand {

	public LockCommand(StandardPlugin plugin, BaseCommand command) {
		super(plugin, command, "lock");

		addHelp(ChatColor.YELLOW + "/g lock" + ChatColor.RESET + " - lock the block being looked at");
		addHelp(ChatColor.YELLOW + "/g lock add <player1> <player2>..." + ChatColor.RESET + " - give one or more players access to an existing lock");
		addHelp(ChatColor.YELLOW + "/g lock remove <player1> <player2>..." + ChatColor.RESET + " - revoke one or more players' access to an existing lock");
		addHelp(ChatColor.YELLOW + "/g lock public" + ChatColor.RESET + " - toggle public access to a lock");
		addHelp(ChatColor.YELLOW + "/g lock info" + ChatColor.RESET + " - show info about the lock being looked at");
	}

	@Override
	public boolean handle(CommandSender sender, String[] args) {
		StandardPlayer player = plugin.getStandardPlayer(sender);
		
		if (player == null) {
			command.showPlayerOnlyMessage(sender);
			return false;
		}
		
		GroupManager groupManager = StandardGroups.getPlugin().getGroupManager();

		Block block = player.getTargetBlock(3);

		if (args.length == 0) {
			if (block == null) {
				sender.sendMessage("No block in range");
				return true;
			}

			groupManager.lock(player, block);

			return true;
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("info")) {
				if (block == null) {
					sender.sendMessage("No block in range");
					return true;
				}

				groupManager.lockInfo(player, block);

				return true;
			} else if (args[0].equalsIgnoreCase("public")) {
				if (block == null) {
					sender.sendMessage("No block in range");
					return true;
				}

				groupManager.togglePublicLock(player, block);

				return true;
			}
		} else {
			if (block == null) {
				sender.sendMessage("No block in range");
				return true;
			}

			List<StandardPlayer> players = new ArrayList<>();

			for (int i = 1; i < args.length; ++i) {
				StandardPlayer otherPlayer = plugin.matchPlayer(args[i]);

				if (otherPlayer == null) {
					sender.sendMessage("Player " + args[i] + " doesn't exist");
					return true;
				}

				players.add(otherPlayer);
			}

			if (args[0].equalsIgnoreCase("add")) {
				groupManager.addLockMembers(player, block, players);

				return true;
			} else if (args[0].equalsIgnoreCase("remove")) {
				groupManager.removeLockMembers(player, block, players);

				return true;
			}
		}

		showHelp(sender);
		return false;
	}

}
