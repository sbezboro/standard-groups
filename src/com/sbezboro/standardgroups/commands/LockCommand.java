package com.sbezboro.standardgroups.commands;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.commands.BaseCommand;
import com.sbezboro.standardplugin.commands.SubCommand;
import com.sbezboro.standardplugin.model.StandardPlayer;
import com.sbezboro.standardplugin.util.PaginatedOutput;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class LockCommand extends SubCommand {

	public LockCommand(StandardPlugin plugin, BaseCommand command) {
		super(plugin, command, "lock");

		addHelp(ChatColor.YELLOW + "/g lock" + ChatColor.RESET + " - lock the block being looked at");
		addHelp(ChatColor.YELLOW + "/g lock add <player>" + ChatColor.RESET + " - give a player access to an existing lock");
		addHelp(ChatColor.YELLOW + "/g lock remove <player>" + ChatColor.RESET + " - revoke a player's access to an existing lock");
		addHelp(ChatColor.YELLOW + "/g lock public" + ChatColor.RESET + " - toggle public access to a lock");
		addHelp(ChatColor.YELLOW + "/g lock info" + ChatColor.RESET + " - show info about the lock being looked at");
	}

	@Override
	public boolean handle(CommandSender sender, String[] args) {
		StandardPlayer player = plugin.getStandardPlayer(sender);
		
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
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("add")) {
				if (block == null) {
					sender.sendMessage("No block in range");
					return true;
				}

				StandardPlayer otherPlayer = plugin.matchPlayer(args[1]);

				if (otherPlayer == null) {
					sender.sendMessage("That player doesn't exist");
					return true;
				}

				groupManager.addLockMember(player, block, otherPlayer);

				return true;
			} else if (args[0].equalsIgnoreCase("remove")) {
				if (block == null) {
					sender.sendMessage("No block in range");
					return true;
				}

				StandardPlayer otherPlayer = plugin.matchPlayer(args[1]);

				if (otherPlayer == null) {
					sender.sendMessage("That player doesn't exist");
					return true;
				}

				groupManager.removeLockMember(player, block, otherPlayer);

				return true;
			}
		}

		showHelp(sender);
		return false;
	}

}
