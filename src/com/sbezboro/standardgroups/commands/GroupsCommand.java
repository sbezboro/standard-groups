package com.sbezboro.standardgroups.commands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.sbezboro.standardgroups.StandardGroups;
import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.SubPluginCommand;
import com.sbezboro.standardplugin.commands.SubCommand;

public class GroupsCommand extends SubPluginCommand<StandardGroups> {
	
	private Map<String, SubCommand> subCommands;
	
	public GroupsCommand(StandardPlugin plugin, StandardGroups subPlugin) {
		super(plugin, subPlugin, "groups");
		
		subCommands = new HashMap<String, SubCommand>();
		
		addSubCommand(new CreateCommand(plugin, this));
		addSubCommand(new ClaimCommand(plugin, this));
		addSubCommand(new DestroyCommand(plugin, this));
		addSubCommand(new InviteCommand(plugin, this));
		addSubCommand(new JoinCommand(plugin, this));
		addSubCommand(new LeaveCommand(plugin, this));
		addSubCommand(new RenameCommand(plugin, this));
		addSubCommand(new UnclaimCommand(plugin, this));
		addSubCommand(new InfoCommand(plugin, this));
		addSubCommand(new PowerCommand(plugin, this));
		addSubCommand(new GroupMessageCommand(plugin, this));
		addSubCommand(new AutolockCommand(plugin, this));
		addSubCommand(new LockCommand(plugin, this));
		addSubCommand(new UnlockCommand(plugin, this));
		addSubCommand(new KickCommand(plugin, this));
		addSubCommand(new ListCommand(plugin, this));
		addSubCommand(new ModCommand(plugin, this));
		addSubCommand(new UnmodCommand(plugin, this));
		addSubCommand(new LeaderCommand(plugin, this));
		addSubCommand(new ChatCommand(plugin, this));
		addSubCommand(new MapCommand(plugin, this));
		addSubCommand(new ShowCommand(plugin, this));
		addSubCommand(new UninviteCommand(plugin, this));
		addSubCommand(new FriendCommand(plugin, this));
		addSubCommand(new UnfriendCommand(plugin, this));
		addSubCommand(new HelpCommand(plugin, this, subCommands.values()));
	}

	@Override
	public boolean handle(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			subCommands.get("help").handle(sender, new String[] {});
			return false;
		}

		String[] subCommandArgs = new String[args.length - 1];
		System.arraycopy(args, 1, subCommandArgs, 0, args.length - 1);
		
		SubCommand subCommand = subCommands.get(args[0]);
		if (subCommand != null) {
			return subCommand.handle(sender, subCommandArgs);
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
	
	private void addSubCommand(SubCommand subCommand) {
		subCommands.put(subCommand.getCommandName(), subCommand);
		for (String alternative : subCommand.getAlternatives()) {
			subCommands.put(alternative, subCommand);
		}
	}

}
