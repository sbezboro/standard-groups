package com.sbezboro.standardgroups.commands;

import org.bukkit.ChatColor;
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
		} else if (subCommand.equalsIgnoreCase("invite")) {
			return handleInvite(sender, subCommandArgs);
		} else if (subCommand.equalsIgnoreCase("join")) {
			return handleJoin(sender, subCommandArgs);
		} else if (subCommand.equalsIgnoreCase("leave")) {
			return handleLeave(sender);
		} else if (subCommand.equalsIgnoreCase("claim")) {
			return handleClaim(sender);
		} else if (subCommand.equalsIgnoreCase("unclaim")) {
			return handleUnclaim(sender);
		} else if (subCommand.equalsIgnoreCase("help")) {
			return handleHelp(sender);
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
		sender.sendMessage("Unknown command. Type " + ChatColor.AQUA + "/g help");
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
	
	private boolean handleClaim(CommandSender sender) {
		StandardPlayer player = plugin.getStandardPlayer(sender);
		
		GroupManager groupManager = StandardGroups.getPlugin().getGroupManager();
		groupManager.claim(player);
		
		return true;
	}
	
	private boolean handleUnclaim(CommandSender sender) {
		StandardPlayer player = plugin.getStandardPlayer(sender);
		
		GroupManager groupManager = StandardGroups.getPlugin().getGroupManager();
		groupManager.unclaim(player);
		
		return true;
	}
	
	private boolean handleHelp(CommandSender sender) {
		sender.sendMessage("Groups help:");
		sender.sendMessage("/g create <name> - create a group");
		sender.sendMessage("/g destroy - destroy a group you own");
		sender.sendMessage("/g invite <player> - invite a player to your group");
		sender.sendMessage("/g join <name> - attemp to join a group");
		sender.sendMessage("/g leave - leave a group you are in");
		sender.sendMessage("/g claim - claim land for your group");
		sender.sendMessage("/g unclaim - unclaim land from your group");
		
		return true;
	}

}
