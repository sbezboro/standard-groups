package com.sbezboro.standardgroups.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardgroups.managers.GroupManager;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginCommand;
import com.sbezboro.standardplugin.model.StandardPlayer;

public class GroupCommand extends SubPluginCommand<StandardGroups> {

	public GroupCommand(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin, "groups");
	}

	@Override
	public boolean handle(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			showUsageInfo(sender);
			return false;
		}

		String subCommand = args[0];
		String[] subCommandArgs = new String[args.length - 1];
		System.arraycopy(args, 1, subCommandArgs, 0, args.length - 1);
		
		if (subCommand.equalsIgnoreCase("create")) {
			return handleCreate(sender, subCommandArgs);
		} else if (subCommand.equalsIgnoreCase("destroy")) {
			return handleDestroy(sender);
		} else if (subCommand.equals("invite")) {
			return handleInvite(sender, subCommandArgs);
		} else if (subCommand.equals("join")) {
			return handleJoin(sender, subCommandArgs);
		} else if (subCommand.equals("leave")) {
			return handleLeave(sender);
		}

		showUsageInfo(sender);
		return false;
	}

	@Override
	public boolean isPlayerOnly(int numArgs) {
		return false;
	}

	@Override
	public void showUsageInfo(CommandSender sender) {

	}
	
	private boolean handleCreate(CommandSender sender, String[] args) {
		if (args.length != 1) {
			sender.sendMessage("You must provide a name for your group.");
			return false;
		}
		
		StandardPlayer player = plugin.getStandardPlayer(sender);
		
		GroupManager groupManager = StandardGroups.getPlugin().getGroupManager();
		groupManager.createGroup(player, args[0]);
		
		return true;
	}
	
	private boolean handleDestroy(CommandSender sender) {
		StandardPlayer player = plugin.getStandardPlayer(sender);
		
		GroupManager groupManager = StandardGroups.getPlugin().getGroupManager();
		groupManager.destroyGroup(player);
		
		return true;
	}
	
	private boolean handleInvite(CommandSender sender, String[] args) {
		if (args.length != 1) {
			sender.sendMessage("You must specify a player to invite.");
			return false;
		}
		
		StandardPlayer player = plugin.getStandardPlayer(sender);
		
		GroupManager groupManager = StandardGroups.getPlugin().getGroupManager();
		groupManager.invitePlayer(player, args[0]);
		
		return true;
	}
	
	private boolean handleJoin(CommandSender sender, String[] args) {
		if (args.length != 1) {
			sender.sendMessage("You must specify which group name you would like to join.");
			return false;
		}
		
		StandardPlayer player = plugin.getStandardPlayer(sender);
		
		GroupManager groupManager = StandardGroups.getPlugin().getGroupManager();
		groupManager.joinGroup(player, args[0]);
		
		return true;
	}
	
	private boolean handleLeave(CommandSender sender) {
		StandardPlayer player = plugin.getStandardPlayer(sender);
		
		GroupManager groupManager = StandardGroups.getPlugin().getGroupManager();
		groupManager.leaveGroup(player);
		
		return true;
	}

}
