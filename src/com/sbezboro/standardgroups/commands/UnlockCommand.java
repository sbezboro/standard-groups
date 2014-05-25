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

public class UnlockCommand extends SubCommand {

	public UnlockCommand(StandardPlugin plugin, BaseCommand command) {
		super(plugin, command, "unlock");

		addHelp(ChatColor.YELLOW + "/g unlock" + ChatColor.RESET + " - unlock the block being looked at");
	}

	@Override
	public boolean handle(CommandSender sender, String[] args) {
		StandardPlayer player = plugin.getStandardPlayer(sender);
		
		GroupManager groupManager = StandardGroups.getPlugin().getGroupManager();

		Block block = player.getTargetBlock(3);

		if (block == null) {
			sender.sendMessage("No block in range");
			return true;
		}

		groupManager.unlock(player, block);
		
		return true;
	}

}
