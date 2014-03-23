package com.sbezboro.standardgroups.commands;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.commands.BaseCommand;
import com.sbezboro.standardplugin.commands.SubCommand;
import com.sbezboro.standardplugin.model.StandardPlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;

public class LockCommand extends SubCommand {

	public LockCommand(StandardPlugin plugin, BaseCommand command) {
		super(plugin, command, "lock");
	}

	@Override
	public boolean handle(CommandSender sender, String[] args) {
		StandardPlayer player = plugin.getStandardPlayer(sender);
		
		GroupManager groupManager = StandardGroups.getPlugin().getGroupManager();

		if (args.length == 0) {
			Block block = player.getTargetBlock(3);

			if (block == null) {
				sender.sendMessage("No block in range");
				return true;
			}

			groupManager.lock(player, block);
		}
		
		return true;
	}

	@Override
	public void showHelp(CommandSender sender) {
		sender.sendMessage("/g lock - lock the block being looked at");
		sender.sendMessage("/g lock add <player> - give a player access to a block");
		sender.sendMessage("/g lock remove <player> - revoke access to a block");
	}
}
